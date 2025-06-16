package ru.nesterov.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.nesterov.common.dto.CalendarServiceDto;
import ru.nesterov.common.dto.EventDto;
import ru.nesterov.common.service.CalendarService;
import ru.nesterov.common.service.EvenExtensionService;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientIsAlreadyCreatedException;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.date.helper.MonthDatesPair;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.dto.FullClientInfoDto;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.event.EventService;
import ru.nesterov.service.event.EventsAnalyzerService;
import ru.nesterov.service.mapper.ClientMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final EventsAnalyzerService eventsAnalyzerService;

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
        Client client;
        try {
            Client clientForSave = ClientMapper.mapToClient(clientDto);
            clientForSave.setUser(user);
            client = clientRepository.save(clientForSave);
        } catch (DataIntegrityViolationException exception) {
            throw new ClientIsAlreadyCreatedException(clientDto.getName());
        }
        return ClientMapper.mapToClientDto(client);
    }

    @Override
    public List<ClientDto> getActiveClients(UserDto userDto) {
        return clientRepository.findClientByUserIdAndActiveOrderByPricePerHourDesc(userDto.getId(), true).stream()
                .map(ClientMapper::mapToClientDto)
                .toList();
    }

    @Override
    public FullClientInfoDto getClientInfo(UserDto userDto, String clientName) {
        ClientDto clientDto = ClientMapper.mapToClientDto(clientRepository.findClientByNameAndUserId(clientName, userDto.getId()));

        return FullClientInfoDto.builder()
                .id(clientDto.getId())
                .name(clientDto.getName())
                .pricePerHour(clientDto.getPricePerHour())
                .description(clientDto.getDescription())
                .startDate(clientDto.getStartDate())
                .phone(clientDto.getPhone())
                .serviceDuration(ChronoUnit.MONTHS.between(LocalDateTime.ofInstant(clientDto.getStartDate().toInstant(), ZoneId.systemDefault()), LocalDateTime.now()))
                .totalMeetings(getTotalMeetings(userDto).size())
                .totalMeetingsHours(getMeetingsHours(getTotalMeetings(userDto)))
                .totalIncome(getMeetingsHours(getTotalMeetings(userDto)) * clientDto.getPricePerHour())
                .plannedCancelledEventsCount(getPlannedCancelledEventsCount(getTotalMeetings(userDto)))
                .unplannedCancelledEventsCount(getUnplannedCancelledEventsCount(getTotalMeetings(userDto)))
                .build();
    }

    public List<EventDto> getTotalMeetings(UserDto userDto) {
        CalendarServiceDto calendarServiceDto = CalendarServiceDto.builder()
                .cancelledCalendar(userDto.getCancelledCalendar())
                .mainCalendar(userDto.getMainCalendar())
                .leftDate(LocalDateTime.now().minusMonths(2))
                .rightDate(LocalDateTime.now())
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .build();

        return calendarService.getEventsBetweenDates(calendarServiceDto);
    }

    public double getMeetingsHours(List<EventDto> eventDtoList) {
        double meetingsHours = 0;
        for(EventDto eventDto : eventDtoList) {
            meetingsHours += eventService.getEventDuration(eventDto);
        }
        return meetingsHours;
    }

    public int getUnplannedCancelledEventsCount(List<EventDto> eventDtos) {
        int unplannedCancelledEventsCount = 0;
        for(EventDto eventDto : eventDtos) {
            if (EvenExtensionService.isPlannedStatus(eventDto)) {
                unplannedCancelledEventsCount++;
            }
        }
        return unplannedCancelledEventsCount;
    }

    public int getPlannedCancelledEventsCount(List<EventDto> eventDtos) {
        int plannedCancelledEventsCount = 0;
        for(EventDto eventDto : eventDtos) {
            if (!(EvenExtensionService.isPlannedStatus(eventDto))) {
                plannedCancelledEventsCount++;
            }
        }
        return plannedCancelledEventsCount;
    }


}
