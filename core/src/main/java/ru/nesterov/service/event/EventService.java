package ru.nesterov.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventExtension;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.EventBackup;
import ru.nesterov.entity.User;
import ru.nesterov.exception.AppException;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.EventBackupRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.dto.UserDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EventService {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final GoogleCalendarService googleCalendarService;
    private final int backupRange;
    private final int backupFrequency;
    private final boolean backupStartingApp;
    private final EventBackupRepository eventBackupRepository;
    
    public EventService(
            ClientRepository clientRepository,
            UserRepository userRepository,
            GoogleCalendarService googleCalendarService,
            @Value("${app.calendar.events.backup.range}") int backupRange,
            @Value("${app.calendar.events.backup.frequency}") int backupFrequency,
            @Value("${app.calendar.events.backup.starting-app}") boolean backupStartingApp,
            EventBackupRepository eventBackupRepository
    ) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.googleCalendarService = googleCalendarService;
        this.backupRange = backupRange;
        this.backupFrequency = backupFrequency;
        this.backupStartingApp = backupStartingApp;
        this.eventBackupRepository = eventBackupRepository;
    }
    
    public double getEventIncome(UserDto userDto, Event event) {
        Client client = clientRepository.findClientByNameAndUserId(event.getSummary(), userDto.getId());
        if (client == null) {
            throw new AppException("Пользователь с именем '" + event.getSummary() + "' от даты " + event.getStart() + " не найден в базе");
        }
        EventExtension extension = event.getEventExtension();
        if (extension != null && extension.getIncome() != null) {
            return extension.getIncome();
        }
        return getEventDuration(event) * client.getPricePerHour();
    }
    
    public double getEventDuration(Event event) {
        Duration duration = Duration.between(event.getStart(), event.getEnd());
        return duration.toMinutes() / 60.0;
    }
    
    public void backup(UserDto userDto) {
        User user = userRepository.findByUsername(userDto.getUsername());
        
        if (!checkTimeForBackup(user.getId())) {
            return;
        }
        
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime startDate = currentDate.minusDays(backupRange);
        LocalDateTime endDate = currentDate.plusDays(backupRange);
        
        List<Event> events = googleCalendarService.getEventsBetweenDates(
                userDto.getMainCalendar(),
                userDto.getCancelledCalendar(),
                userDto.isCancelledCalendarEnabled(),
                startDate,
                endDate
        );
        
        List<EventBackup> backup = toEventBackupList(events, user);
        eventBackupRepository.removeAllByUserId(user.getId());
        eventBackupRepository.saveAll(backup);
    }
    
    private List<EventBackup> toEventBackupList(List<Event> events, User user) {
        return events.stream()
                .map(event -> {
                    EventBackup eventBackup = new EventBackup();
                    eventBackup.setUser(user);
                    eventBackup.setStatus(event.getStatus());
                    eventBackup.setSummary(event.getSummary());
                    eventBackup.setStartDate(event.getStart());
                    eventBackup.setEndDate(event.getEnd());
                    
                    if (event.getEventExtension() != null) {
                        eventBackup.setComment(event.getEventExtension().getComment());
                        eventBackup.setIncome(event.getEventExtension().getIncome());
                    }
                    
                    return eventBackup;
                })
                .toList();
    }
    
    private boolean checkTimeForBackup(long userId) {
        EventBackup previousBackup = eventBackupRepository.findFirstByUserId(userId);
        
        if (previousBackup != null) {
            Duration timeAfterPreviousBackup = Duration.between(previousBackup.getBackupTime(), LocalDateTime.now());
            return timeAfterPreviousBackup.toDays() >= backupFrequency;
        }
        
        return true;
    }
}
