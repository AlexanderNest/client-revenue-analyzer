package ru.nesterov.gigachat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.gigachat.GigaChatIntegrationProperties;

@Service
@RequiredArgsConstructor
public class GigaChatIntegrationService {
    private final GigaChatIntegrationProperties properties;
    private final RestTemplate restTemplate;

    public void generateText() {

    }
}
