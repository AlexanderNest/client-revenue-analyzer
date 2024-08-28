package ru.nesterov.google;

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
    private String mainCalendarId;
    private String cancelledCalendarId;
    private Boolean cancelledCalendarEnabled;
}
