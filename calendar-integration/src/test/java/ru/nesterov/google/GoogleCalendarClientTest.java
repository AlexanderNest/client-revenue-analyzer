package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nesterov.common.dto.CalendarType;
import ru.nesterov.common.dto.EventDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoogleCalendarClientTest {
    @Mock
    Calendar calendar;
    @Mock
    GoogleCalendarProperties properties;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    EventStatusService eventStatusService;
    @InjectMocks
    GoogleCalendarClient googleCalendarClient;
    @Mock
    Calendar.Events events;
    @Mock
    Calendar.Events.List list;


    @Test
    void getEventsBetweenDates() throws IOException {
        String calendarId = "testId";
        Events firstPage = new Events();
        Events secondPage = new Events();

        Event event1 = new Event().setSummary("Event1");
        Event event2 = new Event().setSummary("Event2");
        Event event3 = new Event().setSummary("Event3");

        EventDateTime startOfEvent = new EventDateTime().setDateTime(new DateTime(System.currentTimeMillis()));
        EventDateTime endOfEvent = new EventDateTime().setDateTime(new DateTime(System.currentTimeMillis() + 3600000));
        event1.setStart(startOfEvent).setEnd(endOfEvent);
        event2.setStart(startOfEvent).setEnd(endOfEvent);
        event3.setStart(startOfEvent).setEnd(endOfEvent);

        List<Event> firstPageItems = new ArrayList<>();
        firstPageItems.add(event1);
        firstPageItems.add(event2);
        firstPage.setItems(firstPageItems);
        firstPage.setNextPageToken("test-token1");

        List<Event> secondPageItems = new ArrayList<>();
        secondPageItems.add(event3);
        secondPage.setItems(secondPageItems);
        secondPage.setNextPageToken(null);

        when(calendar.events()).thenReturn(events);
        when(events.list(calendarId)).thenReturn(list);
        when(list.setTimeMin(any())).thenReturn(list);
        when(list.setTimeMax(any())).thenReturn(list);
        when(list.setOrderBy(any())).thenReturn(list);
        when(list.setSingleEvents(true)).thenReturn(list);
        when(list.setPageToken(null)).thenReturn(list);
        when(list.setPageToken("test-token1")).thenReturn(list);
        when(list.execute()).thenReturn(firstPage).thenReturn(secondPage);

        List<EventDto> eventDtoList = googleCalendarClient.getEventsBetweenDates(calendarId,
                CalendarType.PLAIN,
                LocalDateTime.of(2025, 6, 3, 1, 1, 1, 1),
                LocalDateTime.of(2025, 6, 3, 1, 1, 1, 3));

        Assertions.assertEquals(3, eventDtoList.size());
    }
}
