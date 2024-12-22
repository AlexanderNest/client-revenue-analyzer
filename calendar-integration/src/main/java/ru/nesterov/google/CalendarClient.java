package ru.nesterov.google;

import com.google.api.services.calendar.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarClient {
    List<Event> getEventsBetweenDates(String calendarId, boolean isCancelledCalendar, LocalDateTime from, LocalDateTime to, List<String> colorsId);
}
