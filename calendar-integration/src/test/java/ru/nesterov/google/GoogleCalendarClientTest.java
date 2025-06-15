package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.dto.EventDto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = {
        GoogleCalendarClient.class,
        GoogleCalendarProperties.class,
        ObjectMapper.class,
        EventStatusServiceImpl.class
})
public class GoogleCalendarClientTest {
    @MockBean
    private Calendar calendar;

    @SpyBean
    private GoogleCalendarClient spyClient;

    @Test
    public void testGetEventsBetweenDates_withPaging() throws Exception {

        Events firstPage = mock(Events.class);
        Events secondPage = mock(Events.class);

        when(firstPage.getNextPageToken()).thenReturn("token");
        when(firstPage.getItems()).thenReturn(List.of());

        when(secondPage.getNextPageToken()).thenReturn(null);
        when(secondPage.getItems()).thenReturn(List.of());

        doReturn(firstPage).when(spyClient).getEventsBetweenDates(anyString(), any(Date.class), any(Date.class), eq(null));  // оно??
        doReturn(secondPage).when(spyClient).getEventsBetweenDates(anyString(), any(Date.class), any(Date.class), eq("token"));

        List<EventDto> result = spyClient.getEventsBetweenDates("calendarId", CalendarType.PLAIN, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertEquals(0, result.size());
    }
}
