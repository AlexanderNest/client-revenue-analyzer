package ru.nesterov.mapper;

import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.response.CreateClientResponse;
import ru.nesterov.service.dto.ClientDto;

public class ClientMapper {
    public static ClientDto mapToClientDto(CreateClientRequest request) {
        return ClientDto.builder()
                .description(request.getDescription())
                .pricePerHour(request.getPricePerHour())
                .name(request.getName())
                .pricePerHour(request.getPricePerHour())
                .build();
    }

    public static CreateClientResponse mapToCreateClientResponse(ClientDto clientDto) {
        return CreateClientResponse.builder()
                .id(clientDto.getId())
                .name(clientDto.getName())
                .description(clientDto.getDescription())
                .pricePerHour(clientDto.getPricePerHour())
                .build();
    }
}

