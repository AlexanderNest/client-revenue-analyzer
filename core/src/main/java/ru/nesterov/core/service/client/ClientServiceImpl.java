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
import ru.nesterov.core.service.dto.UpdatedClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.mapper.ClientMapper;
import java.time.LocalDateTime;
import java.util.Date;
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
                        .eventStart(event.getStart())
                        .eventEnd(event.getEnd())
                        .requiresShift(event.getStatus() == EventStatus.REQUIRES_SHIFT)
                        .build())
                .toList();
    }

    public ClientDto createClient(UserDto userDto, ClientDto clientDto, boolean isIdGenerationNeeded) throws ClientDataIntegrityException {
        List<Client> clientsWithThisName = clientRepository.findAllByExactNameOrNameStartingWithAndEndingWithNumberAndUserId(clientDto.getName(), userDto.getId());

        if (!clientsWithThisName.isEmpty() && !isIdGenerationNeeded) {
            throw new ClientDataIntegrityException("Клиент с таким именем уже существует");
        }
        if (!clientsWithThisName.isEmpty()) {
            clientDto.setName(clientDto.getName() + " " + (clientsWithThisName.size() + 1));
        }

        User user = userRepository.findByUsername(userDto.getUsername());
        try {
            Client forSave = ClientMapper.mapToClient(clientDto);
            forSave.setUser(user);
            Client saved = clientRepository.save(forSave);
            return ClientMapper.mapToClientDto(saved);
        } catch (DataIntegrityViolationException ex) {
            String alias = DataIntegrityViolationExceptionHandler.getLocalizedMessage(ex);

            String message = (alias != null)
                    ? String.format("%s уже используется", alias)
                    : "Одно из значений, указанных для этого клиента уже используется";

            throw new ClientDataIntegrityException(message);
        }
    }

    @Override
    public List<ClientDto> getActiveClientsOrderedByPrice(UserDto userDto) {
        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(userDto.getId(), true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }

    @Override
    public void deleteClient(UserDto userDto, String nameClient) {
        Client client = clientRepository.findClientByNameAndUserId(nameClient, userDto.getId());
        if (client == null) {
            throw new ClientNotFoundException(userDto.getUsername());
        }
        clientRepository.delete(client);
    }

    @Override
    public ClientDto updatedClient(UserDto userDto, UpdatedClientDto updatedClientDto, String lastNameClient) {
        Client clientForUpdate = clientRepository.findClientByNameAndUserId(lastNameClient, userDto.getId()); //нашла клиента с нужными данными по старому имени
        if (clientForUpdate == null) {
        return null;
    }
        if (updatedClientDto.getNewName() != null){
            clientForUpdate.setName(updatedClientDto.getNewName());
        }

        if (updatedClientDto.getDescription() != null){
            clientForUpdate.setDescription(updatedClientDto.getDescription());
        }

        if (updatedClientDto.getPhone() != null){
            clientForUpdate.setPhone(updatedClientDto.getPhone());
        }

        if (updatedClientDto.getPricePerHour() != null){
            clientForUpdate.setPricePerHour(updatedClientDto.getPricePerHour());
        }

        Client updatedClient = clientRepository.save(clientForUpdate);
        return ClientMapper.mapToClientDto(updatedClient);
    }

}
