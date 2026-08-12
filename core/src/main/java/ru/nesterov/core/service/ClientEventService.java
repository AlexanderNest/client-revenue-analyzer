package ru.nesterov.core.service;

import ru.nesterov.calendar.integration.dto.ClientEventDto;

public interface ClientEventService {
    ClientEventDto createClientEventLink(ClientEventDto clientEventDto);
}
