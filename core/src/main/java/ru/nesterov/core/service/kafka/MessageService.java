package ru.nesterov.core.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.nesterov.core.service.kafka.dto.KafkaMessage;

@Service
@Slf4j
@ConditionalOnProperty(value = "app.kafka.enabled", havingValue = "true")
public class MessageService {
    private final String topicName;
    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    public MessageService(@Value("${app.kafka.default-topic}") String topicName, KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.topicName = topicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(KafkaMessage message) {
        kafkaTemplate.send(topicName, message);
    }
}
