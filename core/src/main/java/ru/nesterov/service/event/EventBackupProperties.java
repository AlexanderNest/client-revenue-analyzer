package ru.nesterov.service.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app.calendar.events.backup")
@Data
public class EventBackupProperties {
    private String delayForBackupAfterAppStarting;
    private String backupTime;
    private int delayBetweenAutomaticBackups;
    private int delayBetweenManualBackups;
    private int datesRangeForBackup;
}
