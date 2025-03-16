package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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
                    .createScoped(List.of(CalendarScopes.CALENDAR_READONLY));

        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName(properties.getApplicationName())
                .build();
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

        EventExtensionDto eventExtensionDto = buildFromJson(event);
        if (eventExtensionDto != null) {
            return eventExtensionDto;
        }

        eventExtensionDto = buildFromPlainText(event);
        if (eventExtensionDto == null) {
            throw new CannotBuildEventException(event.getSummary(), event.getStart());
        }

        return eventExtensionDto;
    }

    @Nullable
    private EventExtensionDto buildFromJson(Event event) {
        try {
            return objectMapper.readValue(event.getDescription(), EventExtensionDto.class);
        } catch (Exception e) {
            log.trace("Не удалось собрать EventExtensionDto в виде JSON, неверный формат", e);
            return null;
        }
    }

    private EventExtensionDto buildFromPlainText(Event event) {
        try {
            return PlainTextMapper.fillFromString(event.getDescription(), EventExtensionDto.class);
        } catch (Exception e) {
            log.trace("Не удалось собрать EventExtensionDto в виде PLAIN TEXT, неверный формат", e);
            return null;
        }
    }
}
