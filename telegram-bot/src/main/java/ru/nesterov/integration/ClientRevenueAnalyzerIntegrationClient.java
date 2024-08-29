package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.GetClientScheduleRequest;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.properties.RevenueAnalyzerProperties;

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GetClientScheduleRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<List<GetClientScheduleResponse>> responseEntity = restTemplate.exchange(
                revenueAnalyzerProperties.getUrl() + "/revenue-analyzer/client/getSchedule",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        return responseEntity.getBody();
    }

    private <T> T post(Object request, String endpoint, Class<T> responseType) {
        return restTemplate.postForObject(
                revenueAnalyzerProperties.getUrl() + endpoint,
                request,
                responseType
        );
    }
}
