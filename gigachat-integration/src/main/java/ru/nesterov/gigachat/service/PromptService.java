package ru.nesterov.gigachat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PromptService {
    private final ResourceReader resourceReader;

    public String getPrompt(String resourcePath) throws IOException {
        return resourceReader.read(resourcePath);
    }
}
