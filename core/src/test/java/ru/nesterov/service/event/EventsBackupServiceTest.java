package ru.nesterov.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.EventsBackupRepository;
import ru.nesterov.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        EventsBackupService.class,
        EventsBackupProperties.class
})
public class EventsBackupServiceTest {
    @Autowired
    private EventsBackupService eventsBackupService;
    
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GoogleCalendarService googleCalendarService;
    @MockBean
    private EventsBackupRepository eventsBackupRepository;
    
    @BeforeEach
    public void init() {
        User user = new User();
        user.setId(1);
        user.setUsername("testUsername");
        user.setEventsBackupEnabled(true);
        user.setCancelledCalendar("cancelledCalendar");
        user.setMainCalendar("mainCalendar");
        when(userRepository.findByUsername("testUsername")).thenReturn(user);
        
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        EventDto eventDto1 = EventDto.builder()
                .summary("event1")
                .status(EventStatus.SUCCESS)
                .start(start.minusDays(10))
                .end(end.minusDays(10))
                .build();
        
        EventDto eventDto2 = EventDto.builder()
                .summary("event2")
                .status(EventStatus.SUCCESS)
                .start(start.minusDays(5))
                .end(end.minusDays(5))
                .build();
        
        when(googleCalendarService.getEventsBetweenDates(any(), any(), anyBoolean(), any(), any()))
                .thenReturn(List.of(eventDto1, eventDto2));
        
        when(eventsBackupRepository)
    }
    
    @Test
    public void backupCurrentUserEvents() {
        int result = eventsBackupService.backupCurrentUserEvents("testUsername");
        assertEquals(6, result);
    }
}
