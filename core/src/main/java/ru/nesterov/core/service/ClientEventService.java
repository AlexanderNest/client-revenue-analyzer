package ru.nesterov.core.service;

import ru.nesterov.calendar.integration.dto.ClientEventDto;

import java.util.List;

public interface ClientEventService {
    List<ClientEventDto> batchCreateRelation(List<ClientEventDto> clientEventDtoList);

    void deleteRelation();
}
