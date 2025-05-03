package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.Calendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nesterov.google.exception.EventsBetweenDatesException;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class GoogleCalendarClientTest {

    Calendar calendar;

    GoogleCalendarClient googleCalendarClient;

    @BeforeEach
    void setUp() {

        calendar = mock(Calendar.class);

        googleCalendarClient = new GoogleCalendarClient(
                mock(GoogleCalendarProperties.class),
                mock(ObjectMapper.class),
                mock(EventStatusService.class)) {

            @Override
            public Calendar createCalendarService() {
                return calendar;
            }
        };
    }

    @Test
    void moveEventsToOtherCalendar_throwsEventsBetweenDatesException() throws IOException {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime oneMonthForward = now.plusMonths(1);

        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.List list = mock(Calendar.Events.List.class);

        when(calendar.events()).thenReturn(events);
        when(events.list(anyString())).thenReturn(list);
        when(list.setTimeMin(any())).thenReturn(list);
        when(list.setTimeMax(any())).thenReturn(list);
        when(list.setOrderBy(anyString())).thenReturn(list);
        when(list.setSingleEvents(anyBoolean())).thenReturn(list);
        when(list.setPageToken(nullable(String.class))).thenReturn(list);
        when(list.execute()).thenThrow(IOException.class);

        assertThrows(EventsBetweenDatesException.class, () -> googleCalendarClient.moveEventsToOtherCalendar(
                "sourceCalendarId",
                "targetCalendarId",
                oneMonthAgo,
                oneMonthForward
        ));
    }
}