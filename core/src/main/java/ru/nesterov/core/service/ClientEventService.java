package ru.nesterov.core.service;

import ru.nesterov.calendar.integration.dto.ClientEventDto;

import java.util.List;

public interface ClientEventService {
    List<ClientEventDto> createClientEventLinks(List<ClientEventDto> clientEventDtoList);

    List<String> getEventIdsByClientId(Long clientId);
}
