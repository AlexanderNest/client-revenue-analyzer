package ru.nesterov.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.properties.RevenueAnalyzerProperties;

@Component
@RequiredArgsConstructor
public class ClientRevenueAnalyzerIntegrationClient {
    private final RestTemplate restTemplate;
    private final RevenueAnalyzerProperties revenueAnalyzerProperties;

    public GetIncomeAnalysisForMonthResponse getIncomeAnalysisForMonth(String monthName) {
        GetForMonthRequest getForMonthRequest = new GetForMonthRequest();
        getForMonthRequest.setMonthName(monthName);

        return post(getForMonthRequest, "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth", GetIncomeAnalysisForMonthResponse.class);
    }

    private <T> T post(Object request, String endpoint, Class<T> responseType) {
        return restTemplate.postForObject(
                revenueAnalyzerProperties.getUrl() + endpoint,
                request,
                responseType
        );
    }
}
