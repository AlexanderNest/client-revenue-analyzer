package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.*;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.dto.GetUserResponse;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.dto.GetUserResponse;
import ru.nesterov.properties.BotProperties;
import ru.nesterov.properties.RevenueAnalyzerProperties;

import java.time.LocalDateTime;
import java.util.List;


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

    public GetUserResponse getUserByUsername(GetUserRequest request) {
        ResponseEntity<GetUserResponse> responseEntity = post(request.getUsername(), request, "/revenue-analyzer/user/getUserByUsername", GetUserResponse.class);
        if (responseEntity.getStatusCode() == HttpStatusCode.valueOf(404)) {
            return null;
        } else {
            return responseEntity.getBody();
        }
    }

    public List<GetClientScheduleResponse> getClientSchedule(long userId, String clientName, LocalDateTime leftDate, LocalDateTime rightDate) {
        GetForClientScheduleRequest request = new GetForClientScheduleRequest();
        request.setClientName(clientName);
        request.setLeftDate(leftDate);
        request.setRightDate(rightDate);

        return postForList(
                String.valueOf(userId),
                request,
                "/revenue-analyzer/client/getSchedule",
                new ParameterizedTypeReference<List<GetClientScheduleResponse>>() {
                }
        );
    }

    public List<GetActiveClientResponse> getActiveClients(long userId) {
        return postForList(String.valueOf(userId),
                null,
                "/revenue-analyzer/client/getActiveClients",
                new ParameterizedTypeReference<List<GetActiveClientResponse>>() {
                }
        );
    }

    private <T> T post(String username, Object request, String endpoint, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(request, createHeaders(username));

        return restTemplate.postForObject(
                revenueAnalyzerProperties.getUrl() + endpoint,
                entity,
                responseType
        );
    }

    private <T> List<T> postForList(String username, Object request, String endpoint, ParameterizedTypeReference<List<T>> typeReference) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(request, createHeaders(username));

        ResponseEntity<List<T>> responseEntity = restTemplate.exchange(
                revenueAnalyzerProperties.getUrl() + endpoint,
                HttpMethod.POST,
                requestEntity,
                typeReference
        );

        return responseEntity.getBody();
    }

    private HttpHeaders createHeaders(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-secret-token", botProperties.getSecretToken());
        headers.set("X-username", username);

        return headers;
    }
}