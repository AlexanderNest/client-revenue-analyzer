package ru.nesterov.core.service.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(value = "app.kafka.enabled")
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(kafkaProperties.getDefaultTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

    @KafkaListener(
            topics = "${app.kafka.default-topic}",
            groupId = "revenue-analyzer-group"
    )
    public void consume(String message) {
        System.out.println("Received Message: " + message);
    }
}
