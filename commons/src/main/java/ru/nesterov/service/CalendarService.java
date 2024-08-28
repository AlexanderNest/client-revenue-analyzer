package ru.nesterov.service;

import ru.nesterov.dto.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarService {
    List<Event> getEventsBetweenDates(LocalDateTime leftDate, LocalDateTime rightDate);
}
