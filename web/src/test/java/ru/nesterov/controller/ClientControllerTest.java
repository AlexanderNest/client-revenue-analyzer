package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.entity.Client;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.client.ClientService;
import ru.nesterov.service.dto.ClientDto;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private ClientService clientService;
    @Autowired
    private ClientRepository clientRepository;
    private static final String GET_FILTERED_CLIENTS_URL = "/client/getFilteredClients";

    @Test
    void getFilteredClients() throws Exception {
//        ClientDto clientDto1 = ClientDto.builder()
//                .name("a")
//                .description("aa")
//                .pricePerHour(100)
//                .active(true)
//                .build();
        Client client1 = new Client();
        client1.setActive(true);
        client1.setName("a");
        client1.setDescription("aa");
        client1.setPricePerHour(100);
        clientRepository.save(client1);

//        ClientDto clientDto2 = ClientDto.builder()
//                .name("b")
//                .description("bb")
//                .pricePerHour(200)
//                .active(false)
//                .build();
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

        List<Long> expectedIds = Arrays.asList(client1.getId(), client2.getId());
        List<String> expectedNames = Arrays.asList(client1.getName(), client2.getName());
        List<String> expectedDescriptions = Arrays.asList(client1.getDescription(), client2.getDescription());
        List<Integer> expectedPrices = Arrays.asList(client1.getPricePerHour(), client2.getPricePerHour());
        List<Boolean> expectedActive = Arrays.asList(client1.isActive(), client2.isActive());

        mockMvc.perform(
                        get(GET_FILTERED_CLIENTS_URL)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(expectedIds.toArray())))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(expectedNames.toArray())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(expectedDescriptions.toArray())))
                .andExpect(jsonPath("$[*].pricePerHour", containsInAnyOrder(expectedPrices.toArray())))
                .andExpect(jsonPath("$[*].active", containsInAnyOrder(expectedActive.toArray())));
    }
}