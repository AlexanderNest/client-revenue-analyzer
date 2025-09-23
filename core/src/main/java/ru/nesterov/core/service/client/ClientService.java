package ru.nesterov.core.service.client;

import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.ClientScheduleDto;
import ru.nesterov.core.service.dto.UpdateClientDto;
import ru.nesterov.core.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ClientService {
    ClientDto createClient(UserDto userDto, ClientDto clientDto, boolean isGenerationOfIdNeeded);

    List<ClientScheduleDto> getClientSchedule(UserDto userDto, String clientName, LocalDateTime leftDate, LocalDateTime rightDate);

    List<ClientDto> getActiveClientsOrderedByPrice(UserDto userDto);

    void deleteClient(UserDto userDto, String nameClient);

    ClientDto updateClient(UserDto userDto, UpdateClientDto updateClientDto, String lastName);

}

