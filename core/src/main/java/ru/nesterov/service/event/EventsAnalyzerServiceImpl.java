package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.exception.UnknownEventStatusException;
import ru.nesterov.google.EventStatusService;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.monthHelper.MonthDatesPair;
import ru.nesterov.service.monthHelper.MonthHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventsAnalyzerServiceImpl implements EventsAnalyzerService {
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;
    private final EventStatusService eventStatusService;
    private final EventsAnalyzerProperties eventsAnalyzerProperties;
    private final UserRepository userRepository;

    public Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetings(String username, String monthName) {
        List<Event> events = getEventsByMonth(username, monthName);

        Map<String, ClientMeetingsStatistic> meetingsStatistics = new HashMap<>();

        for (Event event : events) {
            EventStatus eventStatus = event.getStatus();

            ClientMeetingsStatistic clientMeetingsStatistic = meetingsStatistics.get(event.getSummary());
            if (clientMeetingsStatistic == null) {
                Client client = clientRepository.findClientByNameAndUserId(event.getSummary(), event.getUserId());
                if (client == null) {
                    throw new ClientNotFoundException(event.getSummary());
                }
                clientMeetingsStatistic = new ClientMeetingsStatistic(client.getPricePerHour());
            }

            double eventDuration = getEventDuration(event);
            if (eventStatus == EventStatus.SUCCESS) {
                clientMeetingsStatistic.increaseSuccessful(eventDuration);
            } else if (eventStatus == EventStatus.CANCELLED) {
                clientMeetingsStatistic.increaseCancelled(eventDuration);
            }

            meetingsStatistics.put(event.getSummary(), clientMeetingsStatistic);
        }

        return meetingsStatistics;
    }

    public IncomeAnalysisResult getIncomeAnalysisByMonth(String username, String monthName) {
        List<Event> events = getEventsByMonth(username, monthName);

        double actualIncome = 0;
        double lostIncome = 0;
        double expectedIncome = 0;

        for (Event event : events) {
            EventStatus eventStatus = event.getStatus();

            Client client = clientRepository.findClientByNameAndUserId(event.getSummary(), event.getUserId());
            if (client == null) {
                throw new ClientNotFoundException(event.getSummary(), event.getStart());
            }

            double eventPrice = getEventDuration(event) * client.getPricePerHour();

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
    public List<Event> getUnpaidEventsBetweenDates(String mainCalender, String cancelledCalendar, LocalDateTime leftDate, LocalDateTime rightDate) {
        return calendarService.getEventsBetweenDates(mainCalender, cancelledCalendar, leftDate, rightDate).stream()
                .filter(event -> {
                    EventStatus eventStatus = event.getStatus();
                    return eventStatus == EventStatus.PLANNED || eventStatus == EventStatus.REQUIRES_SHIFT;
                })
                .toList();
    }

    @Override
    public List<Event> getUnpaidEvents(String username) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime requiredDateTime = currentDateTime.minusDays(eventsAnalyzerProperties.getUnpaidEventsRange());
        User user = userRepository.findByUsername(username);
        return getUnpaidEventsBetweenDates(user.getMainCalendar(), user.getCancelledCalendar(), requiredDateTime, LocalDateTime.now());
    }

    private double getEventDuration(Event event) {
        Duration duration = Duration.between(event.getStart(), event.getEnd());
        return duration.toMinutes() / 60.0;
    }


    public Map<EventStatus, Integer> getEventStatusesByMonthName(String username, String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return getEventStatusesBetweenDates(username, monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }

    public Map<EventStatus, Integer> getEventStatusesBetweenDates(String username, LocalDateTime leftDate, LocalDateTime rightDate) {
        User user = userRepository.findByUsername(username);

        List<Event> events = calendarService.getEventsBetweenDates(user.getMainCalendar(), user.getCancelledCalendar(), leftDate, rightDate);

        Map<EventStatus, Integer> statuses = new HashMap<>();
        for (Event event : events) {
            EventStatus eventStatus = event.getStatus();

            statuses.put(eventStatus, statuses.getOrDefault(eventStatus, 0) + 1);
        }

        return statuses;
    }

    private List<Event> getEventsByMonth(String username, String monthName) {
        User user = userRepository.findByUsername(username);
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        List<Event> events = calendarService.getEventsBetweenDates(user.getMainCalendar(), user.getCancelledCalendar(), monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());

        for (Event event : events) {
            event.setUserId(user.getId());
        }

        return events;
    }
}
