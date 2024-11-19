package ru.nesterov.gigachat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.gigachat.GigaChatIntegrationProperties;
import ru.nesterov.gigachat.GigaChatToken;
import ru.nesterov.gigachat.response.GigaChatTokenResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GigaChatTokenService {
    private volatile GigaChatToken token;
    private final RestTemplate restTemplate;
    private final GigaChatIntegrationProperties properties;

    public String getTokenForUser() {
        if (isUpdateRequires()) {
            synchronized (this) {
                if (isUpdateRequires()) {
                    GigaChatTokenResponse response = requestNewToken();

                    GigaChatToken gigaChatToken = new GigaChatToken();
                    gigaChatToken.setAccessToken(response.getAccessToken());
                    gigaChatToken.setExpirationTime(response.getExpiresAt());
                    token = gigaChatToken;
                }
            }
        }

        return token.getAccessToken();
    }

    private GigaChatTokenResponse requestNewToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer "+ properties.getAuthKey());
        headers.set("RqUID", UUID.randomUUID().toString());

        HttpEntity<String> requestEntity = new HttpEntity<>("scope=GIGACHAT_API_PERS", headers);

        GigaChatTokenResponse response = restTemplate.postForObject(properties.getAuthUrl(), requestEntity, GigaChatTokenResponse.class);

        return response;
    }

    private boolean isExpired(GigaChatToken token) {
        return System.currentTimeMillis() > token.getExpirationTime();
    }

    private boolean isUpdateRequires() {
        return token == null || isExpired(token);
    }
}
