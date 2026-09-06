package ru.nesterov.core.service.kafka.dto;

import lombok.Value;

@Value
public class KafkaMessage {
    String userName;
    String message;
}
