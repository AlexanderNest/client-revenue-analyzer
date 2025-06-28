package ru.nesterov.service;

import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventsFilter;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarService {
    List<EventDto> getEventsBetweenDates(EventsFilter eventsFilter);
    List<EventDto> getHolidays(LocalDateTime leftDate, LocalDateTime rightDate);
}
