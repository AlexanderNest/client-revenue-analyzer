package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.CheckUserForExistenceInDbRequest;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
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

        return post(String.valueOf(userId), getForMonthRequest, "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth", GetIncomeAnalysisForMonthResponse.class);
    }

    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        return post(createUserRequest.getUserIdentifier(), createUserRequest, "/revenue-analyzer/user/createUser", CreateUserResponse.class);
    }

    public Boolean checkUserForExistenceInDb(CheckUserForExistenceInDbRequest request) {
        return post(request.getUserIdentifier(), request, "/revenue-analyzer/user/checkUserForExistenceInDB", Boolean.class);
    }

    private <T> T post(String username, Object request, String endpoint, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-secret-token", botProperties.getSecretToken());
        headers.set("X-username", username);

        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(
                revenueAnalyzerProperties.getUrl() + endpoint,
                entity,
                responseType);
    }
}
