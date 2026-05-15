package ru.nesterov.core.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.PriceChangeHistory;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.exception.ClientDataIntegrityException;
import ru.nesterov.core.exception.ClientNotFoundException;
import ru.nesterov.core.exception.NoPriceChangeHistoryException;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.PriceChangeHistoryRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.ClientScheduleDto;
import ru.nesterov.core.service.dto.UpdateClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.mapper.ClientMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PriceChangeHistoryRepository priceChangeHistoryRepository;

    public double getPricePerHourForDate(Client client, LocalDateTime dateTime) {
        return priceChangeHistoryRepository.findByClientId(client.getId()).stream()
                .filter(pch -> pch.getChangeDate().isBefore(dateTime) || pch.getChangeDate().isEqual(dateTime))
                .max(Comparator.comparing(PriceChangeHistory::getChangeDate))
                .map(PriceChangeHistory::getPrice)
                .orElseThrow(NoPriceChangeHistoryException::new);
    }

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

    @Override
    @Transactional
    public ClientDto createClient(UserDto userDto, ClientDto clientDto, boolean isIdGenerationNeeded) throws ClientDataIntegrityException {
        String uniqueName = generateUniqueClientName(clientDto.getName(), userDto.getId(), isIdGenerationNeeded);
        clientDto.setName(uniqueName);

        User user = userRepository.findByUsername(userDto.getUsername());
        Client forSave = ClientMapper.mapToClient(clientDto);
        forSave.setUser(user);

        PriceChangeHistory history = new PriceChangeHistory();
        history.setPrice(clientDto.getPricePerHour());
        history.setChangeDate(LocalDateTime.now());
        history.setClient(forSave);

        forSave.setPriceChangeHistory(new ArrayList<>(List.of(history)));

        Client savedClient = clientRepository.save(forSave);
        log.info("Создан новый клиент: {} с начальной ценой: {}", savedClient.getName(), clientDto.getPricePerHour());

        ClientDto response = ClientMapper.mapToClientDto(savedClient, clientDto.getPricePerHour());
        return response;
    }

    @Override
    public List<ClientDto> getActiveClientsOrderedByPrice(UserDto userDto) {
        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(userDto.getId(), true).stream()
                .map(client -> {
                    int actualPrice = (int) getPricePerHourForDate(client, LocalDateTime.now());
                    return ClientMapper.mapToClientDto(client, actualPrice);
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteClient(UserDto userDto, String clientName) {
        Client client = clientRepository.findClientByNameAndUserId(clientName, userDto.getId());
        if (client == null) {
            throw new ClientNotFoundException(clientName);
        }
        clientRepository.delete(client);
    }

    @Override
    @Transactional
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
            savePriceToHistory(clientForUpdate, updateClientDto.getPricePerHour());
        }

        Client savedClient = clientRepository.save(clientForUpdate);
        int actualPrice = (int) getPricePerHourForDate(savedClient, LocalDateTime.now());

        return ClientMapper.mapToClientDto(savedClient, actualPrice);
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

    private void savePriceToHistory(Client client, Integer price) {
        PriceChangeHistory history = new PriceChangeHistory();
        history.setClient(client);
        history.setPrice(price);
        history.setChangeDate(LocalDateTime.now());
        priceChangeHistoryRepository.save(history);
    }
}
