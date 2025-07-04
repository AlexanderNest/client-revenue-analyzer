package ru.nesterov.ai.gigachat.service;

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
import ru.nesterov.ai.core.api.AIIntegrationService;
import ru.nesterov.ai.core.api.TokenService;
import ru.nesterov.ai.gigachat.config.GigaChatIntegrationProperties;
import ru.nesterov.ai.gigachat.dto.GigaChatMessageImp;
import ru.nesterov.ai.gigachat.request.GigaChatTextGenerationRequest;
import ru.nesterov.ai.gigachat.dto.GigaChatChoiceImp;
import ru.nesterov.ai.gigachat.response.GigaChatTextGenerationResponse;

import java.util.List;
import java.util.Optional;

@Service
public class GigachatIntegrationService implements AIIntegrationService {
    private final GigaChatIntegrationProperties properties;
 //   private final TokenServiceImpl tokenService;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;

    public GigachatIntegrationService(GigaChatIntegrationProperties properties,
                                      TokenService tokenService,
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
                    .map(GigaChatTextGenerationResponse::getGigaChatChoiceImps)
                    .flatMap(choices -> choices.stream().findFirst())
                    .map(GigaChatChoiceImp::getGigaChatMessage)
                    .map(GigaChatMessageImp::getContent)
                    .orElse(null);
    }

    private GigaChatTextGenerationRequest getGigaChatTextGenerationRequest(String prompt) {
        GigaChatTextGenerationRequest request = new GigaChatTextGenerationRequest();

        GigaChatMessageImp gigaChatMessageImp1 = new GigaChatMessageImp();
        gigaChatMessageImp1.setRole("system");
        gigaChatMessageImp1.setContent("Отвечай как научный сотрудник");

        GigaChatMessageImp gigaChatMessageImp2 = new GigaChatMessageImp();
        gigaChatMessageImp2.setRole("user");
        gigaChatMessageImp2.setContent(prompt);

        request.setModel("GigaChat");
        request.setStream(false);
        request.setUpdateInterval(0L);
        request.setMessages(List.of(gigaChatMessageImp1, gigaChatMessageImp2));

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
