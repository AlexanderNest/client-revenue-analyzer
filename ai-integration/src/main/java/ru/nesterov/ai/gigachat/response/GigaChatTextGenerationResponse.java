package ru.nesterov.ai.gigachat.response;

import lombok.Data;

import java.util.List;

@Data
public class GigaChatTextGenerationResponse {
    private List<Choice> choices;
    private Long created;
    private String model;
    private String object;
    private Usage usage;
}
