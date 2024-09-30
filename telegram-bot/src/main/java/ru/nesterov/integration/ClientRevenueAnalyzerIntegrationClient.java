package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.dto.GetUserResponse;
import ru.nesterov.properties.BotProperties;
import ru.nesterov.properties.RevenueAnalyzerProperties;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class ClientRevenueAnalyzerIntegrationClient {
    private final RestTemplate restTemplate;
    private final RevenueAnalyzerProperties revenueAnalyzerProperties;
    private final BotProperties botProperties;

    public GetIncomeAnalysisForMonthResponse getIncomeAnalysisForMonth(long userId, String monthName) {
        GetForMonthRequest getForMonthRequest = new GetForMonthRequest();
        getForMonthRequest.setMonthName(monthName);

        ResponseEntity<GetIncomeAnalysisForMonthResponse> responseEntity = post(String.valueOf(userId), getForMonthRequest, "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth", GetIncomeAnalysisForMonthResponse.class);

        return responseEntity.getBody();
    }

    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        ResponseEntity<CreateUserResponse> responseEntity = post(createUserRequest.getUserIdentifier(), createUserRequest, "/revenue-analyzer/user/createUser", CreateUserResponse.class);
        return responseEntity.getBody();
    }

    public GetUserResponse getUserByUsername(GetUserRequest request) {
        ResponseEntity<GetUserResponse> responseEntity = post(request.getUsername(), request, "/revenue-analyzer/user/getUserByUsername", GetUserResponse.class);
        if (responseEntity.getStatusCode() == HttpStatusCode.valueOf(404)) {
            return null;
        } else {
            return responseEntity.getBody();
        }
    }

    private <T> ResponseEntity<T> post(String username, Object request, String endpoint, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-secret-token", botProperties.getSecretToken());
        headers.set("X-username", username);

        HttpEntity<Object> entity = new HttpEntity<>(request, headers);

        try {
            T response = restTemplate.postForObject(
                    revenueAnalyzerProperties.getUrl() + endpoint,
                    entity,
                    responseType
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }
}
