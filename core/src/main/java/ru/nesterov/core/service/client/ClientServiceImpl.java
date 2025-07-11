package ru.nesterov.core.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.exception.ClientDataIntegrityException;
import ru.nesterov.core.exception.ClientNotFoundException;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.date.helper.MonthDatesPair;
import ru.nesterov.core.service.dto.ClientDto;
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

    public List<MonthDatesPair> getClientSchedule(UserDto userDto, String clientName, LocalDateTime leftDate, LocalDateTime rightDate) {
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
                .map(event -> new MonthDatesPair(event.getStart(), event.getEnd()))
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
    public List<ClientDto> getActiveClients(UserDto userDto) {
        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(userDto.getId(), true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }
}
