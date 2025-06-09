package ru.nesterov.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.CalendarServiceDto;
import ru.nesterov.dto.EventDto;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientIsAlreadyCreatedException;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;
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
    private final IndexesService indexesService;
    private static final Pattern H2_PATTERN =
            Pattern.compile("\"([A-Za-z0-9_.]+)\" ON", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONSTRAINT_PATTERN =
            Pattern.compile("constraint \\$?([A-Za-z0-9_.]+)\\$?", Pattern.CASE_INSENSITIVE);

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

    public ClientDto createClient(UserDto userDto, ClientDto clientDto, boolean isIdGenerationNeeded) throws ClientIsAlreadyCreatedException {
        List<Client> clientsWithThisName = clientRepository.findAllByExactNameOrNameStartingWithAndEndingWithNumberAndUserId(clientDto.getName(), userDto.getId());

        if (!clientsWithThisName.isEmpty() && !isIdGenerationNeeded) {
            throw new ClientIsAlreadyCreatedException(clientDto.getName());
        }
        if (!clientsWithThisName.isEmpty()) {
            clientDto.setName(clientDto.getName() + " " + (clientsWithThisName.size() + 1));
        }

        User user = userRepository.findByUsername(userDto.getUsername());
        try {
            Client toSave = ClientMapper.mapToClient(clientDto);
            toSave.setUser(user);

            // ВАЖНО: сразу вставляем и флашим
            Client saved = clientRepository.saveAndFlush(toSave);

            return ClientMapper.mapToClientDto(saved);

        } catch (DataIntegrityViolationException ex) {
            // разбираем исключительно ex.getMessage() — там уже есть имя индекса
            String raw = ex.getMessage();
            String constraint = null;
            if (raw != null) {
                Matcher m = H2_PATTERN.matcher(raw);
                if (m.find()) {
                    constraint = m.group(1);
                } else {
                    m = CONSTRAINT_PATTERN.matcher(raw);
                    if (m.find()) {
                        constraint = m.group(1);
                    }
                }
            }

            String alias = (constraint != null)
                    ? indexesService.getAlias(constraint)
                    : null;

            String msg = (alias != null)
                    ? String.format("Клиент с таким %s уже существует", alias)
                    : "Клиент с таким идентификатором уже существует";

            throw new ClientIsAlreadyCreatedException(msg);
        }
    }



    @Override
    public List<ClientDto> getActiveClients(UserDto userDto) {
        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(userDto.getId(), true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }

    private String getConstraintName(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause().getMessage();
        if (raw == null) {
            return null;
        }
        Matcher m = H2_PATTERN.matcher(raw);
        if (m.find()) {
            return m.group(1);
        }
        m = CONSTRAINT_PATTERN.matcher(raw);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}