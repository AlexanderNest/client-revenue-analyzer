package ru.nesterov.service.event;

import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventsAnalyzerService {
    Map<EventStatus, Integer> getEventStatusesBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate);
    Map<EventStatus, Integer> getEventStatusesByMonthName(UserDto userDto, String monthName);
    IncomeAnalysisResult getIncomeAnalysisByMonth(UserDto userDto, String monthName);
    Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetings(UserDto userDto, String monthName);
    List<Event> getUnpaidEventsBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate);
    List<Event> getUnpaidEvents(UserDto userDto);
}
