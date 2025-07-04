package ru.nesterov.gigachat.request;

import lombok.Data;

@Data
public class Message {
    private String role;
    private String content;
}
