package ru.nesterov.service.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app.calendar.events.backup")
@Data
public class EventBackupProperties {
    private String automaticInitialDelay;
    private String automaticFixedRate;
    private int manualFixedRate;
    private int range;
}
