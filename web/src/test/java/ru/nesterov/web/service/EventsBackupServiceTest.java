package ru.nesterov.web.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.nesterov.calendar.integration.google.GoogleCalendarClient;
import ru.nesterov.core.entity.EventBackup;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.EventsBackupRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.dto.EventBackupDto;
import ru.nesterov.core.service.event.EventsBackupService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = ("app.calendar.events.backup.enabled=true"))
public class EventsBackupServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventsBackupRepository eventsBackupRepository;
    @Autowired
    private EventsBackupService eventsBackupService;
    @MockBean
    private GoogleCalendarClient googleCalendarClient;

    private User createUser(int suffix) {
        User user = new User();
        user.setUsername("backupTestUsername" + suffix);
        user.setMainCalendar("testCalendar" + suffix);
        user.setEventsBackupEnabled(true);
        return userRepository.save(user);
    }

    @Test
    void deleteOldBackupsTest() {
        User user = createUser(3);

        EventBackupDto eventBackupDto = eventsBackupService.backupCurrentUserEvents(user.getUsername());
        EventBackup eventBackup = eventsBackupRepository.findById(eventBackupDto.getBackupId()).orElseThrow();
        eventBackup.setBackupTime(LocalDateTime.now().minusDays(35));
        eventsBackupRepository.save(eventBackup);

        assertEquals(1, eventsBackupService.deleteOldBackups());
    }
}
