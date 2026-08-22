package ru.nesterov.core.service.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app.kafka")
@Data
public class KafkaProperties {
    private boolean enabled;
    private String defaultTopic;
    private int partitions;
    private int replicas;
}
