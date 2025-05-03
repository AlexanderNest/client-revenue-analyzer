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
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.exception.AppException;
import ru.nesterov.google.exception.CannotBuildEventException;
import ru.nesterov.google.exception.EventsBetweenDatesException;
import ru.nesterov.google.exception.UnprocessedBatchException;
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

import static ru.nesterov.google.mapper.EventMapper.mapEventToEvent;

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
public abstract class GoogleCalendarClient implements CalendarClient {

    private final Calendar calendar;
    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper;
    private final EventStatusService eventStatusService;
    private volatile boolean insertFailureFlag = false;

    public GoogleCalendarClient(GoogleCalendarProperties properties, ObjectMapper objectMapper, EventStatusService eventStatusService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.eventStatusService = eventStatusService;
        this.calendar = createCalendarService();
    }

    @SneakyThrows
    public Calendar createCalendarService() {
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
    public void moveEventsToOtherCalendar2(String sourceCalendarId, String targetCalendarId, LocalDateTime leftDate, LocalDateTime rightDate) {
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

    public void moveEventsToOtherCalendar(String sourceCalendarId, String targetCalendarId,
                                          LocalDateTime leftDate, LocalDateTime rightDate) {

        List<Events> eventsList;
        try {
            eventsList = getEventsBetweenDates(
                    sourceCalendarId,
                    Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant()),
                    Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant()));
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
            throw new EventsBetweenDatesException(ioe.getMessage());
        }

        List<Event> events = eventsList.stream()
                .flatMap(e -> e.getItems().stream())
                .filter(e -> e.getColorId() != null && e.getColorId().equals("11"))
                .toList();

        if (events.isEmpty()) { return; }

        BatchRequest insertBatch = calendar.batch();
        BatchRequest deleteBatch = calendar.batch();

        try {
            for (Event event : events) {
                Event copyEvent = mapEventToEvent(event);
                insertEventIntoCalendar(targetCalendarId, copyEvent, insertBatch);
                deleteEventFromCalendar(sourceCalendarId, event, deleteBatch);
            }
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
            throw new UnprocessedBatchException(ioe.getMessage());
        }

        synchronized (this) {
            try {
                insertBatch.execute();
                if (!insertFailureFlag) {
                    deleteBatch.execute();
                }

            } catch (GoogleJsonResponseException ge) {
                GoogleJsonError details = ge.getDetails();
                log.error(
                        "Ошибка Google API. Код: {}, Сообщение: {}, Детали: {}",
                        details.getCode(), details.getMessage(), details.getErrors()
                );
                throw new UnprocessedBatchException(details.getMessage());

            } catch (IOException ioe) {
                throw new UnprocessedBatchException(ioe.getMessage());

            } finally {
                insertFailureFlag = false;
            }
        }
    }

    private void insertEventIntoCalendar(String targetCalendarId, Event event, BatchRequest batch) throws IOException {

        calendar.events()
                .insert(targetCalendarId, event)
                .queue(batch, new JsonBatchCallback<>() {
                    @Override
                    public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
                        log.error("Событие [{}] не скопировано в календарь: [{}]", event, googleJsonError);
                        insertFailureFlag = true;
                    }

                    @Override
                    public void onSuccess(Event event, HttpHeaders httpHeaders) {}
                });
    }

    private void deleteEventFromCalendar(String targetCalendarId, Event event, BatchRequest batch) throws IOException {

        calendar.events()
                .delete(targetCalendarId, event.getId())
                .queue(batch, new JsonBatchCallback<>() {
                    @Override
                    public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
                        log.error("Событие [{}] не удалено из календаря: [{}]. Ошибка: [{}]",
                                event, targetCalendarId, googleJsonError);
                    }

                    @Override
                    public void onSuccess(Void unused, HttpHeaders httpHeaders) {
                        log.debug("Событие [{}] перенесено в календарь [{}]", event, targetCalendarId);
                    }
                });
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
    public List<EventDto> getEventsBetweenDates(String calendarId, CalendarType calendarType, LocalDateTime leftDate, LocalDateTime rightDate) {
        Date startTime = Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant());

        List<Events> events = getEventsBetweenDates(calendarId, startTime, endTime);
        return events.stream()
                .flatMap(e -> e.getItems().stream())
                .map(event -> buildEvent(event, calendarType))
                .toList();
    }

    private List<Events> getEventsBetweenDates(String calendarId, Date startTime, Date endTime) throws IOException {
        int pageNumber = 1;
        List<Events> allEvents = new ArrayList<>();
        String nextPageToken = null;

        do {
            Events events = getEventsBetweenDates(calendarId, startTime, endTime, nextPageToken);
            log.debug("Для calendarId = [{}] [{} - {}] извлечена страница №[{}]", calendarId, startTime, endTime, pageNumber);
            allEvents.add(events);
            nextPageToken = events.getNextPageToken();
            pageNumber++;
        } while (nextPageToken != null);

        return allEvents;
    }

    private Events getEventsBetweenDates(String calendarId, Date startTime, Date endTime, String pageToken) throws IOException {
        log.debug("Send request to google");
        Events events = calendar.events().list(calendarId)
                .setTimeMin(new DateTime(startTime))
                .setTimeMax(new DateTime(endTime))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setPageToken(pageToken)
                .execute();
        log.debug("Response from google received");
        return events;
    }

    private EventDto buildEvent(com.google.api.services.calendar.model.Event event, CalendarType calendarType) {
        EventStatus eventStatus;
        if (calendarType == CalendarType.CANCELLED) {
            eventStatus = EventStatus.CANCELLED;
        } else if (calendarType == CalendarType.PLAIN) {
            eventStatus = null;
        } else {
            eventStatus = eventStatusService.getEventStatus(event);
        }

        try {
            return EventDto.builder()
                    .status(eventStatus)
                    .summary(event.getSummary())
                    .start(getLocalDateTime(event.getStart()))
                    .end(getLocalDateTime(event.getEnd()))
                    .eventExtensionDto(buildEventExtension(event))
                    .build();
        } catch (Exception e) {
            throw new CannotBuildEventException(event.getSummary(), event.getStart(), e);
        }
    }

    private LocalDateTime getLocalDateTime (EventDateTime eventDateTime) {
        DateTime date;
        if(eventDateTime.getDateTime() != null) {
            date = eventDateTime.getDateTime();  // событие со временем и датой
        } else {
            date = eventDateTime.getDate(); // событие с датой на весь день
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getValue()), ZoneId.systemDefault());
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
