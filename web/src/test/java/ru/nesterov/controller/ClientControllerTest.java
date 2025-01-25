package ru.nesterov.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.service.client.ClientService;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.dto.UserDto;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class ClientControllerTest extends AbstractControllerTest {
    @Autowired
    private ClientService clientService;
    private static final String CREATE_CLIENT_URL = "/client/create";
    private static final String GET_ACTIVE_CLIENTS_URL = "/client/getActiveClients";

    @Test
    void createClientWithoutIdGeneration() throws Exception {
        UserDto user = createUserWithEnabledSettings("user");

        CreateClientRequest createClientRequest = new CreateClientRequest();
        createClientRequest.setDescription("desc");
        createClientRequest.setName("Oleg");
        createClientRequest.setPricePerHour(100);
        createClientRequest.setIdGenerationNeeded(false);
        createClientRequest.setPhone("89001112233");

        mockMvc.perform(
                post(CREATE_CLIENT_URL)
                        .header("X-username", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createClientRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Oleg"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.phone").value("89001112233"))
                .andExpect(jsonPath("$.pricePerHour").value(100));


    }

    @Test
    void createClientWithTheSameNameWithoutIdGeneration() throws Exception {
        UserDto user = createUserWithEnabledSettings("user1");

        CreateClientRequest createClientRequest = new CreateClientRequest();
        createClientRequest.setDescription("desc");
        createClientRequest.setName("Olga");
        createClientRequest.setPricePerHour(100);
        createClientRequest.setIdGenerationNeeded(false);
        createClientRequest.setPhone("89001112233");

        CreateClientRequest createClientRequest2 = new CreateClientRequest();
        createClientRequest2.setDescription("desc");
        createClientRequest2.setName("Olga Petrova");
        createClientRequest2.setPricePerHour(1000);
        createClientRequest2.setIdGenerationNeeded(false);
        createClientRequest2.setPhone("89001112333");

        CreateClientRequest createClientRequest3 = new CreateClientRequest();
        createClientRequest3.setDescription("desc");
        createClientRequest3.setName("Olga");
        createClientRequest3.setPricePerHour(2000);
        createClientRequest3.setIdGenerationNeeded(false);
        createClientRequest3.setPhone("89001113333");

        mockMvc.perform(
                    post(CREATE_CLIENT_URL)
                            .header("X-username", user.getUsername())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createClientRequest))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                post(CREATE_CLIENT_URL)
                        .header("X-username", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createClientRequest2))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(CREATE_CLIENT_URL)
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createClientRequest3))
                )
                .andExpect(status().is(409));
    }

    @Test
    void createClientWithTheSameNameWithIdGeneration() throws Exception {
        UserDto user = createUserWithEnabledSettings("user2");

        CreateClientRequest createClientRequest0 = new CreateClientRequest();
        createClientRequest0.setDescription("desc");
        createClientRequest0.setName("Masha Petrova");
        createClientRequest0.setPricePerHour(100);
        createClientRequest0.setIdGenerationNeeded(true);
        createClientRequest0.setPhone("89001112233");

        CreateClientRequest createClientRequest1 = new CreateClientRequest();
        createClientRequest1.setDescription("desc");
        createClientRequest1.setName("Masha");
        createClientRequest1.setPricePerHour(100);
        createClientRequest1.setIdGenerationNeeded(true);
        createClientRequest1.setPhone("89001111233");

        CreateClientRequest createClientRequest2 = new CreateClientRequest();
        createClientRequest2.setDescription("desc");
        createClientRequest2.setName("Masha");
        createClientRequest2.setPricePerHour(1000);
        createClientRequest2.setIdGenerationNeeded(true);
        createClientRequest2.setPhone("89001122233");

        CreateClientRequest createClientRequest3 = new CreateClientRequest();
        createClientRequest3.setDescription("desc");
        createClientRequest3.setName("Masha");
        createClientRequest3.setPricePerHour(1000);
        createClientRequest3.setIdGenerationNeeded(true);
        createClientRequest3.setPhone("89001132233");

        mockMvc.perform(
                        post(CREATE_CLIENT_URL)
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createClientRequest0))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Masha Petrova"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.phone").value("89001112233"))
                .andExpect(jsonPath("$.pricePerHour").value(100));

        mockMvc.perform(
                        post(CREATE_CLIENT_URL)
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createClientRequest1))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Masha"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.phone").value("89001111233"))
                .andExpect(jsonPath("$.pricePerHour").value(100));

        mockMvc.perform(
                        post(CREATE_CLIENT_URL)
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createClientRequest2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Masha 2"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.phone").value("89001122233"))
                .andExpect(jsonPath("$.pricePerHour").value(1000));

        mockMvc.perform(
                        post(CREATE_CLIENT_URL)
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createClientRequest3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Masha 3"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.phone").value("89001132233"))
                .andExpect(jsonPath("$.pricePerHour").value(1000));
    }

    @Test
    void getActiveClients() throws Exception {
        UserDto user = createUserWithEnabledSettings("user3");

        ClientDto clientDto1 = ClientDto.builder()
                .active(true)
                .name("a")
                .description("aa")
                .pricePerHour(100)
                .build();
        ClientDto client1 = clientService.createClient(user, clientDto1, false);

        ClientDto clientDto2 = ClientDto.builder()
                .active(true)
                .name("b")
                .description("bbb")
                .pricePerHour(200)
                .build();
        ClientDto client2 = clientService.createClient(user, clientDto2, false);

        ClientDto clientDto3 = ClientDto.builder()
                .active(false)
                .name("c")
                .description("ccc")
                .pricePerHour(200)
                .build();
        clientService.createClient(user, clientDto3, false);

        mockMvc.perform(
                        post(GET_ACTIVE_CLIENTS_URL)
                                .header("X-username", user.getUsername())
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
