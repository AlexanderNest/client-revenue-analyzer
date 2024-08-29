package ru.nesterov.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.mapper.ClientMapper;
import ru.nesterov.service.monthHelper.MonthDatesPair;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;


    public List<MonthDatesPair> getClientSchedule(String username, String clientName, LocalDateTime leftDate, LocalDateTime rightDate) {
        User user = userRepository.findByUsername(username);

        Client client = clientRepository.findClientByNameAndUserId(clientName, user.getId());
        if (client == null) {
            throw new ClientNotFoundException(clientName);
        }
        List<Event> events = calendarService.getEventsBetweenDates(user.getMainCalendar(), user.getCancelledCalendar(), leftDate, rightDate);

        return events.stream()
                .filter(event -> event.getSummary().equals(client.getName()))
                .map(event -> new MonthDatesPair(event.getStart(), event.getEnd()))
                .toList();
    }

    public ClientDto createClient(String username, ClientDto clientDto, boolean isIdGenerationNeeded) throws ClientNotFoundException {
        User user = userRepository.findByUsername(username);
        clientDto.setUserId(user.getId());

        List<Client> clientsWithThisName = clientRepository.findAllByNameContainingAndUserId(clientDto.getName(), user.getId()).stream()
                .toList();
        if (!clientsWithThisName.isEmpty() && !isIdGenerationNeeded) {
            throw new ClientNotFoundException(clientDto.getName());
        }
        if (!clientsWithThisName.isEmpty()) {
            clientDto.setName(clientDto.getName() + " " + (clientsWithThisName.size() + 1));
        }
        Client client = clientRepository.save(ClientMapper.mapToClient(clientDto));
        return ClientMapper.mapToClientDto(client);
    }

    @Override
    public List<ClientDto> getActiveClients(String username) {
        User user = userRepository.findByUsername(username);

        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(user.getId(),true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }
}
