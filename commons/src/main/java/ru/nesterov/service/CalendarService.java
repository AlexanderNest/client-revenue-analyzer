package ru.nesterov.service;

import ru.nesterov.dto.CalendarServiceDto;
import ru.nesterov.dto.EventDto;
import java.util.List;

public interface CalendarService {
    List<EventDto> getEventsBetweenDates(CalendarServiceDto calendarServiceDto);
}
