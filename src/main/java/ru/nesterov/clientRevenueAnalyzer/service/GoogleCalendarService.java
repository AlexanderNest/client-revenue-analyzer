package ru.nesterov.clientRevenueAnalyzer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.clientRevenueAnalyzer.properties.CalendarProperties;


import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class GoogleCalendarService {
    private final JsonFactory JSON_FACTORY ;
    private final Calendar calendar;

    private final CalendarProperties properties;

    public GoogleCalendarService(CalendarProperties properties) {
        this.properties = properties;
        this.JSON_FACTORY = JacksonFactory.getDefaultInstance();
        this.calendar = createCalendarService();
    }

    @SneakyThrows
    private Calendar createCalendarService() {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("\\client-revenue-analyzer\\oauth2template.json")) //TODO подставить свои значения
                    .createScoped(List.of(CalendarScopes.CALENDAR_READONLY));

        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(properties.getApplicationName())
                .build();
    }

    @SneakyThrows
    public List<Event> getEventsBetweenDates(LocalDateTime leftDate, LocalDateTime rightDate) {
        log.debug("Send request to google");
        Date startTime = Date.from(leftDate.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(rightDate.atZone(ZoneId.systemDefault()).toInstant());

        Events events = calendar.events().list(properties.getCalendarId())
                .setTimeMin(new com.google.api.client.util.DateTime(startTime))
                .setTimeMax(new com.google.api.client.util.DateTime(endTime))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        log.debug("Response from google received");
        return events.getItems();
    }

}
