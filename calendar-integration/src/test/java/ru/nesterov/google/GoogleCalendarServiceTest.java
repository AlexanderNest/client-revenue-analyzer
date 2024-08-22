package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        GoogleCalendarProperties.class,
        GoogleCalendarService.class,
        ObjectMapper.class
})
class GoogleCalendarServiceTest {
    @Autowired
    private GoogleCalendarService googleCalendarService;

//    @BeforeEach
//    public void init() throws IOException {
//        Event event1 = new Event();
//        event1.setSummary("from main calendar");
//        Date now = new Date();
//        EventDateTime eventDateTime1 = new EventDateTime();
//        eventDateTime1.setDateTime(new DateTime(now));
//        event1.setStart(eventDateTime1);
//
//        Event event2 = new Event();
//        event2.setSummary("from cancelled calendar");
//        EventDateTime eventDateTime2 = new EventDateTime();
//        eventDateTime2.setDateTime(new DateTime(new Date(now.getTime() - 3600)));
//        event2.setStart(eventDateTime2);
//
//        Events mainCalendarEvents = new Events().setItems(List.of(event1));
//        Events cancelledCalendarEvents = new Events().setItems(List.of(event2));
//
//        when(calendar.events().list("main-calendar-id")
//                .setTimeMin(any(DateTime.class))
//                .setTimeMax(any(DateTime.class))
//                .setOrderBy(anyString())
//                .setSingleEvents(anyBoolean())
//                .execute())
//                .thenReturn(mainCalendarEvents);
//
//        when(calendar.events().list("cancelled-calendar-id")
//                .setTimeMin(any(DateTime.class))
//                .setTimeMax(any(DateTime.class))
//                .setOrderBy(anyString())
//                .setSingleEvents(anyBoolean())
//                .execute())
//                .thenReturn(cancelledCalendarEvents);
//    }

    @Test
    public void getEventsBetweenDateWhenCancelledCalendarEnabled() {
        List<ru.nesterov.dto.Event> events = googleCalendarService.getEventsBetweenDates(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertNotNull(events);
        assertEquals(2, events.size());
        assertEquals("from main calendar", events.get(0).getSummary());
        assertEquals("from cancelled calendar", events.get(1).getSummary());
    }
}