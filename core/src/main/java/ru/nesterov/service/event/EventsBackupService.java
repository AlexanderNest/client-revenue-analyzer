package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.EventDto;
import ru.nesterov.entity.Event;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.EventBackupRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.mapper.EventMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventsBackupService {
    private final UserRepository userRepository;
    private final GoogleCalendarService googleCalendarService;
    private final EventBackupProperties eventBackupProperties;
    private final EventBackupRepository eventBackupRepository;
    
    @Schedules({
            @Scheduled(initialDelayString = "#{eventBackupProperties.delayAfterAppStarting}"),
            @Scheduled(cron = "#{eventBackupProperties.time}")
    })
    public void backupAllUsersEvents() {
        if (!checkAutomaticBackupTime()) {
            return;
        }
        
        List<User> users = userRepository.findAllByIsEventsBackupEnabled(true);
        List<EventBackup> backups = getBackups(users, false);
        eventBackupRepository.saveAll(backups);
    }
    
    public int backupCurrentUserEvents(String username) {
        User user = userRepository.findByUsername(username);
        
        if (!checkManualBackupTime(user.getId())) {
            return 0;
        }
        
        List<EventBackup> backup = getBackups(List.of(user), true);
        EventBackup saved = eventBackupRepository.save(backup.get(0));
        return saved.getEvents().size();
    }
    
    private boolean checkAutomaticBackupTime() {
        return !eventBackupRepository.existsByAutomaticBackupTimeAfter(
                LocalDateTime
                        .now()
                        .minusDays(eventBackupProperties.getDelayAfterAutomaticBackup())
        );
    }
    
    private boolean checkManualBackupTime(long userId) {
        return !eventBackupRepository.existsByUserIdAndManualBackupTimeAfter(
                userId,
                LocalDateTime.now().minusHours(eventBackupProperties.getDelayAfterManualBackup())
        );
    }
    
    private List<EventBackup> getBackups(List<User> users, boolean isManual) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime backupStartDate = currentDateTime.minusDays(eventBackupProperties.getDatesRange());
        LocalDateTime backupEndDate = currentDateTime.plusDays(eventBackupProperties.getDatesRange());
        
        List<EventBackup> backups = new ArrayList<>();
        
        users.forEach(user -> {
            List<EventDto> eventDtos = googleCalendarService.getEventsBetweenDates(
                    user.getMainCalendar(),
                    user.getCancelledCalendar(),
                    user.isCancelledCalendarEnabled(),
                    backupStartDate,
                    backupEndDate
            );
            
            List<Event> eventsToBackup = eventDtos.stream()
                    .map(EventMapper::mapToEvent)
                    .toList();
            
            EventBackup backup = new EventBackup();
            backup.setUser(user);
            backup.setEvents(eventsToBackup);
            
            if (isManual) {
                backup.setManualBackupTime(currentDateTime);
            } else {
                backup.setAutomaticBackupTime(currentDateTime);
            }
            
            backups.add(backup);
        });
        
        return backups;
    }
}
