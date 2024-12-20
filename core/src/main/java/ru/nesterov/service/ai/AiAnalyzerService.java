package ru.nesterov.service.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.nesterov.gigachat.response.Choice;
import ru.nesterov.gigachat.service.GigaChatApiService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class AiAnalyzerService {
    private final GigaChatApiService gigaChatApiService;
    private final Resource promptTemplateResource;
    private String textWithClientAnalytic;

    public AiAnalyzerService(GigaChatApiService gigaChatApiService, @Value("classpath:prompt-template.txt") Resource promptTemplateResource) {
        this.gigaChatApiService = gigaChatApiService;
        this.promptTemplateResource = promptTemplateResource;
    }

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

        if (choice != null && choice.getMessage() != null) {
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
