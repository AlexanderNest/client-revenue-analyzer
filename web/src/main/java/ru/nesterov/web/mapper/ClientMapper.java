package ru.nesterov.web.mapper;

import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.UpdateClientDto;
import ru.nesterov.web.controller.request.CreateClientRequest;
import ru.nesterov.web.controller.request.UpdateClientRequest;
import ru.nesterov.web.controller.response.ClientMeetingsStatisticResponse;
import ru.nesterov.web.controller.response.ClientResponse;

public class ClientMapper {

    public static UpdateClientDto mapToUpdatedClientDto(UpdateClientRequest request) {

        return UpdateClientDto.builder()
                .description(request.getDescription())
                .pricePerHour(request.getPricePerHour())
                .newName(request.getNewName())
                .idGenerationNeeded(request.getIdGenerationNeeded())
                .phone(request.getPhone())
                .build();
    }
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

    public static ClientMeetingsStatisticResponse mapToClientMeetingsStatisticResponse(ClientMeetingsStatistic clientMeetingsStatistic) {
        return ClientMeetingsStatisticResponse.builder()
                .name(clientMeetingsStatistic.getName())
                .id(clientMeetingsStatistic.getId())
                .description(clientMeetingsStatistic.getDescription())
                .startDate(clientMeetingsStatistic.getStartDate())
                .serviceDuration(clientMeetingsStatistic.getServiceDuration())
                .phone(clientMeetingsStatistic.getPhone())
                .successfulMeetingsHours(clientMeetingsStatistic.getSuccessfulMeetingsHours())
                .cancelledMeetingsHours(clientMeetingsStatistic.getCancelledMeetingsHours())
                .incomePerHour(clientMeetingsStatistic.getIncomePerHour())
                .successfulEventsCount(clientMeetingsStatistic.getSuccessfulEventsCount())
                .plannedCancelledEventsCount(clientMeetingsStatistic.getPlannedCancelledEventsCount())
                .notPlannedCancelledEventsCount(clientMeetingsStatistic.getNotPlannedCancelledEventsCount())
                .totalIncome(clientMeetingsStatistic.getTotalIncome())
                .build();
    }
}

