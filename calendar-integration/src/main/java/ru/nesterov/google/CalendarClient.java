package ru.nesterov.google;

import ru.nesterov.dto.EventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarClient {
    List<EventDto> getEventsBetweenDates(String calendarId, boolean isCancelledCalendar, LocalDateTime from, LocalDateTime to);
}
