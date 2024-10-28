package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.exception.UnknownEventStatusException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.monthHelper.MonthDatesPair;
import ru.nesterov.service.monthHelper.MonthHelper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventsAnalyzerServiceImpl implements EventsAnalyzerService {
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;
    private final EventsAnalyzerProperties eventsAnalyzerProperties;
    private final EventService eventService;
    private final UserRepository userRepository;

    public Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetings(UserDto userDto, String monthName) {
        List<EventDto> eventDtos = getEventsByMonth(userDto, monthName);

        Map<String, ClientMeetingsStatistic> meetingsStatistics = new HashMap<>();

        for (EventDto eventDto : eventDtos) {
            EventStatus eventStatus = eventDto.getStatus();

            ClientMeetingsStatistic clientMeetingsStatistic = meetingsStatistics.get(eventDto.getSummary());
            if (clientMeetingsStatistic == null) {
                Client client = clientRepository.findClientByNameAndUserId(eventDto.getSummary(), userDto.getId());
                if (client == null) {
                    throw new ClientNotFoundException(eventDto.getSummary());
                }
                clientMeetingsStatistic = new ClientMeetingsStatistic(client.getPricePerHour());
            }

            double eventDuration = eventService.getEventDuration(eventDto);
            if (eventStatus == EventStatus.SUCCESS) {
                clientMeetingsStatistic.increaseSuccessful(eventDuration);
            } else if (eventStatus == EventStatus.CANCELLED) {
                clientMeetingsStatistic.increaseCancelled(eventDuration);
            }

            meetingsStatistics.put(eventDto.getSummary(), clientMeetingsStatistic);
        }

        return meetingsStatistics;
    }

    public IncomeAnalysisResult getIncomeAnalysisByMonth(UserDto userDto, String monthName) {
        List<EventDto> eventDtos = getEventsByMonth(userDto, monthName);

        double actualIncome = 0;
        double lostIncome = 0;
        double expectedIncome = 0;

        for (EventDto eventDto : eventDtos) {
            EventStatus eventStatus = eventDto.getStatus();

            Client client = clientRepository.findClientByNameAndUserId(eventDto.getSummary(), userDto.getId());
            if (client == null) {
                throw new ClientNotFoundException(eventDto.getSummary(), eventDto.getStart());
            }

            double eventPrice = eventService.getEventIncome(userDto, eventDto);
            expectedIncome += eventPrice;

            if (eventStatus == EventStatus.SUCCESS) {
                actualIncome += eventPrice;
            } else if (eventStatus == EventStatus.CANCELLED) {
                lostIncome += eventPrice;
            } else if (eventStatus != EventStatus.PLANNED && eventStatus != EventStatus.REQUIRES_SHIFT) {
                throw new UnknownEventStatusException(eventStatus);
            }
        }

        IncomeAnalysisResult incomeAnalysisResult = new IncomeAnalysisResult();
        incomeAnalysisResult.setLostIncome(lostIncome);
        incomeAnalysisResult.setExpectedIncoming(expectedIncome);
        incomeAnalysisResult.setActualIncome(actualIncome);

        return incomeAnalysisResult;
    }

    @Override
    public List<EventDto> getUnpaidEventsBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate) {
        return calendarService.getEventsBetweenDates(userDto.getMainCalendar(), userDto.getCancelledCalendar(), userDto.isCancelledCalendarEnabled(), leftDate, rightDate).stream()
                .filter(event -> {
                    EventStatus eventStatus = event.getStatus();
                    return eventStatus == EventStatus.PLANNED || eventStatus == EventStatus.REQUIRES_SHIFT;
                })
                .toList();
    }

    @Override
    public List<EventDto> getUnpaidEvents(UserDto userDto) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime requiredDateTime = currentDateTime.minusDays(eventsAnalyzerProperties.getUnpaidEventsRange());
        return getUnpaidEventsBetweenDates(userDto, requiredDateTime, LocalDateTime.now());
    }

    public Map<EventStatus, Integer> getEventStatusesByMonthName(UserDto userDto, String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return getEventStatusesBetweenDates(userDto, monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }

    public Map<EventStatus, Integer> getEventStatusesBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate) {
        User user = userRepository.findByUsername(userDto.getUsername());

        List<EventDto> eventDtos = calendarService.getEventsBetweenDates(user.getMainCalendar(), user.getCancelledCalendar(), userDto.isCancelledCalendarEnabled(), leftDate, rightDate);

        Map<EventStatus, Integer> statuses = new HashMap<>();
        for (EventDto eventDto : eventDtos) {
            EventStatus eventStatus = eventDto.getStatus();

            statuses.put(eventStatus, statuses.getOrDefault(eventStatus, 0) + 1);
        }

        return statuses;
    }

    private List<EventDto> getEventsByMonth(UserDto userDto, String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return calendarService.getEventsBetweenDates(userDto.getMainCalendar(), userDto.getCancelledCalendar(), userDto.isCancelledCalendarEnabled(), monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }
}
