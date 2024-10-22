package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.dto.Event;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.EventBackupRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.UserService;
import ru.nesterov.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventsBackupService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final GoogleCalendarService googleCalendarService;
    private final EventBackupProperties eventBackupProperties;
    private final EventBackupRepository eventBackupRepository;
    
    @Scheduled(
            initialDelayString = "#{eventBackupProperties.automaticInitialDelay}",
            fixedRateString = "#{eventBackupProperties.automaticFixedRate}"
    )
    public void automaticBackup() {
        List<User> users = userRepository.findAll();
        users.stream()
                .map(userService::convert)
                .forEach(this::manualBackup);
    }
    
    @Transactional
    public int manualBackup(UserDto userDto) {
        User user = userRepository.findByUsername(userDto.getUsername());
        
        if (!checkTimeForBackup(user.getId())) {
            return 0;
        }
        
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime backupStartDate = currentDate.minusDays(eventBackupProperties.getRange());
        LocalDateTime backupEndDate = currentDate.plusDays(eventBackupProperties.getRange());
        
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
        return !eventBackupRepository.existsByUserIdAndBackupTimeAfter(
                userId,
                LocalDateTime.now().minusHours(eventBackupProperties.getManualFixedRate())
        );
    }
}
