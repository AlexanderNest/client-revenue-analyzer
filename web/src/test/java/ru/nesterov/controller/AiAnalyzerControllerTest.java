package ru.nesterov.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.GetClientAnalyticResponse;
import ru.nesterov.gigachat.service.GigaChatApiService;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AiAnalyzerControllerTest extends AbstractControllerTest {
    private final static String USERNAME = "testAiAnalyzerUser";
    @MockBean
    private GigaChatApiService gigaChatApiService;

    @Test
    void analyzeClients_shouldReturnRecommendations() throws Exception {
        createUser(USERNAME);
        String monthName = "January";
        String recommendation = "Recommendations for the client data.";

        GetForMonthRequest request = new GetForMonthRequest();
        request.setMonthName(monthName);

        when(gigaChatApiService.generateText(anyString()))
                .thenReturn(recommendation);

        GetClientAnalyticResponse expectedResponse = new GetClientAnalyticResponse();
        expectedResponse.setContent(recommendation);

        mockMvc.perform(get("/ai/generateRecommendation")
                        .header("X-username", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

        verify(gigaChatApiService).generateText(anyString());
    }
}
