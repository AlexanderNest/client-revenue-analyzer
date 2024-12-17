package ru.nesterov.gigachat.service;

import ru.nesterov.gigachat.response.GigaChatTextGenerationResponse;

public interface GigaChatApiService {
    GigaChatTextGenerationResponse generateText();
}
