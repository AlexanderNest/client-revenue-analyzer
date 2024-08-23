package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
                                .colorId("4")
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
                                        .colorId("5")
                                        .summary("event from cancelled calendar 1")
                                        .start(LocalDateTime.of(2023, 12, 01, 00, 00))
                                        .end(LocalDateTime.of(2023, 12, 01, 01, 00))
                                        .build(),
                                Event.builder()
                                        .colorId("6")
                                        .summary("event from cancelled calendar 2")
                                        .start(LocalDateTime.of(2023, 12, 01, 00, 00))
                                        .end(LocalDateTime.of(2023, 12, 01, 01, 00))
                                        .build()
                        )
                );
    }

    @Test
    public void getEventsBetweenDateWhenCancelledCalendarEnabled() {
        properties.setCancelledCalendarEnabled(true);
        LocalDateTime leftDate = LocalDateTime.of(2023, 01, 01, 00, 00);
        LocalDateTime rightDate = LocalDateTime.of(2024, 01, 01, 00, 00);

        List<ru.nesterov.dto.Event> events = googleCalendarService.getEventsBetweenDates(leftDate, rightDate);
        assertNotNull(events);
        assertEquals(4, events.size());

        assertEquals("1", events.get(0).getColorId());
        assertEquals("4", events.get(1).getColorId());
        assertEquals("5", events.get(2).getColorId());
        assertEquals("6", events.get(3).getColorId());

        assertEquals("event from main calendar 1", events.get(0).getSummary());
        assertEquals("event from main calendar 2", events.get(1).getSummary());
        assertEquals("event from cancelled calendar 1", events.get(2).getSummary());
        assertEquals("event from cancelled calendar 2", events.get(3).getSummary());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), events.get(0).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), events.get(0).getEnd());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), events.get(1).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), events.get(1).getEnd());

        assertEquals(LocalDateTime.of(2023, 12, 01, 00, 00), events.get(2).getStart());
        assertEquals(LocalDateTime.of(2023, 12, 01, 01, 00), events.get(2).getEnd());

        assertEquals(LocalDateTime.of(2023, 12, 01, 00, 00), events.get(3).getStart());
        assertEquals(LocalDateTime.of(2023, 12, 01, 01, 00), events.get(3).getEnd());
    }

    @Test
    public void getEventsBetweenDateWhenCancelledCalendarDisabled() {
        properties.setCancelledCalendarEnabled(false);
        LocalDateTime leftDate = LocalDateTime.of(2023, 01, 01, 00, 00);
        LocalDateTime rightDate = LocalDateTime.of(2024, 01, 01, 00, 00);

        List<ru.nesterov.dto.Event> events = googleCalendarService.getEventsBetweenDates(leftDate, rightDate);
        assertNotNull(events);
        assertEquals(2, events.size());

        assertEquals("1", events.get(0).getColorId());
        assertEquals("4", events.get(1).getColorId());

        assertEquals("event from main calendar 1", events.get(0).getSummary());
        assertEquals("event from main calendar 2", events.get(1).getSummary());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), events.get(0).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), events.get(0).getEnd());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), events.get(1).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), events.get(1).getEnd());
    }
}