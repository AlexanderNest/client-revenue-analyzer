package ru.nesterov.core.service.event;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app.analyzer")
public class EventsAnalyzerProperties {
    @Value("${app.analyzer.unpaid-events.range}")
    private Long unpaidEventsRange;
}
