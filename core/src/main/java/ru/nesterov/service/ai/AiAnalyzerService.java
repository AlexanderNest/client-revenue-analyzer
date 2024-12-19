package ru.nesterov.service.ai;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.nesterov.gigachat.response.Choice;
import ru.nesterov.gigachat.service.GigaChatApiService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AiAnalyzerService {
    private final GigaChatApiService gigaChatApiService;
    @Value("classpath:prompt-template.txt")
    private final Resource promptTemplateResource;
    private String textWithClientAnalytic;

    @PostConstruct
    private void init() {
        textWithClientAnalytic = getTextFromFile();
    }

    public String analyzeClients() {
        Choice choice = gigaChatApiService
                .generateText(textWithClientAnalytic)
                .getChoices()
                .stream()
                .findFirst()
                .orElse(null);

        String content = null;

        if (choice != null) {
            content = choice.getMessage().getContent();
        }
        return content;
    }

    private String getTextFromFile() {
        try {
            return promptTemplateResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
