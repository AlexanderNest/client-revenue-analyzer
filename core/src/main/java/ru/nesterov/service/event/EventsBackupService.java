package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.EventDto;
import ru.nesterov.entity.Event;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.EventsBackupRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.mapper.EventMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty("app.calendar.events.backup.enable")
@RequiredArgsConstructor
public class EventsBackupService {
    private final UserRepository userRepository;
    private final GoogleCalendarService googleCalendarService;
    private final EventsBackupProperties eventsBackupProperties;
    private final EventsBackupRepository eventsBackupRepository;
    
    @Schedules({
            @Scheduled(
                    initialDelayString = "#{eventsBackupProperties.delayForBackupAfterAppStarting}",
                    timeUnit = TimeUnit.MINUTES
            ),
            @Scheduled(cron = "#{eventsBackupProperties.backupTime}")
    })
    public void backupAllUsersEvents() {
        List<User> users = userRepository.findAllByIsEventsBackupEnabled(true);
        
        if (users.isEmpty()) {
            return;
        }
        
        List<Long> userIds = new ArrayList<>();
        users.forEach(user -> userIds.add(user.getId()));
        
        if (!isTimeForAutomaticBackup(userIds)) {
            return;
        }
        
        List<EventBackup> backups = getBackups(users);
        eventsBackupRepository.saveAll(backups);
    }
    
    public int backupCurrentUserEvents(String username) {
        User user = userRepository.findByUsername(username);
        
        if (!isTimeForManualBackup(user.getId())) {
            return 0;
        }
        
        List<EventBackup> backup = getBackups(List.of(user));
        EventBackup saved = eventsBackupRepository.save(backup.get(0));
        return saved.getEvents().size();
    }
    
    private boolean isTimeForAutomaticBackup(List<Long> userIds) {
        if (userIds.size() > eventsBackupRepository.countAllByUserIdIn(userIds)) {
            return true;
        }
        
        CronExpression cronExpression = CronExpression.parse(eventsBackupProperties.getBackupTime());
        LocalDateTime nextExecution = cronExpression.next(LocalDateTime.now());
        LocalDateTime nextToNextExecution = cronExpression.next(nextExecution);
        Duration durationBetweenExecutions = Duration.between(nextExecution, nextToNextExecution);
        
        return eventsBackupRepository.existsByUserIdInAndBackupTimeBefore(
                userIds,
                LocalDateTime
                        .now()
                        .minusDays(durationBetweenExecutions.toDays())
        );
    }
    
    private boolean isTimeForManualBackup(long userId) {
        return !eventsBackupRepository.existsByUserIdAndBackupTimeAfter(
                userId,
                LocalDateTime.now().minusHours(eventsBackupProperties.getDelayBetweenManualBackups())
        );
    }
    
    private List<EventBackup> getBackups(List<User> users) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime backupStartDate = currentDateTime.minusDays(eventsBackupProperties.getDatesRangeForBackup());
        LocalDateTime backupEndDate = currentDateTime.plusDays(eventsBackupProperties.getDatesRangeForBackup());
        
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
