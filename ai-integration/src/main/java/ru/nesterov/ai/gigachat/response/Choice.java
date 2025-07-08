package ru.nesterov.ai.gigachat.response;

import lombok.Data;

@Data
public class Choice {
    private Message message;
    private Long index;
    private String finishReason;
}
