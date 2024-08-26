package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventExtension;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.google.exception.CannotBuildEventException;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
    public List<Event> getEventsBetweenDates(String calendarId, boolean isCancelledCalendar, LocalDateTime leftDate, LocalDateTime rightDate) {
        Date startTime = Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant());

        Events events = getEventsBetweenDates(calendarId, startTime, endTime);
        return convert(events.getItems(), isCancelledCalendar);
    }

    private Events getEventsBetweenDates(String calendarId, Date startTime, Date endTime) throws IOException {
        log.debug("Send request to google");
        Events events = calendar.events().list(calendarId)
                .setTimeMin(new DateTime(startTime))
                .setTimeMax(new DateTime(endTime))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        log.debug("Response from google received");
        return events;
    }

    private List<Event> convert(List<com.google.api.services.calendar.model.Event> events, boolean isCancelledCalendar) {
        return events.stream()
                .map(event -> buildEvent(event, isCancelledCalendar))
                .toList();
    }

    private Event buildEvent(com.google.api.services.calendar.model.Event event, boolean isCancelledCalendar) {
        try {
            return Event.builder()
                    .status(isCancelledCalendar ? EventStatus.CANCELLED : eventStatusService.getEventStatus(event))
                    .summary(event.getSummary())
                    .start(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getStart().getDateTime().getValue()), ZoneId.systemDefault()))
                    .end(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()), ZoneId.systemDefault()))
                    .eventExtension(buildEventExtension(event))
                    .build();
        } catch (Exception e) {
            throw new CannotBuildEventException(event.getSummary(), event.getStart(), e);
        }
    }

    @Nullable
    private EventExtension buildEventExtension(com.google.api.services.calendar.model.Event event) {
        log.trace("Сборка EventExtension для Event с названием [{}] and date [{}]", event.getSummary(), event.getStart());

        if (event.getDescription() == null) {
            log.trace("Event не содержит EventExtension");
            return null;
        }

        try {
            return objectMapper.readValue(event.getDescription(), EventExtension.class);
        } catch (Exception e) {
            log.trace("Не удалось собрать EventExtension, неверный формат", e);
            return null;
        }
    }
}
