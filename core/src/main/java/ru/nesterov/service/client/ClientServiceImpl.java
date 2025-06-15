package ru.nesterov.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.nesterov.common.dto.CalendarServiceDto;
import ru.nesterov.common.dto.EventDto;
import ru.nesterov.common.service.CalendarService;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientDataIntegrityException;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.database.IndexesService;
import ru.nesterov.service.date.helper.MonthDatesPair;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.mapper.ClientMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        CalendarServiceDto calendarServiceDto = CalendarServiceDto.builder()
                .cancelledCalendar(userDto.getCancelledCalendar())
                .mainCalendar(userDto.getMainCalendar())
                .leftDate(leftDate)
                .rightDate(rightDate)
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .build();

        List<EventDto> eventDtos = calendarService.getEventsBetweenDates(calendarServiceDto);

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
            Client toSave = ClientMapper.mapToClient(clientDto);
            toSave.setUser(user);
            Client saved = clientRepository.saveAndFlush(toSave);
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
