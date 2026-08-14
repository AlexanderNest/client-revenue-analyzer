package ru.nesterov.core.service;

import ru.nesterov.calendar.integration.dto.ClientEventDto;

import java.util.List;

public interface ClientEventService {
    ClientEventDto createClientEventLink(ClientEventDto clientEventDto);

    List<String> getEventIdsByClientId(Long clientId);
}
