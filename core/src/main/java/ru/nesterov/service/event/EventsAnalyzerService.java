package ru.nesterov.service.event;

import ru.nesterov.dto.Event;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.service.dto.IncomeAnalysisResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventsAnalyzerService {
    Map<EventStatus, Integer> getEventStatusesBetweenDates(String username, LocalDateTime leftDate, LocalDateTime rightDate);
    Map<EventStatus, Integer> getEventStatusesByMonthName(String username, String monthName);
    IncomeAnalysisResult getIncomeAnalysisByMonth(String username, String monthName);
    Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetings(String username, String monthName);
    List<Event> getUnpaidEventsBetweenDates(String mainCalendar, String cancelledCalendar, LocalDateTime leftDate, LocalDateTime rightDate);
    List<Event> getUnpaidEvents(String username);
}
