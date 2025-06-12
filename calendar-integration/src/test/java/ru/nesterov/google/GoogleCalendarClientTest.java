package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.dto.EventDto;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleCalendarClientTest {

    @Mock
    private GoogleCalendarProperties googleCalendarProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventStatusService eventStatusService;

    private GoogleCalendarClient spyClient;

    @BeforeEach
    void setUp() {
        when(googleCalendarProperties.getApplicationName()).thenReturn("client-revenue-analyzer");
        when(googleCalendarProperties.getServiceAccountFilePath()).thenReturn("src/test/resources/test-service-account.json");
        Calendar mockCalendar = mock(Calendar.class);
        spyClient = spy(new GoogleCalendarClient(googleCalendarProperties, objectMapper, eventStatusService));
        doReturn(mockCalendar).when(spyClient).createCalendarService();
    }

    @Test
    public void testGetEventsBetweenDates_withPaging() throws Exception {
        Events firstPage = mock(Events.class);
        Events secondPage = mock(Events.class);

        when(firstPage.getNextPageToken()).thenReturn("token");
        when(firstPage.getItems()).thenReturn(Collections.emptyList());

        when(secondPage.getNextPageToken()).thenReturn(null);
        when(secondPage.getItems()).thenReturn(Collections.emptyList());

        doReturn(firstPage).when(spyClient).getEventsBetweenDates(anyString(), any(Date.class), any(Date.class), eq(null));

        doReturn(secondPage).when(spyClient).getEventsBetweenDates(anyString(), any(Date.class), any(Date.class), eq("token"));

        List<EventDto> result = spyClient.getEventsBetweenDates("calendarId", CalendarType.PLAIN, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertEquals(0, result.size());
    }
}