package ru.nesterov.core.service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
public class KafkaMessage {
    String userName;
    String message;
}
