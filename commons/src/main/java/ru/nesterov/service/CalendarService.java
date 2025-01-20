package ru.nesterov.service;

import ru.nesterov.dto.CalendarServiceDto;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.CalendarType;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarService {
    List<EventDto> getEventsBetweenDates(CalendarServiceDto calendarServiceDto);
}
