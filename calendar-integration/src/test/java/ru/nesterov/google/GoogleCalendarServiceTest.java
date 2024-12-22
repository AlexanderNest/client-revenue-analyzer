package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        GoogleCalendarService.class,
        ObjectMapper.class,
        EventStatusServiceImpl.class
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
        Event event = buildEvent("10", "event from main calendar 1", 2024, 1, 1, 0, 0, 2024, 1, 1, 1, 0);
        Event event2 = buildEvent("10", "event from main calendar 2", 2024, 1, 1, 0, 0, 2024, 1, 1, 1, 0);

        when(googleCalendarClient
                .getEventsBetweenDates(eq(MAIN_CALENDAR_ID), eq(false), any(), any(), any()))
                .thenReturn(List.of(event, event2));

        Event event3 = buildEvent("11", "event from cancelled calendar 1", 2023, 12, 1, 0, 0, 2023, 12, 1, 1, 0);
        Event event4 = buildEvent("11", "event from cancelled calendar 2", 2023, 12, 1, 0, 0, 2023, 12, 1,1, 0);

        when(googleCalendarClient
                .getEventsBetweenDates(eq(CANCELLED_CALENDAR_ID), eq(true), any(), any(), any()))
                .thenReturn(List.of(event3, event4)
                );

    }

    @Test
    public void transferCancelledEventsToCancelledCalendar() {
        //fail("NOT IMPLEMENTED");
    }

    @Test
    public void getEventsBetweenDateWhenCancelledCalendarEnabled() {
        LocalDateTime leftDate = LocalDateTime.of(2023, 01, 01, 00, 00);
        LocalDateTime rightDate = LocalDateTime.of(2024, 01, 01, 00, 00);

        List<EventDto> eventDtos = googleCalendarService.getEventsBetweenDates(MAIN_CALENDAR_ID, CANCELLED_CALENDAR_ID, true, leftDate, rightDate);
        assertNotNull(eventDtos);
        assertEquals(4, eventDtos.size());

        assertEquals(EventStatus.SUCCESS, eventDtos.get(0).getStatus());
        assertEquals(EventStatus.SUCCESS, eventDtos.get(1).getStatus());
        assertEquals(EventStatus.CANCELLED, eventDtos.get(2).getStatus());
        assertEquals(EventStatus.CANCELLED, eventDtos.get(3).getStatus());

        assertEquals("event from main calendar 1", eventDtos.get(0).getSummary());
        assertEquals("event from main calendar 2", eventDtos.get(1).getSummary());
        assertEquals("event from cancelled calendar 1", eventDtos.get(2).getSummary());
        assertEquals("event from cancelled calendar 2", eventDtos.get(3).getSummary());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), eventDtos.get(0).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), eventDtos.get(0).getEnd());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), eventDtos.get(1).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), eventDtos.get(1).getEnd());

        assertEquals(LocalDateTime.of(2023, 12, 01, 00, 00), eventDtos.get(2).getStart());
        assertEquals(LocalDateTime.of(2023, 12, 01, 01, 00), eventDtos.get(2).getEnd());

        assertEquals(LocalDateTime.of(2023, 12, 01, 00, 00), eventDtos.get(3).getStart());
        assertEquals(LocalDateTime.of(2023, 12, 01, 01, 00), eventDtos.get(3).getEnd());
    }

    @Test
    public void getEventsBetweenDateWhenCancelledCalendarDisabled() {
        LocalDateTime leftDate = LocalDateTime.of(2023, 01, 01, 00, 00);
        LocalDateTime rightDate = LocalDateTime.of(2024, 01, 01, 00, 00);

        List<EventDto> eventDtos = googleCalendarService.getEventsBetweenDates(MAIN_CALENDAR_ID, CANCELLED_CALENDAR_ID, false, leftDate, rightDate);
        assertNotNull(eventDtos);
        assertEquals(2, eventDtos.size());

        assertEquals(EventStatus.SUCCESS, eventDtos.get(0).getStatus());
        assertEquals(EventStatus.SUCCESS, eventDtos.get(1).getStatus());

        assertEquals("event from main calendar 1", eventDtos.get(0).getSummary());
        assertEquals("event from main calendar 2", eventDtos.get(1).getSummary());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), eventDtos.get(0).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), eventDtos.get(0).getEnd());

        assertEquals(LocalDateTime.of(2024, 01, 01, 00, 00), eventDtos.get(1).getStart());
        assertEquals(LocalDateTime.of(2024, 01, 01, 01, 00), eventDtos.get(1).getEnd());
    }

    private Event buildEvent(String color, String summary, int startY, int startM, int startD, int startH, int startMin,
                             int endY, int endM, int endD, int endH, int endMin) {
        Event event = new Event();
        event.setColorId(color);
        event.setSummary(summary);
        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(java.util.Date.from(
                        LocalDateTime.of(startY, startM, startD, startH, startMin)
                                .atZone(ZoneId.systemDefault())
                                .toInstant())));

        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(java.util.Date.from(
                        LocalDateTime.of(endY, endM, endD, endH, endMin)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()))
                );

        event.setEnd(end);

        return event;
    }
}