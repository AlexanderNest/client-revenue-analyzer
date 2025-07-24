package ru.nesterov.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import ru.nesterov.ai.AIIntegrationService;
import ru.nesterov.web.controller.request.GetForMonthRequest;
import ru.nesterov.web.controller.response.GetClientAnalyticResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(post("/ai/generateRecommendation")
                        .header("X-username", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }
}
