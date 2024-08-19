package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.Json;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.google.GoogleCalendarService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Profile("test")
@SpringBootTest
@Transactional
class ClientControllerTest {
    private static final String CREATE_CLIENT_URL = "/client/create";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private GoogleCalendarService googleCalendarService;


    @Test
    void createClientWithoutIdGeneration() throws Exception {
        CreateClientRequest createClientRequest = new CreateClientRequest();
        createClientRequest.setDescription("desc");
        createClientRequest.setName("Oleg");
        createClientRequest.setPricePerHour(100);
        createClientRequest.setIdGenerationNeeded(false);
        mockMvc.perform(
                post(CREATE_CLIENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createClientRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Oleg"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.pricePerHour").value(100));
    }
}