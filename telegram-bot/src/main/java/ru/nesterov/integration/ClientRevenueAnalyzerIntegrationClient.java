package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.ClientResponse;
import ru.nesterov.dto.GetClientScheduleRequest;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.properties.RevenueAnalyzerProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class ClientRevenueAnalyzerIntegrationClient {
    private final RestTemplate restTemplate;
    private final RevenueAnalyzerProperties revenueAnalyzerProperties;

    public GetIncomeAnalysisForMonthResponse getIncomeAnalysisForMonth(String monthName) {
        GetForMonthRequest getForMonthRequest = new GetForMonthRequest();
        getForMonthRequest.setMonthName(monthName);

        return post(getForMonthRequest, "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth", GetIncomeAnalysisForMonthResponse.class);
    }

    public List<GetClientScheduleResponse> getClientSchedule(String clientName, LocalDateTime leftDate, LocalDateTime rightDate) {
        GetClientScheduleRequest request = new GetClientScheduleRequest();
        request.setClientName(clientName);
        request.setLeftDate(leftDate);
        request.setRightDate(rightDate);

        return postForList(request, "/revenue-analyzer/client/getSchedule", new ParameterizedTypeReference<List<GetClientScheduleResponse>>() {
        });
    }

    public List<ClientResponse> getActiveClients() {
        return postForList(
                null,
                "/revenue-analyzer/client/getActiveClients",
                new ParameterizedTypeReference<List<ClientResponse>>() {
                }
        );
    }

    private <T> T post(Object request, String endpoint, Class<T> responseType) {
        return restTemplate.postForObject(
                revenueAnalyzerProperties.getUrl() + endpoint,
                request,
                responseType
        );
    }

    private <T> List<T> postForList(Object request, String endpoint, ParameterizedTypeReference<List<T>> typeReference) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(request, new HttpHeaders());

        ResponseEntity<List<T>> responseEntity = restTemplate.exchange(
                revenueAnalyzerProperties.getUrl() + endpoint,
                HttpMethod.POST,
                requestEntity,
                typeReference
        );

        return responseEntity.getBody();
    }
}
