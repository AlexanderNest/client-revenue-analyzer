package ru.nesterov.ai.gigachat.dto;

import lombok.Data;

@Data
public class GigaChatToken {
    private String accessToken;
    private Long expirationTime;
}
