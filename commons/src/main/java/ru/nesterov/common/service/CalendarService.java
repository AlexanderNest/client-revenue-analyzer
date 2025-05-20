package ru.nesterov.common.service;

import ru.nesterov.common.dto.CalendarServiceDto;
import ru.nesterov.common.dto.EventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarService {
    List<EventDto> getEventsBetweenDates(CalendarServiceDto calendarServiceDto);
    List<EventDto> getHolidays(LocalDateTime leftDate, LocalDateTime rightDate);
}
