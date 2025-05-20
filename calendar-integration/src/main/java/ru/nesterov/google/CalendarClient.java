package ru.nesterov.google;

import ru.nesterov.common.dto.CalendarType;
import ru.nesterov.common.dto.EventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarClient {
    List<EventDto> getEventsBetweenDates(String calendarId, CalendarType calendarType, LocalDateTime from, LocalDateTime to);
}
