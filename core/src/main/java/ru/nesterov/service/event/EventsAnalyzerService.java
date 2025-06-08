package ru.nesterov.service.event;

import ru.nesterov.common.dto.EventDto;
import ru.nesterov.common.dto.EventStatus;
import ru.nesterov.service.dto.BusynessAnalysisResult;
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
    List<EventDto> getUnpaidEventsBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate);
    List<EventDto> getUnpaidEvents(UserDto userDto);
    BusynessAnalysisResult getBusynessStatisticsByYear(UserDto userDto, int year);
}
