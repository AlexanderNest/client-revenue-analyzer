package ru.nesterov.gigachat.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.core.AIIntegrationService;
import ru.nesterov.gigachat.config.GigaChatIntegrationProperties;
import ru.nesterov.gigachat.request.GigaChatTextGenerationRequest;
import ru.nesterov.gigachat.request.Message;
import ru.nesterov.gigachat.response.Choice;
import ru.nesterov.gigachat.response.GigaChatTextGenerationResponse;

import java.util.List;
import java.util.Optional;

@Service
public class GigachatIntegrationService implements AIIntegrationService {
    private final GigaChatIntegrationProperties properties;
    private final GigaChatTokenServiceImpl tokenService;
    private final RestTemplate restTemplate;

    public GigachatIntegrationService(GigaChatIntegrationProperties properties,
                                      GigaChatTokenServiceImpl tokenService,
                                      @Qualifier("gigachatRestTemplate") RestTemplate restTemplate) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.restTemplate = restTemplate;
    }

    @SneakyThrows
    public String generateText(String text) {
        String accessToken = tokenService.getToken(false);
        HttpHeaders headers = createHeaders(accessToken);
        GigaChatTextGenerationRequest request = getGigaChatTextGenerationRequest(text);
        HttpEntity<GigaChatTextGenerationRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<GigaChatTextGenerationResponse> response = requestToGigaChat(httpEntity);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            accessToken = tokenService.getToken(true);
            headers = createHeaders(accessToken);
            httpEntity = new HttpEntity<>(request, headers);
            response = requestToGigaChat(httpEntity);
        }

        return Optional.ofNullable(response.getBody())
                    .map(GigaChatTextGenerationResponse::getChoices)
                    .flatMap(choices -> choices.stream().findFirst())
                    .map(Choice::getMessage)
                    .map(ru.nesterov.gigachat.response.Message::getContent)
                    .orElse(null);
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

    private ResponseEntity<GigaChatTextGenerationResponse> requestToGigaChat(HttpEntity<GigaChatTextGenerationRequest> httpEntity) {
        return restTemplate.exchange(
                properties.getTextGenerationUrl(),
                HttpMethod.POST,
                httpEntity,
                GigaChatTextGenerationResponse.class);
    }
}
