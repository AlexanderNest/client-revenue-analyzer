package ru.nesterov.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.GetClientAnalyticResponse;
import ru.nesterov.gigachat.service.AIIntegrationService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AiAnalyzerControllerTest extends AbstractControllerTest {
    private final static String USERNAME = "testAiAnalyzerUser";
    @MockBean
    private AIIntegrationService AIIntegrationService;

    @Test
    void analyzeClientsShouldReturnRecommendations() throws Exception {
        createUser(USERNAME);
        String monthName = "January";
        String recommendation = "Recommendations for the client data.";

        GetForMonthRequest request = new GetForMonthRequest();
        request.setMonthName(monthName);

        when(AIIntegrationService.generateText(anyString()))
                .thenReturn(recommendation);

        GetClientAnalyticResponse expectedResponse = new GetClientAnalyticResponse();
        expectedResponse.setContent(recommendation);

        mockMvc.perform(get("/ai/generateRecommendation")
                        .header("X-username", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

        verify(AIIntegrationService).generateText(anyString());
    }
}
