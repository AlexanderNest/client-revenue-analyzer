package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.exception.UnknownEventStatusException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.dateHelper.WeekHelper;
import ru.nesterov.service.dto.BusynessAnalysisResult;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.dateHelper.MonthDatesPair;
import ru.nesterov.service.dateHelper.MonthHelper;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Event> events = getEventsByMonth(userDto, monthName);

        Map<String, ClientMeetingsStatistic> meetingsStatistics = new HashMap<>();

        for (Event event : events) {
            EventStatus eventStatus = event.getStatus();

            ClientMeetingsStatistic clientMeetingsStatistic = meetingsStatistics.get(event.getSummary());
            if (clientMeetingsStatistic == null) {
                Client client = clientRepository.findClientByNameAndUserId(event.getSummary(), userDto.getId());
                if (client == null) {
                    throw new ClientNotFoundException(event.getSummary());
                }
                clientMeetingsStatistic = new ClientMeetingsStatistic(client.getPricePerHour());
            }

            double eventDuration = eventService.getEventDuration(event);
            if (eventStatus == EventStatus.SUCCESS) {
                clientMeetingsStatistic.increaseSuccessful(eventDuration);
            } else if (eventStatus == EventStatus.CANCELLED) {
                clientMeetingsStatistic.increaseCancelled(eventDuration);
            }

            meetingsStatistics.put(event.getSummary(), clientMeetingsStatistic);
        }

        return meetingsStatistics;
    }

    public IncomeAnalysisResult getIncomeAnalysisByMonth(UserDto userDto, String monthName) {
        List<Event> events = getEventsByMonth(userDto, monthName);

        double actualIncome = 0;
        double lostIncome = 0;
        double expectedIncome = 0;

        for (Event event : events) {
            EventStatus eventStatus = event.getStatus();

            Client client = clientRepository.findClientByNameAndUserId(event.getSummary(), userDto.getId());
            if (client == null) {
                throw new ClientNotFoundException(event.getSummary(), event.getStart());
            }

            double eventPrice = eventService.getEventIncome(userDto, event);
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
    public List<Event> getUnpaidEventsBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate) {
        return calendarService.getEventsBetweenDates(userDto.getMainCalendar(), userDto.getCancelledCalendar(), userDto.isCancelledCalendarEnabled(), leftDate, rightDate).stream()
                .filter(event -> {
                    EventStatus eventStatus = event.getStatus();
                    return eventStatus == EventStatus.PLANNED || eventStatus == EventStatus.REQUIRES_SHIFT;
                })
                .toList();
    }

    @Override
    public List<Event> getUnpaidEvents(UserDto userDto) {
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

        List<Event> events = calendarService.getEventsBetweenDates(user.getMainCalendar(), user.getCancelledCalendar(), userDto.isCancelledCalendarEnabled(), leftDate, rightDate);

        Map<EventStatus, Integer> statuses = new HashMap<>();
        for (Event event : events) {
            EventStatus eventStatus = event.getStatus();

            statuses.put(eventStatus, statuses.getOrDefault(eventStatus, 0) + 1);
        }

        return statuses;
    }

    private List<Event> getEventsByMonth(UserDto userDto, String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return calendarService.getEventsBetweenDates(userDto.getMainCalendar(), userDto.getCancelledCalendar(), userDto.isCancelledCalendarEnabled(), monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }

    private List<Event> getEventsByYear(UserDto userDto, int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59);
        return calendarService.getEventsBetweenDates(userDto.getMainCalendar(), userDto.getCancelledCalendar(), userDto.isCancelledCalendarEnabled(), startOfYear, endOfYear);
    }

    @Override
    public BusynessAnalysisResult getBusynessStatisticsByYear(UserDto userDto, int year) {
        List<Event> events = getEventsByYear(userDto, year);
        Map<String, Double> monthHours = new HashMap<>();
        Map<String, Double> weekHours = new HashMap<>();
        for (Event event : events) {
            if (event.getStatus() == EventStatus.SUCCESS) {
                double eventDuration = eventService.getEventDuration(event);
                String monthName = MonthHelper.getMonthNameByNumber(event.getStart().getMonthValue());
                monthHours.merge(monthName, eventDuration, Double::sum);
                String dayOfWeekName = WeekHelper.getWeekDayNameByNumber(event.getStart().getDayOfWeek().getValue());
                weekHours.merge(dayOfWeekName, eventDuration, Double::sum);
            }
        }

        LinkedHashMap<String, Double> sortedMonthHours = new LinkedHashMap<>();
        monthHours.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> sortedMonthHours.put(entry.getKey(), entry.getValue()));

        LinkedHashMap<String, Double> sortedWeekDayHours = new LinkedHashMap<>();
        weekHours.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> sortedWeekDayHours.put(entry.getKey(), entry.getValue()));

        BusynessAnalysisResult result = new BusynessAnalysisResult();
        result.setMonths(sortedMonthHours);
        result.setDays(sortedWeekDayHours);

        return result;
    }
}
