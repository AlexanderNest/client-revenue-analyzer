package ru.nesterov;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "app.calendar")
@Component
public class CalendarProperties {
    private String applicationName;
    private String serviceAccountFileName;
    private String calendarId;
}
