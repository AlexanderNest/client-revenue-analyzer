package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.dto.EventDto;
import ru.nesterov.entity.BackupType;
import ru.nesterov.entity.Event;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.exception.EventBackupTimeoutException;
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
@ConditionalOnProperty("app.calendar.events.backup.enabled")
@RequiredArgsConstructor
@Slf4j
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
    @Transactional
    public void backupAllUsersEvents() {
        List<User> users = userRepository.findAllByIsEventsBackupEnabled(true);
        
        if (users.isEmpty()) {
            return;
        }
        
        if (!isAutomaticBackupRequired()) {
            return;
        }
        
        log.debug("Выполнено автоматическое резервное копирование записей для {} пользователей(я)", users.size());
        saveBackups(users, BackupType.AUTOMATIC);
    }
    
    @Transactional
    public int backupCurrentUserEvents(String username) throws EventBackupTimeoutException {
        User user = userRepository.findByUsername(username);
        
        checkManualBackupTimeout(user.getId());
        
        List<EventBackup> saved = saveBackups(List.of(user), BackupType.MANUAL);
        log.debug("{} выполнил резервное копирование записей", username);
        return saved.get(0).getEvents().size();
    }
    
    private boolean isAutomaticBackupRequired() {
        CronExpression cronExpression = CronExpression.parse(eventsBackupProperties.getBackupTime());
        LocalDateTime nextExecution = cronExpression.next(LocalDateTime.now());
        LocalDateTime nextToNextExecution = cronExpression.next(nextExecution);
        Duration durationBetweenExecutions = Duration.between(nextExecution, nextToNextExecution);
        
        return !eventsBackupRepository.existsByTypeAndBackupTimeAfter(
                BackupType.AUTOMATIC,
                LocalDateTime.now().minusDays(durationBetweenExecutions.toDays())
        );
    }
    
    private void checkManualBackupTimeout(long userId) throws EventBackupTimeoutException {
        LocalDateTime checkedTime = LocalDateTime
                .now()
                .minusMinutes(eventsBackupProperties.getDelayBetweenManualBackups());
        
        EventBackup lastBackup = eventsBackupRepository
                .findByTypeAndUserIdAndBackupTimeAfter(BackupType.MANUAL, userId, checkedTime);
        
        if (lastBackup != null) {
            Duration durationBetweenBackups = Duration.between(lastBackup.getBackupTime(), LocalDateTime.now());
            long timeoutForNextBackup = eventsBackupProperties.getDelayBetweenManualBackups() - durationBetweenBackups.toMinutes();
            throw new EventBackupTimeoutException(timeoutForNextBackup);
        }
    }
    
    private List<EventBackup> saveBackups(List<User> users, BackupType type) {
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
            backup.setType(type);
            
            backups.add(backup);
        });
        
        return eventsBackupRepository.saveAll(backups);
    }
}
