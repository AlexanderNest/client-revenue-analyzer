package ru.nesterov.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.AiAnalyzerResponse;
import ru.nesterov.dto.CreateClientRequest;
import ru.nesterov.dto.CreateClientResponse;
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
import ru.nesterov.dto.GetYearBusynessStatisticsResponse;
import ru.nesterov.dto.MakeEventsBackupResponse;
import ru.nesterov.exception.InternalException;
import ru.nesterov.exception.UserFriendlyException;
import ru.nesterov.properties.BotProperties;
import ru.nesterov.properties.RevenueAnalyzerProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClientRevenueAnalyzerIntegrationClient {
    private final RestTemplate restTemplate;
    private final RevenueAnalyzerProperties revenueAnalyzerProperties;
    private final BotProperties botProperties;
    private final ObjectMapper objectMapper;

    public GetIncomeAnalysisForMonthResponse getIncomeAnalysisForMonth(long userId, String monthName) {
        GetForMonthRequest getForMonthRequest = new GetForMonthRequest();
        getForMonthRequest.setMonthName(monthName);

        return post(String.valueOf(userId), getForMonthRequest, "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth", GetIncomeAnalysisForMonthResponse.class).getBody();
    }

    public GetYearBusynessStatisticsResponse getYearBusynessStatistics(long userId, int year) {
        GetForYearRequest getForYearRequest = new GetForYearRequest();
        getForYearRequest.setYear(year);

        return post(String.valueOf(userId), getForYearRequest, "/revenue-analyzer/user/analyzer/getYearBusynessStatistics", GetYearBusynessStatisticsResponse.class).getBody();
    }

    public AiAnalyzerResponse getAiStatistics(long userId) {
        String currentMonth = LocalDate.now().getMonth().name().toLowerCase();

        GetForMonthRequest request = new GetForMonthRequest();
        request.setMonthName(currentMonth);

        return post(String.valueOf(userId), request, "/revenue-analyzer/ai/generateRecommendation", AiAnalyzerResponse.class).getBody();
    }

    @Cacheable(value = "getUserByUsername", key = "#request.username", unless = "#result == null")
    public GetUserResponse getUserByUsername(GetUserRequest request) {
        ResponseEntity<GetUserResponse> responseEntity = post(request.getUsername(), request, "/revenue-analyzer/user/getUserByUsername", GetUserResponse.class);
        if (responseEntity.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return null;
        }

        return responseEntity.getBody();
    }

    public CreateClientResponse createClient(String userId, CreateClientRequest createClientRequest) {
        try {
            ResponseEntity<CreateClientResponse> responseEntity =
                    post(userId, createClientRequest, "/revenue-analyzer/client/create", CreateClientResponse.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException.Conflict ex) {
            String body = ex.getResponseBodyAsString();
            String message = extractErrorMessage(body);
            return CreateClientResponse.builder()
                    .responseCode(HttpStatus.CONFLICT.value())
                    .errorMessage(message)
                    .build();
        }
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
                new ParameterizedTypeReference<>() {}
        );
    }

    public List<GetActiveClientResponse> getActiveClients(long userId) {
        return postForList(String.valueOf(userId),
                null,
                "/revenue-analyzer/client/getActiveClients",
                new ParameterizedTypeReference<>() {}
        );
    }

    public MakeEventsBackupResponse makeEventsBackup(long userId) {
        ResponseEntity<MakeEventsBackupResponse> response = get(
                String.valueOf(userId),
                "/revenue-analyzer/events/backup",
                MakeEventsBackupResponse.class
        );

        return response.getBody();
    }

    private <T> ResponseEntity<T> get(String username, String endpoint, Class<T> responseType) {
        return exchange(username, null, endpoint, responseType, HttpMethod.GET);
    }

    private <T> ResponseEntity<T> post(String username, Object request, String endpoint, Class<T> responseType) {
        return exchange(username, request, endpoint, responseType, HttpMethod.POST);
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

    private <T> ResponseEntity<T> exchange(String username,
                                           Object request,
                                           String endpoint,
                                           Class<T> responseType,
                                           HttpMethod httpMethod) {
        HttpEntity<Object> entity = new HttpEntity<>(request, createHeaders(username));
        try {
            return restTemplate.exchange(
                    revenueAnalyzerProperties.getUrl() + endpoint,
                    httpMethod,
                    entity,
                    responseType
            );
        } catch (HttpClientErrorException.NotFound ignore) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.Conflict ex) {
            throw ex;
        } catch (HttpServerErrorException.InternalServerError ex) {
            throw new UserFriendlyException(getResponseMessage(ex.getResponseBodyAsString()));
        }
    }

    private String getResponseMessage(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("message").asText();
        } catch (Exception e) {
            log.error("Cannot parse response = [{}]", responseBody);
            throw new InternalException(e);
        }
    }

    private HttpHeaders createHeaders(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-secret-token", botProperties.getSecretToken());
        headers.set("X-username", username);

        return headers;
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("message")) {
                return root.get("message").asText();
            }
        } catch (JsonProcessingException e) {
            log.warn("Не удалось распарсить тело ошибки при создании клиента: {}", responseBody, e);
        }
        return "Клиент уже существует";
    }
}