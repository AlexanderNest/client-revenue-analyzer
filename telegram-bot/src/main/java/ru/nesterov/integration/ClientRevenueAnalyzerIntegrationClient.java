package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.*;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetActiveClientResponse;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.dto.GetForClientScheduleRequest;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetForYearRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.dto.GetUserResponse;
import ru.nesterov.dto.GetYearBusynessStatisticsRequest;
import ru.nesterov.dto.GetYearBusynessStatisticsResponse;
import ru.nesterov.properties.BotProperties;
import ru.nesterov.properties.RevenueAnalyzerProperties;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class ClientRevenueAnalyzerIntegrationClient {
    private final RestTemplate restTemplate;
    private final RevenueAnalyzerProperties revenueAnalyzerProperties;
    private final BotProperties botProperties;

    public GetIncomeAnalysisForMonthResponse getIncomeAnalysisForMonth(long userId, String monthName) {
        GetForMonthRequest getForMonthRequest = new GetForMonthRequest();
        getForMonthRequest.setMonthName(monthName);

        return post(String.valueOf(userId), getForMonthRequest, "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth", GetIncomeAnalysisForMonthResponse.class).getBody();
    }

    @Cacheable(value = "getUserByUsername", unless = "#result == null")
    public GetUserResponse getUserByUsername(GetUserRequest request) {
        ResponseEntity<GetUserResponse> responseEntity = post(request.getUsername(), request, "/revenue-analyzer/user/getUserByUsername", GetUserResponse.class);
        if (responseEntity.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return null;
        }

        return responseEntity.getBody();
    }

    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        ResponseEntity<CreateUserResponse> responseEntity = post(createUserRequest.getUserIdentifier(), createUserRequest, "/revenue-analyzer/user/createUser", CreateUserResponse.class);
        return responseEntity.getBody();
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

    private <T> ResponseEntity<T> post(String username, Object request, String endpoint, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(request, createHeaders(username));

        try {
            return restTemplate.exchange(
                    revenueAnalyzerProperties.getUrl() + endpoint,
                    HttpMethod.POST,
                    entity,
                    responseType
            );
        } catch (HttpClientErrorException.NotFound ignore) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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