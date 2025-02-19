package ru.nesterov.controller;

import com.google.api.services.calendar.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
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

        Event event1 = buildEvent("10", "event1", "", 2024, 10, 11, 10, 10, 2024, 10, 11, 10, 40);
        Event event2 = buildEvent("10", "event2", "", 2024, 11, 11, 10, 10, 2024, 11, 11, 10, 40);

        when(googleCalendarClient.getEventsBetweenDates(anyString(), anyBoolean(), any(), any(), any()))
                .thenReturn(List.of(event1, event2));
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
