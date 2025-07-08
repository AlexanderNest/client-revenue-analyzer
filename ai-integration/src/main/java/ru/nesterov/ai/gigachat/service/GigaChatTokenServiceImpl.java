package ru.nesterov.ai.gigachat.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.ai.gigachat.config.GigaChatIntegrationProperties;
import ru.nesterov.ai.gigachat.dto.GigaChatToken;
import ru.nesterov.ai.gigachat.response.GigaChatTokenResponse;

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

    public String getToken(boolean forceUpdate) {
        if (isUpdateRequires(forceUpdate)) {
            synchronized (this) {
                if (isUpdateRequires(forceUpdate)) {
                    GigaChatTokenResponse response = requestNewToken();
                    updateToken(response);
                }
            }
        }
        return token.getAccessToken();
    }

    private GigaChatTokenResponse requestNewToken() {
        HttpHeaders headers = createHeaders();

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("scope", "GIGACHAT_API_PERS");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

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

    private boolean isUpdateRequires(boolean forceUpdate) {
        return forceUpdate || token == null || isExpired(token);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Asserts.check(StringUtils.isNotBlank(properties.getAuthKey()), "auth key is required");

        headers.set("Authorization", "Basic " + properties.getAuthKey());
        headers.set("RqUID", UUID.randomUUID().toString());
        return headers;
    }
}
