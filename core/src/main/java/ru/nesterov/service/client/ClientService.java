package ru.nesterov.service.client;

import ru.nesterov.service.dto.ClientDto;

import java.util.List;

public interface ClientService {
    ClientDto createClient(ClientDto clientDto, boolean isGenerationOfIdNeeded);

    List<ClientDto> findAllByNameContaining(String name);

    List<MonthDatesPair> getClientSchedule(String clientName, LocalDateTime leftDate, LocalDateTime rightDate);
}