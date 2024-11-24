package ru.nesterov.gigachat;

import lombok.Data;

@Data
public class GigaChatToken {
    private String accessToken;
    private Long expirationTime;
}
