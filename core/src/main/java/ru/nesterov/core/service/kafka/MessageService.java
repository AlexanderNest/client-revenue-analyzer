package ru.nesterov.core.service.kafka;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.nesterov.core.service.kafka.dto.KafkaMessage;

@Data
@Service
public class MessageService {

    private String topicName;

    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public MessageService(@Value("${app.kafka.default-topic}") String topicName, KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.topicName = topicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Autowired
    public MessageService(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(KafkaMessage message) {
        kafkaTemplate.send(topicName, message);
    }
}
