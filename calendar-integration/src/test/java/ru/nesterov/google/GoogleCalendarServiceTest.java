package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventStatus;

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
class GoogleCalendarServiceTest {
    @Autowired
    private GoogleCalendarService googleCalendarService;
    @MockBean
    private GoogleCalendarClient googleCalendarClient;

    private static final String MAIN_CALENDAR_ID = "main_calendar_id";
    private static final String CANCELLED_CALENDAR_ID = "cancelled_calendar_id";

    @BeforeEach
    public void init() {
        when(googleCalendarClient
                .getEventsBetweenDates(eq(MAIN_CALENDAR_ID), eq(false), any(), any()))
                .thenReturn(List.of(
                        Event.builder()
                                .status(EventStatus.SUCCESS)
                                .summary("event from main calendar 1")
                                .start(LocalDateTime.of(2024, 01, 01, 00, 00))
                                .end(LocalDateTime.of(2024, 01, 01, 01, 00))
                                .build(),
                        Event.builder()
                                .status(EventStatus.SUCCESS)
                                .summary("event from main calendar 2")
                                .start(LocalDateTime.of(2024, 01, 01, 00, 00))
                                .end(LocalDateTime.of(2024, 01, 01, 01, 00))
                                .build()
                        )
                );

        when(googleCalendarClient
                .getEventsBetweenDates(eq(CANCELLED_CALENDAR_ID), eq(true), any(), any()))
                .thenReturn(List.of(
                                Event.builder()
                                        .status(EventStatus.CANCELLED)
                                        .summary("event from cancelled calendar 1")
                                        .start(LocalDateTime.of(2023, 12, 01, 00, 00))
                                        .end(LocalDateTime.of(2023, 12, 01, 01, 00))
                                        .build(),
                                Event.builder()
                                        .status(EventStatus.CANCELLED)
                                        .summary("event from cancelled calendar 2")
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

        List<ru.nesterov.dto.Event> events = googleCalendarService.getEventsBetweenDates(MAIN_CALENDAR_ID, CANCELLED_CALENDAR_ID, true, leftDate, rightDate);
        assertNotNull(events);
        assertEquals(4, events.size());

        assertEquals(EventStatus.SUCCESS, events.get(0).getStatus());
        assertEquals(EventStatus.SUCCESS, events.get(1).getStatus());
        assertEquals(EventStatus.CANCELLED, events.get(2).getStatus());
        assertEquals(EventStatus.CANCELLED, events.get(3).getStatus());

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
        LocalDateTime leftDate = LocalDateTime.of(2023, 01, 01, 00, 00);
        LocalDateTime rightDate = LocalDateTime.of(2024, 01, 01, 00, 00);

        List<ru.nesterov.dto.Event> events = googleCalendarService.getEventsBetweenDates(MAIN_CALENDAR_ID, CANCELLED_CALENDAR_ID, false, leftDate, rightDate);
        assertNotNull(events);
        assertEquals(2, events.size());

        assertEquals(EventStatus.SUCCESS, events.get(0).getStatus());
        assertEquals(EventStatus.SUCCESS, events.get(1).getStatus());

        assertEquals("event from main calendar 1", events.get(0).getSummary());
        assertEquals("event from main calendar 2", events.get(1).getSummary());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), events.get(0).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), events.get(0).getEnd());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), events.get(1).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), events.get(1).getEnd());
    }
}