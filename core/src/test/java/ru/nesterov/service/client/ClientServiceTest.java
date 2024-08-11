package ru.nesterov.service.client;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.nesterov.entity.Client;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.mapper.ClientMapper;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {
    @InjectMocks
    protected ClientServiceImpl clientService;
    @Mock
    protected ClientRepository repository;
    @Mock
    protected ClientMapper mapper;

    @Test
    void createClient() {
        Client client = new Client();
        client.setName("Vitaliy");
        client.setPricePerHour(1000);
        client.setDescription("desc");

        Client client1 = new Client();
        client1.setName("Vitaliy2");
        client1.setPricePerHour(1000);
        client1.setDescription("desc");

        clientService.createClient(mapper.mapToClientDto(client), false);
        ClientDto actualClient = clientService.createClient(mapper.mapToClientDto(client), true);
        client1.setId(actualClient.getId());

        assertEquals(mapper.mapToClientDto(client1), actualClient);
    }

    @Test
    void findAllByNameContaining() {
    }

    @Test
    void mapToClientDto() {
        Client client = new Client();

        client.setId(1);
        client.setName("Oleg");
        client.setDescription("description");
        client.setPricePerHour(1000);

        ClientDto clientDto = ClientDto.builder()
                .description("description")
                .pricePerHour(1000)
                .id(1)
                .name("Oleg")
                .build();

        ClientDto actualDto = mapper.mapToClientDto(client);

        assertEquals(clientDto, actualDto);
    }
}