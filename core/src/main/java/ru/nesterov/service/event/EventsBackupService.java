package ru.nesterov.service.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.dto.Event;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.EventBackupRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventsBackupService {
    private final UserRepository userRepository;
    private final GoogleCalendarService googleCalendarService;
    private final int backupRange;
    private final int manualBackupFrequency;
    private final boolean backupStartingApp;
    private final EventBackupRepository eventBackupRepository;
    
    public EventsBackupService(
            UserRepository userRepository,
            GoogleCalendarService googleCalendarService,
            @Value("${app.calendar.events.backup.range}") int backupRange,
            @Value("${app.calendar.events.backup.frequency.automatic}") int manualBackupFrequency,
            @Value("${app.calendar.events.backup.starting-app}") boolean backupStartingApp,
            EventBackupRepository eventBackupRepository
    ) {
        this.userRepository = userRepository;
        this.googleCalendarService = googleCalendarService;
        this.backupRange = backupRange;
        this.manualBackupFrequency = manualBackupFrequency;
        this.backupStartingApp = backupStartingApp;
        this.eventBackupRepository = eventBackupRepository;
    }
    
    //    @Scheduled
    public void automaticBackup() {
        List<User> users = userRepository.findAll();
        
    }
    
    @Transactional
    public int manualBackup(UserDto userDto) {
        User user = userRepository.findByUsername(userDto.getUsername());
        
        if (!checkTimeForBackup(user.getId())) {
            return 0;
        }
        
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime backupStartDate = currentDate.minusDays(backupRange);
        LocalDateTime backupEndDate = currentDate.plusDays(backupRange);
        
        List<Event> eventsToBackup = googleCalendarService.getEventsBetweenDates(
                userDto.getMainCalendar(),
                userDto.getCancelledCalendar(),
                userDto.isCancelledCalendarEnabled(),
                backupStartDate,
                backupEndDate
        );
        
        EventBackup backup = new EventBackup();
        backup.setUser(user);
        backup.setEvents(eventsToBackup);
        eventBackupRepository.save(backup);
        return eventsToBackup.size();
    }
    
    private boolean checkTimeForBackup(long userId) {
        return !eventBackupRepository.existsByUserIdAndBackupTimeAfter(userId, LocalDateTime.now().minusHours(manualBackupFrequency));
    }
}
