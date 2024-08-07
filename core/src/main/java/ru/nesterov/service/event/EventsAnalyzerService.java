package ru.nesterov.service.event;

import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.EventStatus;
import ru.nesterov.service.dto.IncomeAnalysisResult;

import java.time.LocalDateTime;
import java.util.Map;

public interface EventsAnalyzerService {
    Map<EventStatus, Integer> getEventStatusesBetweenDates(LocalDateTime leftDate, LocalDateTime rightDate);
    Map<EventStatus, Integer> getEventStatusesByMonthName(String monthName);
    IncomeAnalysisResult getIncomeAnalysisByMonth(String monthName);
    Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetings(String monthName);
}
