package ru.nesterov.clientRevenueAnalyzer.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.clientRevenueAnalyzer.dto.EventColor;
import ru.nesterov.clientRevenueAnalyzer.dto.EventStatus;
import ru.nesterov.clientRevenueAnalyzer.entity.Client;
import ru.nesterov.clientRevenueAnalyzer.exception.AppException;
import ru.nesterov.clientRevenueAnalyzer.repository.ClientRepository;
import ru.nesterov.clientRevenueAnalyzer.service.dto.ClientMeetingsStatistic;
import ru.nesterov.clientRevenueAnalyzer.service.dto.IncomeAnalysisResult;
import ru.nesterov.clientRevenueAnalyzer.service.monthHelper.MonthDatesPair;
import ru.nesterov.clientRevenueAnalyzer.service.monthHelper.MonthHelper;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventsAnalyzerService {
    private final GoogleCalendarService googleCalendarService;
    private final ClientRepository clientRepository;

    public Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetings(String monthName) {
        List<Event> events = getEventsByMonth(monthName);

        Map<String, ClientMeetingsStatistic> meetingsStatistics = new HashMap<>();

        for (Event event : events) {
            EventColor eventColor = EventColor.fromColorId(event.getColorId());
            EventStatus eventStatus = EventStatus.fromColor(eventColor);


            ClientMeetingsStatistic clientMeetingsStatistic = meetingsStatistics.get(event.getSummary());
            if (clientMeetingsStatistic == null) {
                Client client = clientRepository.findClientByName(event.getSummary());
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
            EventColor eventColor = EventColor.fromColorId(event.getColorId());
            EventStatus eventStatus = EventStatus.fromColor(eventColor);

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

    private double getEventDuration(Event event) {
        DateTime startDateTimeStr = event.getStart().getDateTime();
        DateTime endDateTimeStr = event.getEnd().getDateTime();

        ZonedDateTime startDateTime = toZonedDateTime(startDateTimeStr);
        ZonedDateTime endDateTime = toZonedDateTime(endDateTimeStr);

        Duration duration = Duration.between(startDateTime, endDateTime);
        return duration.toMinutes() / 60.0;

    }
    private ZonedDateTime toZonedDateTime(DateTime dateTime) {
        return ZonedDateTime.parse(dateTime.toStringRfc3339(), DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")));
    }

    public Map<EventStatus, Integer> getEventStatusesByMonthName(String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return getEventStatusesBetweenDates(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }

    public Map<EventStatus, Integer> getEventStatusesBetweenDates(LocalDateTime leftDate, LocalDateTime rightDate) {
        List<Event> events = googleCalendarService.getEventsBetweenDates(leftDate, rightDate);

        Map<EventStatus, Integer> statuses = new HashMap<>();
        for (Event event : events) {
            EventColor eventColor = EventColor.fromColorId(event.getColorId());
            EventStatus eventStatus = EventStatus.fromColor(eventColor);

            statuses.put(eventStatus, statuses.getOrDefault(eventStatus, 0) + 1);
        }

        return statuses;
    }

    private List<Event> getEventsByMonth(String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return googleCalendarService.getEventsBetweenDates(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }
}
