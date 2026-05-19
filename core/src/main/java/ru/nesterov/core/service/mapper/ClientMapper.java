package ru.nesterov.core.service.mapper;

import ru.nesterov.core.entity.Client;
import ru.nesterov.core.service.dto.ClientDto;


public class ClientMapper {
    public static ClientDto mapToClientDto(Client client, Integer price) {
        return ClientDto.builder()
                .description(client.getDescription())
                .pricePerHour(price)
                .name(client.getName())
                .id(client.getId())
                .active(client.isActive())
                .startDate(client.getStartDate())
                .phone(client.getPhone())
                .build();
    }

    public static Client mapToClient(ClientDto clientDto) {
        Client client = new Client();
        client.setDescription(clientDto.getDescription());
        client.setId(clientDto.getId());
        client.setName(clientDto.getName().trim());
        client.setActive(clientDto.isActive());
        client.setPhone(clientDto.getPhone());
        client.setStartDate(clientDto.getStartDate());
        return client;
    }
}
