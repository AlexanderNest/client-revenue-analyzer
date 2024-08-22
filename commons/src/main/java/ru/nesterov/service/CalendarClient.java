package ru.nesterov.service;

import ru.nesterov.dto.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarClient {
    List<Event> getEventsBetweenDates(String calendarId, LocalDateTime from, LocalDateTime to);
}
