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
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.event.EventsBackupService;

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
    @Autowired
    private UserRepository userRepository;
    
    @MockBean
    private GoogleCalendarClient googleCalendarService;
    @MockBean
    private EventsBackupService eventsBackupService;
    
    private static final String CREATE_CLIENT_URL = "/client/create";
    
    private static final String GET_ACTIVE_CLIENTS_URL = "/client/getActiveClients";
    
    @Test
    @Transactional
    void createClientWithoutIdGeneration() throws Exception {
        User user = new User();
        user.setUsername("testUser");
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
    @Transactional
    void createClientWithTheSameNameWithoutIdGeneration() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);
        
        CreateClientRequest createClientRequest = new CreateClientRequest();
        createClientRequest.setDescription("desc");
        createClientRequest.setName("Maria");
        createClientRequest.setPricePerHour(100);
        createClientRequest.setIdGenerationNeeded(false);
        createClientRequest.setPhone("89001112233");
        
        CreateClientRequest createClientRequest2 = new CreateClientRequest();
        createClientRequest2.setDescription("desc");
        createClientRequest2.setName("Maria Petrova");
        createClientRequest2.setPricePerHour(1000);
        createClientRequest2.setIdGenerationNeeded(false);
        createClientRequest2.setPhone("89001112333");
        
        CreateClientRequest createClientRequest3 = new CreateClientRequest();
        createClientRequest3.setDescription("desc");
        createClientRequest3.setName("Maria");
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
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    @Transactional
    void createClientWithTheSameNameWithIdGeneration() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendar("cancelCalendar");
        user = userRepository.save(user);
        
        CreateClientRequest createClientRequest0 = new CreateClientRequest();
        createClientRequest0.setDescription("desc");
        createClientRequest0.setName("Maria Petrova");
        createClientRequest0.setPricePerHour(100);
        createClientRequest0.setIdGenerationNeeded(true);
        createClientRequest0.setPhone("89001112233");
        
        CreateClientRequest createClientRequest1 = new CreateClientRequest();
        createClientRequest1.setDescription("desc");
        createClientRequest1.setName("Maria");
        createClientRequest1.setPricePerHour(100);
        createClientRequest1.setIdGenerationNeeded(true);
        createClientRequest1.setPhone("89001111233");
        
        CreateClientRequest createClientRequest2 = new CreateClientRequest();
        createClientRequest2.setDescription("desc");
        createClientRequest2.setName("Maria");
        createClientRequest2.setPricePerHour(1000);
        createClientRequest2.setIdGenerationNeeded(true);
        createClientRequest2.setPhone("89001122233");
        
        CreateClientRequest createClientRequest3 = new CreateClientRequest();
        createClientRequest3.setDescription("desc");
        createClientRequest3.setName("Maria");
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
                .andExpect(jsonPath("$.name").value("Maria Petrova"))
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
                .andExpect(jsonPath("$.name").value("Maria"))
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
                .andExpect(jsonPath("$.name").value("Maria 2"))
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
                .andExpect(jsonPath("$.name").value("Maria 3"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.phone").value("89001132233"))
                .andExpect(jsonPath("$.pricePerHour").value(1000));
    }
    
    
    @Test
    @Transactional
    void getActiveClients() throws Exception {
        User user = new User();
        user.setUsername("testUser");
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
}
