package ru.nesterov.gigachat.response;

import lombok.Data;

@Data
public class GigaChatTokenResponse {
    private String accessToken;
    private long expiresAt;
}
