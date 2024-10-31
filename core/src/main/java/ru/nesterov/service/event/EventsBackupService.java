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
            @Scheduled(initialDelayString = "#{eventBackupProperties.delayForBackupAfterAppStarting}"),
            @Scheduled(cron = "#{eventBackupProperties.backupTime}")
    })
    public void backupAllUsersEvents() {
        List<User> users = userRepository.findAllByIsEventsBackupEnabled(true);
        
        List<Long> userIds = new ArrayList<>();
        users.forEach(user -> userIds.add(user.getId()));
        
        if (!isTimeForAutomaticBackup(userIds)) {
            return;
        }
        
        List<EventBackup> backups = getBackups(users);
        eventBackupRepository.saveAll(backups);
    }
    
    public int backupCurrentUserEvents(String username) {
        User user = userRepository.findByUsername(username);
        
        if (!isTimeForManualBackup(user.getId())) {
            return 0;
        }
        
        List<EventBackup> backup = getBackups(List.of(user));
        EventBackup saved = eventBackupRepository.save(backup.get(0));
        return saved.getEvents().size();
    }
    
    private boolean isTimeForAutomaticBackup(List<Long> userIds) {
        return eventBackupRepository.existsByUserIdInAndBackupTimeBefore(
                userIds,
                LocalDateTime
                        .now()
                        .minusDays(eventBackupProperties.getDelayBetweenAutomaticBackups())
        );
    }
    
    private boolean isTimeForManualBackup(long userId) {
        return !eventBackupRepository.existsByUserIdAndBackupTimeAfter(
                userId,
                LocalDateTime.now().minusHours(eventBackupProperties.getDelayBetweenManualBackups())
        );
    }
    
    private List<EventBackup> getBackups(List<User> users) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime backupStartDate = currentDateTime.minusDays(eventBackupProperties.getDatesRangeForBackup());
        LocalDateTime backupEndDate = currentDateTime.plusDays(eventBackupProperties.getDatesRangeForBackup());
        
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
            
            backups.add(backup);
        });
        
        return backups;
    }
}
