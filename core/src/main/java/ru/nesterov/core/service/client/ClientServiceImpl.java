package ru.nesterov.core.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.exception.ClientDataIntegrityException;
import ru.nesterov.core.exception.ClientNotFoundException;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.ClientScheduleDto;
import ru.nesterov.core.service.dto.UpdateClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.mapper.ClientMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public List<ClientScheduleDto> getClientSchedule(UserDto userDto, String clientName, LocalDateTime leftDate, LocalDateTime rightDate) {
        Client client = clientRepository.findClientByNameAndUserId(clientName, userDto.getId());
        if (client == null) {
            throw new ClientNotFoundException(clientName);
        }

        EventsFilter eventsFilter = EventsFilter.builder()
                .cancelledCalendar(userDto.getCancelledCalendar())
                .mainCalendar(userDto.getMainCalendar())
                .leftDate(leftDate)
                .rightDate(rightDate)
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .build();

        List<EventDto> eventDtos = calendarService.getEventsBetweenDates(eventsFilter);

        return eventDtos.stream()
                .filter(event -> event.getSummary().equals(client.getName()))
                .filter(event -> !event.getStatus().isCancelledStatus())
                .map(event -> ClientScheduleDto.builder()
                        .clientName(client.getName())
                        .eventStart(event.getStart())
                        .eventEnd(event.getEnd())
                        .requiresShift(event.getStatus() == EventStatus.REQUIRES_SHIFT)
                        .build())
                .toList();
    }

    public ClientDto createClient(UserDto userDto, ClientDto clientDto, boolean isIdGenerationNeeded) throws ClientDataIntegrityException {
        String uniqueName = generateUniqueClientName(clientDto.getName(), userDto.getId(), isIdGenerationNeeded);
        clientDto.setName(uniqueName);

        User user = userRepository.findByUsername(userDto.getUsername());
        Client forSave = ClientMapper.mapToClient(clientDto);
        forSave.setUser(user);

        return saveClient(forSave);
    }

    @Override
    public List<ClientDto> getActiveClientsOrderedByPrice(UserDto userDto) {
        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(userDto.getId(), true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }

    @Override
    public void deleteClient(UserDto userDto, String clientName) {
        Client client = clientRepository.findClientByNameAndUserId(clientName, userDto.getId());
        if (client == null) {
            throw new ClientNotFoundException(clientName);
        }
        clientRepository.delete(client);
    }

    @Override
    public ClientDto updateClient(UserDto userDto, UpdateClientDto updateClientDto) {
        Client clientForUpdate = clientRepository.findClientByNameAndUserId(updateClientDto.getOldClientName(), userDto.getId());

        if (clientForUpdate == null) {
            throw new ClientNotFoundException(updateClientDto.getOldClientName());
        }

        if (updateClientDto.getNewName() != null){
            clientForUpdate.setName(updateClientDto.getNewName());
        }

        if (updateClientDto.getDescription() != null){
            clientForUpdate.setDescription(updateClientDto.getDescription());
        }

        if (updateClientDto.getPhone() != null){
            clientForUpdate.setPhone(updateClientDto.getPhone());
        }

        if (updateClientDto.getPricePerHour() != null){
            clientForUpdate.setPricePerHour(updateClientDto.getPricePerHour());
        }

        return saveClient(clientForUpdate);
    }

    private String generateUniqueClientName(String baseName, long userId, boolean isIdGenerationNeeded) {
        List<Client> clientsWithThisName = clientRepository
                .findAllByExactNameOrNameStartingWithAndEndingWithNumberAndUserId(baseName, userId);

        if (!clientsWithThisName.isEmpty() && !isIdGenerationNeeded) {
            throw new ClientDataIntegrityException("Клиент с таким именем уже существует");
        }
        if (!clientsWithThisName.isEmpty()) {
            return baseName + " " + (clientsWithThisName.size() + 1);
        }
        return baseName;
    }


    private ClientDto saveClient(Client client) {
        try {
            Client saved = clientRepository.save(client);
            return ClientMapper.mapToClientDto(saved);
        } catch (DataIntegrityViolationException ex) {
            String alias = DataIntegrityViolationExceptionHandler.getLocalizedMessage(ex);

            String message = (alias != null)
                    ? String.format("%s уже используется", alias)
                    : "Одно из значений, указанных для этого клиента уже используется";

            throw new ClientDataIntegrityException(message);
        }
    }
}
