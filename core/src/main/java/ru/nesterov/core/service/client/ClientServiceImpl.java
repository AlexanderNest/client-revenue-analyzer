package ru.nesterov.core.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

        Client savedClient = clientRepository.save(forSave);

        savePriceToHistory(savedClient.getId(), clientDto.getPricePerHour());
        Client reloaded = clientRepository.findById(savedClient.getId()).orElseThrow(() -> new ClientNotFoundException(savedClient.getName()));

        log.info("Создан новый клиент: {} с начальной ценой: {}", savedClient.getName(), clientDto.getPricePerHour());

        ClientDto response = ClientMapper.mapToClientDto(reloaded);
        response.setPricePerHour(clientDto.getPricePerHour());
        return response;
    }

    @Override
    public List<ClientDto> getActiveClientsOrderedByPrice(UserDto userDto) {
        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(userDto.getId(), true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteClient(UserDto userDto, String clientName) {
        Client client = clientRepository.findClientByNameAndUserId(clientName, userDto.getId());
        if (client == null) {
            throw new ClientNotFoundException(clientName);
        }
        priceChangeHistoryRepository.deleteByClientId(client.getId());
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

        Integer currentPrice = clientForUpdate.getPriceChangeHistory().stream()
                .max(Comparator.comparing(PriceChangeHistory::getChangeDate))
                .map(PriceChangeHistory::getPrice)
                .orElse(0);
        Integer responsePrice = currentPrice;

        if (updateClientDto.getPricePerHour() != null){
            savePriceToHistory(clientForUpdate.getId(), updateClientDto.getPricePerHour());
            responsePrice = updateClientDto.getPricePerHour();
        }
        Client savedClient = clientRepository.save(clientForUpdate);
        ClientDto response = ClientMapper.mapToClientDto(savedClient);
        response.setPricePerHour(responsePrice);
        return response;
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

    private void savePriceToHistory(Long clientId, Integer price) {
        PriceChangeHistory history = new PriceChangeHistory();
        history.setClientId(clientId);
        history.setPrice(price);
        history.setChangeDate(LocalDateTime.now());
        priceChangeHistoryRepository.save(history);
    }
}
