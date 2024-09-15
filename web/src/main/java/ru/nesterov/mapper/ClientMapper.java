package ru.nesterov.mapper;

import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.response.ClientResponse;
import ru.nesterov.service.dto.ClientDto;

public class ClientMapper {
    public static ClientDto mapToClientDto(CreateClientRequest request) {
        return ClientDto.builder()
                .description(request.getDescription())
                .pricePerHour(request.getPricePerHour())
                .name(request.getName())
                .pricePerHour(request.getPricePerHour())
                .active(true)
                .build();
    }

    public static ClientResponse mapToClientResponse(ClientDto clientDto) {
        return ClientResponse.builder()
                .id(clientDto.getId())
                .name(clientDto.getName())
                .description(clientDto.getDescription())
                .pricePerHour(clientDto.getPricePerHour())
                .active(clientDto.isActive())
                .startDate(clientDto.getStartDate())
                .phone(clientDto.getPhone())
                .build();
    }

}

