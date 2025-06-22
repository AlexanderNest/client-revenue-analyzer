package ru.nesterov.service;

import ru.nesterov.dto.CalendarType;
import ru.nesterov.dto.EventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarClient {
    List<EventDto> getEventsBetweenDates(String calendarId, CalendarType calendarType, LocalDateTime from, LocalDateTime to);
}
