package ru.nesterov.service.mapper;

import ru.nesterov.entity.Client;
import ru.nesterov.service.dto.ClientDto;

public class ClientMapper {
    public static ClientDto mapToClientDto(Client client) {
        return ClientDto.builder()
                .description(client.getDescription())
                .pricePerHour(client.getPricePerHour())
                .name(client.getName())
                .id(client.getId())
                .build();
    }

    public static Client mapToClient(ClientDto clientDto) {
        Client client = new Client();
        client.setDescription(clientDto.getDescription());
        client.setId(clientDto.getId());
        client.setName(clientDto.getName());
        client.setPricePerHour(clientDto.getPricePerHour());
        return client;
    }
}
