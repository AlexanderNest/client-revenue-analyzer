package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.entity.Client;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.repository.ClientRepository;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClientRepository clientRepository;
    @MockBean
    private GoogleCalendarClient googleCalendarService;

    private static final String CREATE_CLIENT_URL = "/client/create";

    private static final String GET_ACTIVE_CLIENTS_URL = "/client/getActiveClients";

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
                        .content(objectMapper.writeValueAsString(createClientRequest))
                )
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
                        .content(objectMapper.writeValueAsString(createClientRequest))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                    post(CREATE_CLIENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createClientRequest2))
                )
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
                            .content(objectMapper.writeValueAsString(createClientRequest))
                )
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

    @Test
    void getActiveClients() throws Exception {
        Client client1 = new Client();
        client1.setActive(true);
        client1.setName("a");
        client1.setDescription("aa");
        client1.setPricePerHour(100);
        clientRepository.save(client1);

        Client client2 = new Client();
        client2.setActive(true);
        client2.setName("b");
        client2.setDescription("bbb");
        client2.setPricePerHour(200);
        clientRepository.save(client2);

        Client client3 = new Client();
        client3.setActive(false);
        client3.setName("c");
        client3.setDescription("ccc");
        client3.setPricePerHour(200);
        clientRepository.save(client3);


        mockMvc.perform(
                        post(GET_ACTIVE_CLIENTS_URL)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(client2.getId()))
                .andExpect(jsonPath("$[0].name").value(client2.getName()))
                .andExpect(jsonPath("$[0].description").value(client2.getDescription()))
                .andExpect(jsonPath("$[0].pricePerHour").value(client2.getPricePerHour()))
                .andExpect(jsonPath("$[0].active").value(client2.isActive()))
                .andExpect(jsonPath("$[1].id").value(client1.getId()))
                .andExpect(jsonPath("$[1].name").value(client1.getName()))
                .andExpect(jsonPath("$[1].description").value(client1.getDescription()))
                .andExpect(jsonPath("$[1].pricePerHour").value(client1.getPricePerHour()))
                .andExpect(jsonPath("$[1].active").value(client1.isActive()));
    }
}