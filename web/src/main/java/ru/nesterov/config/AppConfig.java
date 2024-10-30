package ru.nesterov.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.nesterov.controller.EventsBackupController;
import ru.nesterov.controller.EventsBackupControllerImpl;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.EventBackupRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.event.EventBackupProperties;
import ru.nesterov.service.event.EventsBackupService;

@Configuration
@EnableScheduling
public class AppConfig {
    @Bean
    @ConditionalOnProperty(name = "backup.enable")
    public EventsBackupService eventsBackupService(UserRepository userRepository,
                                                   GoogleCalendarService googleCalendarService,
                                                   EventBackupProperties eventBackupProperties,
                                                   EventBackupRepository eventBackupRepository) {
        return new EventsBackupService(userRepository, googleCalendarService, eventBackupProperties, eventBackupRepository);
    }
}
