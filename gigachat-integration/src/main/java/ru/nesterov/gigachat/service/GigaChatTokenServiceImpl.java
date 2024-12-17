package ru.nesterov.gigachat.service;

import org.springframework.beans.factory.annotation.Qualifier;
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
public class GigaChatTokenServiceImpl implements GigaChatTokenService {
    private volatile GigaChatToken token;
    private final RestTemplate restTemplate;
    private final GigaChatIntegrationProperties properties;

    public GigaChatTokenServiceImpl(@Qualifier("gigachatRestTemplate")RestTemplate restTemplate, GigaChatIntegrationProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String getToken() {
        if (isUpdateRequires()) {
            synchronized (this) {
                if (isUpdateRequires()) {
                    GigaChatTokenResponse response = requestNewToken();
                    updateToken(response);
                }
            }
        }
        return token.getAccessToken();
    }

    private GigaChatTokenResponse requestNewToken() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>("scope=GIGACHAT_API_PERS", headers);

        return restTemplate.postForObject(properties.getAuthUrl(), requestEntity, GigaChatTokenResponse.class);
    }

    private void updateToken(GigaChatTokenResponse response) {
        GigaChatToken gigaChatToken = new GigaChatToken();
        gigaChatToken.setAccessToken(response.getAccessToken());
        gigaChatToken.setExpirationTime(response.getExpiresAt());
        token = gigaChatToken;
    }

    private boolean isExpired(GigaChatToken token) {
        return System.currentTimeMillis() > token.getExpirationTime();
    }

    private boolean isUpdateRequires() {
        return token == null || isExpired(token);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + properties.getAuthKey());
        headers.set("RqUID", UUID.randomUUID().toString());
        return headers;
    }
}
