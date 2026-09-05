package ru.nesterov.core.service.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.nesterov.core.service.kafka.dto.KafkaMessage;

@Data
@Service
@Slf4j
public class MessageService {

    private String topicName;
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    public MessageService(@Value("${app.kafka.default-topic}") String topicName, KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.topicName = topicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(KafkaMessage message) {
        try {
            kafkaTemplate.send(topicName, message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
