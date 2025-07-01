package ru.nesterov.web.mapper;

import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.web.controller.request.CreateClientRequest;
import ru.nesterov.web.controller.response.ClientResponse;

public class ClientMapper {
    public static ClientDto mapToClientDto(CreateClientRequest request) {

        return ClientDto.builder()
                .description(request.getDescription())
                .pricePerHour(request.getPricePerHour())
                .name(request.getName())
                .active(true)
                .phone(request.getPhone())
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

