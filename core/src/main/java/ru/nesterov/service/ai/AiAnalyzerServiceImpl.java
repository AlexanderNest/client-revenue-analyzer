package ru.nesterov.service.ai;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.nesterov.gigachat.response.Choice;
import ru.nesterov.gigachat.service.GigaChatApiService;

import java.nio.charset.StandardCharsets;

@Service
public class AiAnalyzerServiceImpl implements AiAnalyzerService {
    private final GigaChatApiService gigaChatApiService;
    private final String prompt;

    @SneakyThrows
    public AiAnalyzerServiceImpl(GigaChatApiService gigaChatApiService, @Value("${prompt.path}") Resource promptTemplateResource) {
        this.gigaChatApiService = gigaChatApiService;
        this.prompt = promptTemplateResource.getContentAsString(StandardCharsets.UTF_8);
    }

    public String analyzeClients(String formattedText) {
        Choice choice = gigaChatApiService
                .generateText(prompt.replace("{{ClientData}}", formattedText))
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
}
