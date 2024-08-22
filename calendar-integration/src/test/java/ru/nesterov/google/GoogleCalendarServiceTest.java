package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.Event;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        GoogleCalendarService.class,
        ObjectMapper.class
})
@EnableConfigurationProperties(GoogleCalendarProperties.class)
class GoogleCalendarServiceTest {
    @Autowired
    GoogleCalendarService googleCalendarService;
    @MockBean
    private GoogleCalendarClient googleCalendarClient;
    @Autowired
    private GoogleCalendarProperties properties;

    @BeforeEach
    public void init() throws IOException {
        when(googleCalendarClient
                .getEventsBetweenDates(eq(properties.getMainCalendarId()), any(), any()))
                .thenReturn(List.of(
                        Event.builder()
                        .colorId("1")
                        .summary("event from main calendar 1")
                        .start(LocalDateTime.of(2024, 01, 01, 00, 00))
                        .end(LocalDateTime.of(2024, 01, 01, 01, 00))
                        .build(),
                        Event.builder()
                                .colorId("11")
                                .summary("event from main calendar 2")
                                .start(LocalDateTime.of(2024, 01, 01, 00, 00))
                                .end(LocalDateTime.of(2024, 01, 01, 01, 00))
                                .build()
                        )
                );
        when(googleCalendarClient
                .getEventsBetweenDates(eq(properties.getCancelledCalendarId()), any(), any()))
                .thenReturn(List.of(
                                Event.builder()
                                        .colorId("1")
                                        .summary("event from canceled calendar 1")
                                        .start(LocalDateTime.of(2023, 12, 01, 00, 00))
                                        .end(LocalDateTime.of(2023, 12, 01, 01, 00))
                                        .build(),
                                Event.builder()
                                        .colorId("11")
                                        .summary("event from canceled calendar 2")
                                        .start(LocalDateTime.of(2023, 12, 01, 00, 00))
                                        .end(LocalDateTime.of(2023, 12, 01, 01, 00))
                                        .build()
                        )
                );

    }

    @Test
    public void getEventsBetweenDateWhenCancelledCalendarEnabled() {
        LocalDateTime leftDate = LocalDateTime.of(2023, 01, 01, 00, 00);
        LocalDateTime rightDate = LocalDateTime.of(2024, 01, 01, 00, 00);

        List<ru.nesterov.dto.Event> events = googleCalendarService.getEventsBetweenDates(leftDate, rightDate);
        assertNotNull(events);
        assertEquals(4, events.size());
//        assertEquals("from main calendar", events.get(0).getSummary());
//        assertEquals("from cancelled calendar", events.get(1).getSummary());
    }
}