package ru.nesterov.ai.gigachat.request;

import lombok.Data;

@Data
public class Message {
    private String role;
    private String content;
}
