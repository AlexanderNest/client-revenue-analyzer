package ru.nesterov.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.AppException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.dto.ClientDto;
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
            throw new AppException("Клиент с именем " + clientName + " не существует");
        }
        List<Event> events = calendarService.getEventsBetweenDates(leftDate, rightDate);

        return events.stream()
                .filter(event -> event.getSummary().equals(client.getName()))
                .map(event -> new MonthDatesPair(event.getStart(), event.getEnd()))
                .toList();
    }

    @Override
    public List<ClientDto> getFilteredByPriceClients(boolean active) {
        return clientRepository.findClientByActiveOrderByPricePerHourDesc(active).stream()
                .map(client -> ClientDto.builder()
                        .description(client.getDescription())
                        .id(client.getId())
                        .name(client.getName())
                        .pricePerHour(client.getPricePerHour())
                        .active(client.isActive())
                        .build())
                .toList();
    }
}
