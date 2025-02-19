package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.google.exception.CannotBuildEventException;
import ru.nesterov.util.PlainTextMapper;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
Основные методы Google Calendar API для работы с событиями:

1. delete(String calendarId, String eventId)
   - Удаляет событие из календаря.

2. get(String calendarId, String eventId)
   - Получает информацию о конкретном событии.

3. importEvent(String calendarId, Event event)
   - Импортирует событие в календарь без отправки приглашений.

4. insert(String calendarId, Event event)
   - Создаёт новое событие в календаре.

5. instances(String calendarId, String eventId)
   - Возвращает список экземпляров повторяющегося события.

6. list(String calendarId)
   - Получает список событий с возможностью фильтрации и сортировки.

7. move(String calendarId, String eventId, String destinationCalendarId)
   - Перемещает событие в другой календарь.

8. patch(String calendarId, String eventId, Event event)
   - Частично обновляет событие (обновляет только указанные поля).

9. update(String calendarId, String eventId, Event event)
   - Полностью обновляет событие.

10. watch(String calendarId, Channel channel)
    - Устанавливает уведомления об изменении событий в календаре.
*/

@Slf4j
@Component
@ConditionalOnProperty("app.google.calendar.integration.enabled")
public class GoogleCalendarClient implements CalendarClient {
    private final Calendar calendar;
    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper;
    private final EventStatusService eventStatusService;

    public GoogleCalendarClient(GoogleCalendarProperties properties, ObjectMapper objectMapper, EventStatusService eventStatusService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.eventStatusService = eventStatusService;
        this.calendar = createCalendarService();
    }

    @SneakyThrows
    private Calendar createCalendarService() {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(properties.getServiceAccountFilePath()))
                .createScoped(List.of(CalendarScopes.CALENDAR));

        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName(properties.getApplicationName())
                .build();
    }

    /**
     * Работает только с неповторяющимися событиями. Для повторяющихся использовать метод copy insertEventsToOtherCalendar
     * @param sourceCalendarId
     * @param targetCalendarId
     * @param leftDate
     * @param rightDate
     */
    @SneakyThrows
    public void moveEventsToOtherCalendar(String sourceCalendarId, String targetCalendarId, LocalDateTime leftDate, LocalDateTime rightDate) {
        List<Events> eventsList = getEventsBetweenDates(
                sourceCalendarId,
                Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant())
        );

        List<Event> events = eventsList.stream()
                .flatMap(e -> e.getItems().stream())
                .toList();

        log.info("Перенос {} событий из календаря [{}] в календарь [{}]", events.size(), sourceCalendarId, targetCalendarId);
        for (Event event: events) {
            calendar.events()
                    .move(sourceCalendarId, event.getId(), targetCalendarId)
                    .execute();
            log.debug("Событие [{}] перенесено в календарь [{}]", event.getSummary(), targetCalendarId);
        }

        log.info("Все события успешно перенесены из календаря [{}] в календарь [{}]", sourceCalendarId, targetCalendarId);
    }

    @SneakyThrows
    public void insertEventsToOtherCalendar(String sourceCalendarId, String targetCalendarId, LocalDateTime leftDate, LocalDateTime rightDate) {
        List<Events> eventsList = getEventsBetweenDates(
                sourceCalendarId,
                Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant())
        );

        BatchRequest batch = calendar.batch();

        List<Event> events = eventsList.stream()
                .flatMap(e -> e.getItems().stream())
                .toList();

        for (Event event : events) {
            try {
                Event newEvent = new Event()
                        .setSummary(event.getSummary())
                        .setDescription(event.getDescription())
                        .setStart(event.getStart())
                        .setEnd(event.getEnd())
                        .setAttendees(event.getAttendees());

                calendar.events()
                        .insert(targetCalendarId, newEvent)
                        .queue(batch, new JsonBatchCallback<Event>() {
                            @Override
                            public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) throws IOException {
                                log.debug("Событие [{}] не будет перенесено", event);
                            }

                            @Override
                            public void onSuccess(Event event, HttpHeaders httpHeaders) throws IOException {
                                if (event.getSummary().equals("error")) {
                                    throw new RuntimeException();
                                }

                                log.debug("Событие [{}] перенесено в календарь [{}]", event.getSummary(), targetCalendarId);
                            }
                        });

            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 409) {
                    log.debug("Событие [{}] уже имеется в календаре [{}]", event, targetCalendarId);
                }
            }
        }

        batch.execute();
    }

    private com.google.api.services.calendar.model.Event buildGoogleEvent(EventDto eventDto) {
        com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event()
                .setSummary(eventDto.getSummary())
                .setStart(new com.google.api.services.calendar.model.EventDateTime()
                        .setDateTime(new DateTime(eventDto.getStart().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())))
                .setEnd(new com.google.api.services.calendar.model.EventDateTime()
                        .setDateTime(new DateTime(eventDto.getEnd().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())));

        if (eventDto.getEventExtensionDto() != null) {
            try {
                String description = objectMapper.writeValueAsString(eventDto.getEventExtensionDto());
                event.setDescription(description);
            } catch (Exception e) {
                log.warn("Не удалось сериализовать EventExtensionDto для события [{}]", eventDto.getSummary(), e);
            }
        }

        return event;
    }

    @SneakyThrows
    public List<EventDto> getEventsBetweenDates(String calendarId, boolean isCancelledCalendar, LocalDateTime leftDate,
                                                LocalDateTime rightDate, List<EventStatus> requiredStatuses) {
        Date startTime = Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant());

        List<Events> events = getEventsBetweenDates(calendarId, startTime, endTime);
        return events.stream()
                .flatMap(e -> e.getItems().stream())
                .map(event -> buildEvent(event, isCancelledCalendar))
                .filter(dto -> {
                    if (requiredStatuses == null) {
                        return true;
                    }
                    return requiredStatuses.contains(dto.getStatus());
                })
                .toList();
    }

    private List<Events> getEventsBetweenDates(String calendarId, Date startTime, Date endTime) throws IOException {
        int pageNumber = 1;

        List<Events> allEvents = new ArrayList<>();

        Events events = getEventsBetweenDates(calendarId, startTime, endTime, null);
        allEvents.add(events);
        log.debug("Для calendarId = [{}] [{} - {}] извлечена страница №[{}]", calendarId, startTime, endTime, pageNumber);

        while (events.getNextPageToken() != null) {
            events = getEventsBetweenDates(calendarId, startTime, endTime, events.getNextPageToken());
            allEvents.add(events);
            pageNumber++;
            log.debug("Для calendarId = [{}] [{} - {}] извлечена страница №[{}]", calendarId, startTime, endTime, pageNumber);
        }

        return allEvents;
    }

    private Events getEventsBetweenDates(String calendarId, Date startTime, Date endTime, String nextPageToken) throws IOException {
        log.debug("Send request to google");
        Events events = calendar.events().list(calendarId)
                .setTimeMin(new DateTime(startTime))
                .setTimeMax(new DateTime(endTime))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setPageToken(nextPageToken)
                .execute();
        log.debug("Response from google received");
        return events;
    }

    private EventDto buildEvent(com.google.api.services.calendar.model.Event event, boolean isCancelledCalendar) {
        try {
            return EventDto.builder()
                    .status(isCancelledCalendar ? EventStatus.CANCELLED : eventStatusService.getEventStatus(event))
                    .summary(event.getSummary())
                    .start(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getStart().getDateTime().getValue()), ZoneId.systemDefault()))
                    .end(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()), ZoneId.systemDefault()))
                    .eventExtensionDto(buildEventExtension(event))
                    .build();
        } catch (Exception e) {
            throw new CannotBuildEventException(event.getSummary(), event.getStart(), e);
        }
    }

    @Nullable
    private EventExtensionDto buildEventExtension(com.google.api.services.calendar.model.Event event) {
        log.trace("Сборка EventExtensionDto для EventDto с названием [{}] and date [{}]", event.getSummary(), event.getStart());

        if (event.getDescription() == null) {
            log.trace("EventDto не содержит EventExtensionDto");
            return null;
        }

        EventExtensionDto eventExtensionDto = buildFromPlainText(event.getDescription());
        if (eventExtensionDto != null) {
            return eventExtensionDto;
        }

        return buildFromJson(event.getDescription());
    }

    private EventExtensionDto buildFromJson(String description) {
        try {
            return objectMapper.readValue(description, EventExtensionDto.class);
        } catch (Exception e) {
            log.trace("Не удалось собрать EventExtensionDto в виде JSON, неверный формат", e);
            return null;
        }
    }

    private EventExtensionDto buildFromPlainText(String description) {
        try {
            return PlainTextMapper.fillFromString(description, EventExtensionDto.class);
        } catch (Exception e) {
            log.trace("Не удалось собрать EventExtensionDto в виде PLAIN TEXT, неверный формат", e);
            return null;
        }
    }
}
