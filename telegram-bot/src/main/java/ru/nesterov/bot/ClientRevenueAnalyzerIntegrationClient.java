package ru.nesterov.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.dto.GetForMonthRequest;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;

@Component
@RequiredArgsConstructor
public class ClientRevenueAnalyzerIntegrationClient {
    private final RestTemplate restTemplate;

    private static final String URL = "http://localhost:8080";

    public GetIncomeAnalysisForMonthResponse getIncomeAnalysisForMonth(String monthName) {
        GetForMonthRequest getForMonthRequest = new GetForMonthRequest();
        getForMonthRequest.setMonthName(monthName);

        return restTemplate.postForObject(
                URL + "/revenue-analyzer/events/analyzer/getIncomeAnalysisForMonth",
                getForMonthRequest,
                GetIncomeAnalysisForMonthResponse.class
        );
    }
}
