package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.CreateUserRequest;
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

    public GetIncomeAnalysisForMonthResponse getIncomeAnalysisForMonth(String monthName) {
        GetForMonthRequest getForMonthRequest = new GetForMonthRequest();
        getForMonthRequest.setMonthName(monthName);

        return post(getForMonthRequest, "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth", GetIncomeAnalysisForMonthResponse.class);
    }

    public String createUser(CreateUserRequest createUserRequest) {
        return post(createUserRequest, "/revenue-analyzer/events/analyzer/createUser", String.class);
    }

    private <T> T post(Object request, String endpoint, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-secret-token", botProperties.getSecretToken());

        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(
                revenueAnalyzerProperties.getUrl() + endpoint,
                entity,
                responseType
        );
    }
}
