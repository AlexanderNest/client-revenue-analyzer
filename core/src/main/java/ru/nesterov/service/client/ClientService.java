package ru.nesterov.service.client;

import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.monthHelper.MonthDatesPair;

import java.time.LocalDateTime;
import java.util.List;

public interface ClientService {
    ClientDto createClient(UserDto userDto, ClientDto clientDto, boolean isGenerationOfIdNeeded);

    List<MonthDatesPair> getClientSchedule(UserDto userDto, String clientName, LocalDateTime leftDate, LocalDateTime rightDate);

    List<ClientDto> getActiveClients(UserDto userDto);
}
