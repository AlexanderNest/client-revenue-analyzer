package ru.nesterov.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.google.CalendarClient;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.EventsBackupRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.event.EventsBackupProperties;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"app.calendar.events.backup.enabled=true"})
@AutoConfigureMockMvc
public class EventsBackupControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventsBackupProperties eventsBackupProperties;
    @Autowired
    private EventsBackupRepository eventsBackupRepository;
    
    @MockBean
    private GoogleCalendarService googleCalendarService;
    @MockBean
    private CalendarClient calendarClient;
    
    private static final String URL = "/events/backup";
    private static final String HEADER = "X-username";
    
    @BeforeEach
    public void init() {
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
    }
    
    @Test
    public void makeSuccessfulBackup() throws Exception {
        User user1 = new User();
        user1.setUsername("backupTestUsername1");
        user1.setMainCalendar("testCalendar1");
        user1.setEventsBackupEnabled(true);
        userRepository.save(user1);
        
        mockMvc.perform(get(URL)
                        .header(HEADER, user1.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Встреч сохранено: 2"));
        
        User savedUser = userRepository.findByUsername(user1.getUsername());
        LocalDateTime checkedTime = LocalDateTime
                .now().minusMinutes(eventsBackupProperties.getDelayBetweenManualBackups() - 1);
        EventBackup savedBackup = eventsBackupRepository
                .findByIsManualIsTrueAndUserIdAndBackupTimeAfter(savedUser.getId(), checkedTime);
        
        assertEquals(2, savedBackup.getEvents().size());
    }
    
    @Test
    public void checkDelayBetweenNextBackup() throws Exception {
        User user2 = new User();
        user2.setUsername("backupTestUsername2");
        user2.setMainCalendar("testCalendar2");
        user2.setEventsBackupEnabled(true);
        userRepository.save(user2);
        
        mockMvc.perform(get(URL)
                        .header(HEADER, user2.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Встреч сохранено: 2"));
        
        User savedUser = userRepository.findByUsername(user2.getUsername());
        LocalDateTime checkedTime = LocalDateTime
                .now().minusMinutes(eventsBackupProperties.getDelayBetweenManualBackups() - 1);
        EventBackup savedBackup = eventsBackupRepository
                .findByIsManualIsTrueAndUserIdAndBackupTimeAfter(savedUser.getId(), checkedTime);
        
        mockMvc.perform(get(URL)
                        .header(HEADER, user2.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Следующий бэкап можно будет сделать по прошествии " +
                        eventsBackupProperties.getDelayBetweenManualBackups() + " минут(ы)"));
        
        EventBackup lastSavedBackup = eventsBackupRepository
                .findByIsManualIsTrueAndUserIdAndBackupTimeAfter(savedUser.getId(), checkedTime);
        
        assertEquals(savedBackup.getBackupTime(), lastSavedBackup.getBackupTime());
    }
}
