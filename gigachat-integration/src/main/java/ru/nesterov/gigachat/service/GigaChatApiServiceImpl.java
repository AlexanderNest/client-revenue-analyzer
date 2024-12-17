package ru.nesterov.gigachat.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.gigachat.request.GigaChatTextGenerationRequest;
import ru.nesterov.gigachat.request.Message;
import ru.nesterov.gigachat.response.GigaChatTextGenerationResponse;

import java.util.List;

@Service
public class GigaChatApiServiceImpl implements GigaChatApiService {
    private final GigaChatTokenServiceImpl tokenService;
    private final PromptService promptService;
    private final RestTemplate restTemplate;

    public GigaChatApiServiceImpl(GigaChatTokenServiceImpl tokenService, PromptService promptService, @Qualifier("gigachatRestTemplate") RestTemplate restTemplate) {
        this.tokenService = tokenService;
        this.promptService = promptService;
        this.restTemplate = restTemplate;
    }

    @SneakyThrows
    public GigaChatTextGenerationResponse generateText() {
        String prompt = promptService.getPrompt("prompt-template.txt");
        String accessToken = tokenService.getToken();

        HttpHeaders headers = createHeaders(accessToken);

        GigaChatTextGenerationRequest request = getGigaChatTextGenerationRequest(prompt);

        HttpEntity<GigaChatTextGenerationRequest> httpEntity = new HttpEntity<>(request, headers);

        return restTemplate.postForObject(
                "https://gigachat.devices.sberbank.ru/api/v1/chat/completions",
                httpEntity,
                GigaChatTextGenerationResponse.class);
    }

    private GigaChatTextGenerationRequest getGigaChatTextGenerationRequest(String prompt) {
        GigaChatTextGenerationRequest request = new GigaChatTextGenerationRequest();

        Message message1 = new Message();
        message1.setRole("system");
        message1.setContent("Отвечай как научный сотрудник");

        Message message2 = new Message();
        message2.setRole("user");
        message2.setContent(prompt);

        request.setModel("GigaChat");
        request.setStream(false);
        request.setUpdateInterval(0L);
        request.setMessages(List.of(message1, message2));

        return request;
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }
}
