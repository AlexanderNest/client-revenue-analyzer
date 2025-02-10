package ru.nesterov.gigachat.response;

import lombok.Data;

@Data
public class Choice {
    private Message message;
    private Long index;
    private String finishReason;
}
