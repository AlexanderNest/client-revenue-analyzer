package ru.nesterov.service.event;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.AppException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.EventStatus;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.monthHelper.MonthDatesPair;
import ru.nesterov.service.monthHelper.MonthHelper;
import ru.nesterov.service.status.EventStatusService;
import ru.nesterov.dto.Event;
import ru.nesterov.service.CalendarService;

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

    public Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetings(String monthName) {
        List<Event> events = getEventsByMonth(monthName);

        Map<String, ClientMeetingsStatistic> meetingsStatistics = new HashMap<>();

        for (Event event : events) {
            EventStatus eventStatus = eventStatusService.getEventStatus(event.getColorId());

            ClientMeetingsStatistic clientMeetingsStatistic = meetingsStatistics.get(event.getSummary());
            if (clientMeetingsStatistic == null) {
                Client client = clientRepository.findClientByName(event.getSummary());
                if (client == null) {
                    throw new AppException("Клиент с именем " + event.getSummary() + " не найден");
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

    public IncomeAnalysisResult getIncomeAnalysisByMonth(String monthName) {
        List<Event> events = getEventsByMonth(monthName);

        double actualIncome = 0;
        double lostIncome = 0;
        double expectedIncome = 0;

        for (Event event : events) {
            EventStatus eventStatus = eventStatusService.getEventStatus(event.getColorId());

            Client client = clientRepository.findClientByName(event.getSummary());
            if (client == null) {
                throw new AppException("Пользователь с именем '" + event.getSummary() + "' от даты " + event.getStart() + " не найден в базе");
            }

            double eventPrice = getEventDuration(event) * client.getPricePerHour();

            expectedIncome += eventPrice;

            if (eventStatus == EventStatus.SUCCESS) {
                actualIncome += eventPrice;
            } else if (eventStatus == EventStatus.CANCELLED) {
                lostIncome += eventPrice;
            } else if (eventStatus != EventStatus.PLANNED && eventStatus != EventStatus.REQUIRES_SHIFT) {
                throw new AppException("Обнаружен неизвестный EventStatus " + eventStatus);
            }
        }

        IncomeAnalysisResult incomeAnalysisResult = new IncomeAnalysisResult();
        incomeAnalysisResult.setLostIncome(lostIncome);
        incomeAnalysisResult.setExpectedIncoming(expectedIncome);
        incomeAnalysisResult.setActualIncome(actualIncome);

        return incomeAnalysisResult;
    }

    @Override
    public List<Event> getUnpaidEventsBetweenDates(LocalDateTime leftDate, LocalDateTime rightDate) {
        return calendarService.getEventsBetweenDates(leftDate, rightDate).stream()
                .filter(event -> eventStatusService.getEventStatus(event.getColorId()) == EventStatus.PLANNED)
                .toList();
    }

    @Override
    public List<Event> getUnpaidEvents() {
        return getUnpaidEventsBetweenDates(null, LocalDateTime.now());
    }

    private double getEventDuration(Event event) {
        Duration duration = Duration.between(event.getStart(), event.getEnd());
        return duration.toMinutes() / 60.0;
    }


    public Map<EventStatus, Integer> getEventStatusesByMonthName(String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return getEventStatusesBetweenDates(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }

    public Map<EventStatus, Integer> getEventStatusesBetweenDates(LocalDateTime leftDate, LocalDateTime rightDate) {
        List<Event> events = calendarService.getEventsBetweenDates(leftDate, rightDate);

        Map<EventStatus, Integer> statuses = new HashMap<>();
        for (Event event : events) {
            EventStatus eventStatus = eventStatusService.getEventStatus(event.getColorId());

            statuses.put(eventStatus, statuses.getOrDefault(eventStatus, 0) + 1);
        }

        return statuses;
    }

    private List<Event> getEventsByMonth(String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return calendarService.getEventsBetweenDates(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }
}
