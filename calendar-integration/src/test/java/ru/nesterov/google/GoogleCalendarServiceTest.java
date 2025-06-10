package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.common.dto.CalendarServiceDto;
import ru.nesterov.common.dto.CalendarType;
import ru.nesterov.common.dto.EventDto;
import ru.nesterov.common.dto.EventStatus;

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
                .getEventsBetweenDates(eq(MAIN_CALENDAR_ID), eq(CalendarType.MAIN), any(), any()))
                .thenReturn(List.of(
                        EventDto.builder()
                                .status(EventStatus.SUCCESS)
                                .summary("event from main calendar 1")
                                .start(LocalDateTime.of(2024, 01, 01, 00, 00))
                                .end(LocalDateTime.of(2024, 01, 01, 01, 00))
                                .build(),
                        EventDto.builder()
                                .status(EventStatus.SUCCESS)
                                .summary("event from main calendar 2")
                                .start(LocalDateTime.of(2024, 01, 01, 00, 00))
                                .end(LocalDateTime.of(2024, 01, 01, 01, 00))
                                .build()
                        )
                );

        when(googleCalendarClient
                .getEventsBetweenDates(eq(CANCELLED_CALENDAR_ID), eq(CalendarType.CANCELLED), any(), any()))
                .thenReturn(List.of(
                                EventDto.builder()
                                        .status(EventStatus.CANCELLED)
                                        .summary("event from cancelled calendar 1")
                                        .start(LocalDateTime.of(2023, 12, 01, 00, 00))
                                        .end(LocalDateTime.of(2023, 12, 01, 01, 00))
                                        .build(),
                                EventDto.builder()
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

        CalendarServiceDto calendarServiceDto = CalendarServiceDto.builder()
                .mainCalendar(MAIN_CALENDAR_ID)
                .cancelledCalendar(CANCELLED_CALENDAR_ID)
                .leftDate(leftDate)
                .rightDate(rightDate)
                .isCancelledCalendarEnabled(true)
                .build();

        List<EventDto> eventDtos = googleCalendarService.getEventsBetweenDates(calendarServiceDto);
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

        CalendarServiceDto calendarServiceDto = CalendarServiceDto.builder()
                .mainCalendar(MAIN_CALENDAR_ID)
                .cancelledCalendar(null)
                .leftDate(leftDate)
                .rightDate(rightDate)
                .isCancelledCalendarEnabled(false)
                .build();

        List<EventDto> eventDtos = googleCalendarService.getEventsBetweenDates(calendarServiceDto);
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
}