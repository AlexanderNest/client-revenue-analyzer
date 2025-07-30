package ru.nesterov.core.service.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app.calendar.events.backup")
@Data
public class EventsBackupProperties {
    private String backupTime;
    private int delayForBackupAfterAppStarting;
    private int delayBetweenManualBackups;
    private int datesRangeForBackup;
    private String backupsCleaningSchedule;
    private int daysLimit;
}
