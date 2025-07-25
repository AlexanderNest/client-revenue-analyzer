package ru.nesterov.calendar.integration.google;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("app.google.calendar")
@ConditionalOnProperty("app.google.calendar.integration.enabled")
public class GoogleCalendarProperties {
    private String applicationName;
    private String serviceAccountFilePath;
}
