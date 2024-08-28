package ru.nesterov.google;

import ru.nesterov.dto.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarClient {
    List<Event> getEventsBetweenDates(String calendarId, boolean isCancelledCalendar, LocalDateTime from, LocalDateTime to);
}
