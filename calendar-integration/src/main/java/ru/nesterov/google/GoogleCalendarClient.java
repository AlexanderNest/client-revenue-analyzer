package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty("app.google.calendar.integration.enabled")
public class GoogleCalendarClient implements CalendarClient {
    private final Calendar calendar;
    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper;


    public GoogleCalendarClient(GoogleCalendarProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
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

    @SneakyThrows
    public void copyCancelledEventsToCancelledCalendar(String sourceCalendarId, String targetCalendarId, LocalDateTime leftDate, LocalDateTime rightDate, List<String> cancelledColorIds) {
        List<Event> events = getEventsBetweenDates(sourceCalendarId, false, leftDate, rightDate, cancelledColorIds);
        List<Event> targetEvents = getEventsBetweenDates(targetCalendarId, false, leftDate, rightDate, cancelledColorIds);
        Set<String> targetEventsId = targetEvents.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        List<Event> eventsToTransfer = events.stream()
                .filter(e -> !targetEventsId.contains(e.getId()))
                .toList();

        log.info("Копирование {} событий из календаря [{}] в календарь [{}]. [{}] событий уже находятся в целевом календаре", eventsToTransfer.size(), sourceCalendarId, targetCalendarId, events.size() - eventsToTransfer.size());

        for (Event event : eventsToTransfer) {
            try {
                calendar.events()
                        .insert(targetCalendarId, event)
                        .execute();

                log.debug("Событие [{}] перенесено в календарь [{}]", event.getSummary(), targetCalendarId);
            } catch (GoogleJsonResponseException e) {
                log.debug("Событие [{}] не будет перенесено", event, e);
                if (e.getStatusCode() == 409) {
                    log.debug("Событие [{}] уже имеется в календаре [{}]", event, targetCalendarId);
                }
            }
        }
    }

    @SneakyThrows
    public List<Event> getEventsBetweenDates(String calendarId, boolean isCancelledCalendar, LocalDateTime leftDate, LocalDateTime rightDate, List<String> colorsId) {
        Date startTime = Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant());

        List<Events> events = getEventsBetweenDates(calendarId, startTime, endTime);
        return events.stream()
                .flatMap(e -> e.getItems().stream())
                .filter(event -> {
                    if (colorsId == null) {
                        return true;
                    }
                    return colorsId.contains(event.getColorId());
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
}
