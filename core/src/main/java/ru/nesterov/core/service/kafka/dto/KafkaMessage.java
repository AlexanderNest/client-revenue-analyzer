package ru.nesterov.core.service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class KafkaMessage {

    private String userName;
    private String message;

}
