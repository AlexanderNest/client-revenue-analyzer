package ru.nesterov.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.nesterov.calendar.integration.dto.CalendarType;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.User;
import ru.nesterov.web.controller.request.CreateClientRequest;
import ru.nesterov.web.controller.request.GetClientScheduleRequest;
import ru.nesterov.web.controller.request.UpdateClientRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class ClientControllerTest extends AbstractControllerTest {
    private static final String CREATE_CLIENT_URL = "/client/create";
    private static final String GET_ACTIVE_CLIENTS_URL = "/client/getActiveClients";

    @Test
    void createClientWithoutIdGeneration() throws Exception {
        User user = new User();
        user.setUsername("testUser2");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);

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
        User user = new User();
        user.setUsername("testUser4");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);

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
    void deleteClient() throws Exception {
        User user = new User();
        user.setUsername("testUser15");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);

        Client client = new Client();
        client.setActive(true);
        client.setName("toDelete");
        client.setDescription("deleteDesc");
        client.setPricePerHour(300);
        client.setUser(user);
        client = clientRepository.save(client);

        mockMvc.perform(
                        delete("/client")
                                .param("clientName", client.getName())
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void deleteNotExistingClient() throws Exception {
        User user = new User();
        user.setUsername("testUser6");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);

        String nonExistentClientName = "nonExistentClient";

        mockMvc.perform(
                        delete("/client" + nonExistentClientName)
                                .param("clientName", nonExistentClientName)
                                .header("X-username", user.getUsername())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClient() throws Exception {
        User user = new User();
        user.setUsername("testUser7");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);

        Client client = new Client();
        client.setActive(true);
        client.setName("oldName");
        client.setDescription("oldDesc");
        client.setPhone("89000000000");
        client.setPricePerHour(500);
        client.setUser(user);
        clientRepository.save(client);

        UpdateClientRequest updateRequest = new UpdateClientRequest();
        updateRequest.setClientName("oldName");
        updateRequest.setNewName("newName");
        updateRequest.setDescription("newDesc");
        updateRequest.setPhone("89999999999");
        updateRequest.setPricePerHour(1000);
        updateRequest.setIdGenerationNeeded(false);

        mockMvc.perform(
                        post("/client/update")
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.getNewName()))
                .andExpect(jsonPath("$.description").value(updateRequest.getDescription()))
                .andExpect(jsonPath("$.phone").value(updateRequest.getPhone()))
                .andExpect(jsonPath("$.pricePerHour").value(updateRequest.getPricePerHour()));
    }

    @Test
    void createClientWithTheSameNameWithIdGeneration() throws Exception {
        User user = new User();
        user.setUsername("testUser5");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);

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
        User user = new User();
        user.setUsername("testUser3");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);

        Client client1 = new Client();
        client1.setActive(true);
        client1.setName("a");
        client1.setDescription("aa");
        client1.setPricePerHour(100);
        client1.setUser(user);

        clientRepository.save(client1);

        Client client2 = new Client();
        client2.setActive(true);
        client2.setName("b");
        client2.setDescription("bbb");
        client2.setPricePerHour(200);
        client2.setUser(user);

        clientRepository.save(client2);

        Client client3 = new Client();
        client3.setActive(false);
        client3.setName("c");
        client3.setDescription("ccc");
        client3.setPricePerHour(200);
        client3.setUser(user);

        clientRepository.save(client3);

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

    @Test
    public  void shouldMarkApproveRequiredIfRequiresShift() throws Exception{
        User user = createUser(System.currentTimeMillis() + "_user");
        Client client = createClient("testClient2" + System.currentTimeMillis(), user);

        EventDto eventWithShift = EventDto.builder()
                .summary(client.getName())
                .status(EventStatus.REQUIRES_SHIFT)
                .start(LocalDateTime.of(2024, 8, 11, 11, 30))
                .end(LocalDateTime.of(2024, 8, 11, 12, 30))
                .build();

        EventDto plannedEvent = EventDto.builder()
                .summary(client.getName())
                .status(EventStatus.PLANNED)
                .start(LocalDateTime.of(2024, 8, 12, 11, 30))
                .end(LocalDateTime.of(2024, 8, 12, 12, 30))
                .build();

        when(googleCalendarClient.getEventsBetweenDates(eq("someCalendar1"), eq(CalendarType.MAIN), any(), any(), any()))
                .thenReturn(List.of(eventWithShift, plannedEvent));

        GetClientScheduleRequest request = new GetClientScheduleRequest();
        request.setClientName(client.getName());
        request.setLeftDate(LocalDateTime.of(2024, 8, 9, 11, 30));
        request.setRightDate(LocalDateTime.of(2024, 8, 13, 12, 30));

        mockMvc.perform(post("/client/getSchedule")
                        .header("X-username", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventStart").value("2024-08-11T11:30:00"))
                .andExpect(jsonPath("$[0].eventEnd").value("2024-08-11T12:30:00"))
                .andExpect(jsonPath("$[0].requiresShift").value(true))
                .andExpect(jsonPath("$[1].eventStart").value("2024-08-12T11:30:00"))
                .andExpect(jsonPath("$[1].eventEnd").value("2024-08-12T12:30:00"))
                .andExpect(jsonPath("$[1].requiresShift").value(false));
    }
}
