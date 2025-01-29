package ru.nesterov.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.BackupType;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.repository.EventsBackupRepository;
import ru.nesterov.service.event.EventsBackupProperties;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = ("app.calendar.events.backup.enabled=true"))
public class EventsBackupControllerTest extends AbstractControllerTest {
    @Autowired
    private EventsBackupProperties eventsBackupProperties;
    @Autowired
    private EventsBackupRepository eventsBackupRepository;

    private static final String URL = "/events/backup";
    private static final String HEADER_X_USERNAME = "X-username";
    
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
        
        when(googleCalendarClient.getEventsBetweenDates(anyString(), CalendarType.PLAIN, any(), any()))
                .thenReturn(List.of(eventDto1, eventDto2));
    }
    
    @Test
    public void makeSuccessfulBackup() throws Exception {
        User user = createUser(1);
        
        mockMvc.perform(get(URL)
                        .header(HEADER_X_USERNAME, user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Встреч сохранено: 2"));
        
        User savedUser = userRepository.findByUsername(user.getUsername());
        LocalDateTime checkedTime = LocalDateTime
                .now().minusMinutes(eventsBackupProperties.getDelayBetweenManualBackups() - 1);
        EventBackup savedBackup = eventsBackupRepository
                .findByTypeAndUserIdAndBackupTimeAfter(BackupType.MANUAL, savedUser.getId(), checkedTime);
        
        assertEquals(2, savedBackup.getEvents().size());
    }
    
    @Test
    public void checkDelayBetweenNextBackup() throws Exception {
        User user = createUser(2);
        
        mockMvc.perform(get(URL)
                        .header(HEADER_X_USERNAME, user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Встреч сохранено: 2"));
        
        User savedUser = userRepository.findByUsername(user.getUsername());
        LocalDateTime checkedTime = LocalDateTime
                .now().minusMinutes(eventsBackupProperties.getDelayBetweenManualBackups() - 1);
        EventBackup savedBackup = eventsBackupRepository
                .findByTypeAndUserIdAndBackupTimeAfter(BackupType.MANUAL, savedUser.getId(), checkedTime);
        
        mockMvc.perform(get(URL)
                        .header(HEADER_X_USERNAME, user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Следующий бэкап можно будет сделать по прошествии " +
                        eventsBackupProperties.getDelayBetweenManualBackups() + " минут(ы)"));
        
        EventBackup lastSavedBackup = eventsBackupRepository
                .findByTypeAndUserIdAndBackupTimeAfter(BackupType.MANUAL, savedUser.getId(), checkedTime);
        
        assertEquals(savedBackup.getBackupTime(), lastSavedBackup.getBackupTime());
    }
    
    private User createUser(int suffix) {
        User user = new User();
        user.setUsername("backupTestUsername" + suffix);
        user.setMainCalendar("testCalendar" + suffix);
        user.setEventsBackupEnabled(true);
        return userRepository.save(user);
    }
}
