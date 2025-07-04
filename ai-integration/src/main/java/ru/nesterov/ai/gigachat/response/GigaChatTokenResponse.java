package ru.nesterov.ai.gigachat.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GigaChatTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_at")
    private long expiresAt;
}
