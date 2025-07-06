package ru.nesterov.core.service.event;

import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.core.service.dto.BusynessAnalysisResult;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.IncomeAnalysisResult;
import ru.nesterov.core.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventsAnalyzerService {
    Map<EventStatus, Integer> getEventStatusesBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate);
    Map<EventStatus, Integer> getEventStatusesByMonthName(UserDto userDto, String monthName);
    IncomeAnalysisResult getIncomeAnalysisByMonth(UserDto userDto, String monthName);
    Map<String, ClientMeetingsStatistic> getStatisticsByOneClientMeetings(UserDto userDto, String clientName);
    Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetingsForMonth(UserDto userDto, String monthName);
    List<EventDto> getUnpaidEventsBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate);
    List<EventDto> getUnpaidEvents(UserDto userDto);
    BusynessAnalysisResult getBusynessStatisticsByYear(UserDto userDto, int year);
}
