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

    @Value("${app.kafka.default-topic}")
    private String topicName;

    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Autowired
    public MessageService(KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(KafkaMessage message) {
        kafkaTemplate.send(topicName, message);
    }
}
