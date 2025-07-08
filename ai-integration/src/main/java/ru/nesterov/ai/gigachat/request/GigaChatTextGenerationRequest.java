package ru.nesterov.ai.gigachat.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GigaChatTextGenerationRequest {
    private String model;
    private boolean stream;
    @JsonProperty("update_interval")
    private Long updateInterval;
    private List<Message> messages;
}
