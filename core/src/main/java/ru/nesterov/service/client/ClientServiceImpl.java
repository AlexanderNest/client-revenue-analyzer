package ru.nesterov.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.repository.ClientRepository;
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


    public List<MonthDatesPair> getClientSchedule(String clientName, LocalDateTime leftDate, LocalDateTime rightDate) {
        Client client = clientRepository.findClientByName(clientName);
        if (client == null) {
            throw new ClientNotFoundException(clientName);
        }
        List<Event> events = calendarService.getEventsBetweenDates(leftDate, rightDate);

        return events.stream()
                .filter(event -> event.getSummary().equals(client.getName()))
                .map(event -> new MonthDatesPair(event.getStart(), event.getEnd()))
                .toList();
    }

    public ClientDto createClient(ClientDto clientDto, boolean isIdGenerationNeeded) throws ClientNotFoundException {
        List<Client> clientsWithThisName = clientRepository.findAllByNameContaining(clientDto.getName()).stream()
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
    public List<ClientDto> getActiveClients() {
        return clientRepository.findClientByActiveOrderByPricePerHourDesc(true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }
}
