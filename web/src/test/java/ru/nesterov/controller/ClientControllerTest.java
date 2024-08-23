package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.google.GoogleCalendarClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private GoogleCalendarClient googleCalendarService;

    private static final String CREATE_CLIENT_URL = "/client/create";

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

    @Test
    void createClientWithTheSameNameWithoutIdGeneration() throws Exception {
        CreateClientRequest createClientRequest = new CreateClientRequest();
        createClientRequest.setDescription("desc");
        createClientRequest.setName("Masha");
        createClientRequest.setPricePerHour(100);
        createClientRequest.setIdGenerationNeeded(false);
        CreateClientRequest createClientRequest2 = new CreateClientRequest();
        createClientRequest2.setDescription("desc");
        createClientRequest2.setName("Masha");
        createClientRequest2.setPricePerHour(1000);
        createClientRequest2.setIdGenerationNeeded(false);
        mockMvc.perform(
                post(CREATE_CLIENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createClientRequest)))
                .andExpect(status().isOk());
        mockMvc.perform(
                post(CREATE_CLIENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createClientRequest2)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void createClientWithTheSameNameWithIdGeneration() throws Exception {
        CreateClientRequest createClientRequest = new CreateClientRequest();
        createClientRequest.setDescription("desc");
        createClientRequest.setName("Misha");
        createClientRequest.setPricePerHour(100);
        createClientRequest.setIdGenerationNeeded(false);
        CreateClientRequest createClientRequest2 = new CreateClientRequest();
        createClientRequest2.setDescription("desc");
        createClientRequest2.setName("Misha");
        createClientRequest2.setPricePerHour(1000);
        createClientRequest2.setIdGenerationNeeded(true);
        mockMvc.perform(
                        post(CREATE_CLIENT_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createClientRequest)))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post(CREATE_CLIENT_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createClientRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Misha 2"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.pricePerHour").value(1000));
    }
}