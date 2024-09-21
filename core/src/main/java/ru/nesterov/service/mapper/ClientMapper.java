package ru.nesterov.service.mapper;

import ru.nesterov.entity.Client;
import ru.nesterov.service.dto.ClientDto;

import java.sql.Timestamp;

public class ClientMapper {
    public static ClientDto mapToClientDto(Client client) {
//        Timestamp startDate = new Timestamp(client.getStartDate().getTime());
        return ClientDto.builder()
                .description(client.getDescription())
                .pricePerHour(client.getPricePerHour())
                .name(client.getName())
                .id(client.getId())
                .active(client.isActive())
//                .startDate(startDate)
                .startDate(client.getStartDate())
                .phone(client.getPhone())
                .build();
    }

    public static Client mapToClient(ClientDto clientDto) {
        Client client = new Client();
        client.setDescription(clientDto.getDescription());
        client.setId(clientDto.getId());
        client.setName(clientDto.getName());
        client.setPricePerHour(clientDto.getPricePerHour());
        client.setActive(clientDto.isActive());
        client.setPhone(clientDto.getPhone());
        client.setStartDate(clientDto.getStartDate());
        return client;
    }
}
