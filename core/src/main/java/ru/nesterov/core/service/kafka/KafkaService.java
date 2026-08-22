package ru.nesterov.core.service.kafka;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.nesterov.core.service.kafka.dto.EventUserActionDto;

@Data
@Service
public class KafkaService {

    @Value("${app.kafka.default-topic}")
    private String topicName;

    private KafkaTemplate<String, EventUserActionDto> kafkaTemplate;

    @Autowired
    public KafkaService(KafkaTemplate<String, EventUserActionDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToQueue(EventUserActionDto message) {
        kafkaTemplate.send(topicName, message);
    }
}
