package ru.nesterov.service.mapper;

import org.springframework.stereotype.Service;
import ru.nesterov.entity.Client;
import ru.nesterov.service.dto.ClientDto;

@Service
public class ClientMapper {
    public ClientDto mapToClientDto(Client client) {
        return ClientDto.builder()
                .description(client.getDescription())
                .pricePerHour(client.getPricePerHour())
                .name(client.getName())
                .id(client.getId())
                .build();
    }

    public Client mapToClient(ClientDto clientDto) {
        Client client = new Client();
        client.setDescription(clientDto.getDescription());
        client.setId(clientDto.getId());
        client.setName(clientDto.getName());
        client.setPricePerHour(clientDto.getPricePerHour());
        return client;
    }
}
