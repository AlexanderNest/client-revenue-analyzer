package ru.nesterov.ai.gigachat.response;

import lombok.Data;

@Data
public class Message {
    private String content;
    private String role;
}
