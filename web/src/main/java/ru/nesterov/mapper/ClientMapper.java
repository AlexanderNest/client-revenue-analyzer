package ru.nesterov.mapper;

import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.response.ClientResponse;
import ru.nesterov.controller.response.FullClientInfoResponse;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.dto.FullClientInfoDto;

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

    public static FullClientInfoResponse mapToFullClientInfoResponse(FullClientInfoDto fullClientInfoDto) {
        return FullClientInfoResponse.builder()
                .id(fullClientInfoDto.getId())
                .name(fullClientInfoDto.getName())
                .pricePerHour(fullClientInfoDto.getPricePerHour())
                .description(fullClientInfoDto.getDescription())
                .startDate(fullClientInfoDto.getStartDate())
                .phone(fullClientInfoDto.getPhone())
                .serviceDuration(fullClientInfoDto.getServiceDuration())
                .totalMeetings(fullClientInfoDto.getTotalMeetings())
                .totalMeetingsHours(fullClientInfoDto.getTotalMeetingsHours())
                .totalIncome(fullClientInfoDto.getTotalIncome())
                .unplannedCancelledEventsCount(fullClientInfoDto.getUnplannedCancelledEventsCount())
                .plannedCancelledEventsCount(fullClientInfoDto.getPlannedCancelledEventsCount())
                .build();
    }

    public static FullClientInfoDto mapToFullClientInfoDto(FullClientInfoResponse fullClientInfoResponse) {
        return FullClientInfoDto.builder()
                .id(fullClientInfoResponse.getId())
                .name(fullClientInfoResponse.getName())
                .pricePerHour(fullClientInfoResponse.getPricePerHour())
                .description(fullClientInfoResponse.getDescription())
                .startDate(fullClientInfoResponse.getStartDate())
                .phone(fullClientInfoResponse.getPhone())
                .serviceDuration(fullClientInfoResponse.getServiceDuration())
                .totalMeetings(fullClientInfoResponse.getTotalMeetings())
                .totalMeetingsHours(fullClientInfoResponse.getTotalMeetingsHours())
                .totalIncome(fullClientInfoResponse.getTotalIncome())
                .unplannedCancelledEventsCount(fullClientInfoResponse.getUnplannedCancelledEventsCount())
                .plannedCancelledEventsCount(fullClientInfoResponse.getPlannedCancelledEventsCount())
                .build();
    }
}

