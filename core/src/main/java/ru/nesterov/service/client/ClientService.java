package ru.nesterov.service.client;

import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.monthHelper.MonthDatesPair;

import java.time.LocalDateTime;
import java.util.List;

public interface ClientService {
    ClientDto createClient(String username, ClientDto clientDto, boolean isGenerationOfIdNeeded);

    List<MonthDatesPair> getClientSchedule(String username, String clientName, LocalDateTime leftDate, LocalDateTime rightDate);

    List<ClientDto> getActiveClients(String username);
}
