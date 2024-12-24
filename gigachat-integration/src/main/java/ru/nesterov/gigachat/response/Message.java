package ru.nesterov.gigachat.response;

import lombok.Data;

@Data
public class Message {
    private String content;
    private String role;
}
