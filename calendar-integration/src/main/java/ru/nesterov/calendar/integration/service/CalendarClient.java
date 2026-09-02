package ru.nesterov.calendar.integration.service;

import ru.nesterov.calendar.integration.dto.CalendarType;
import ru.nesterov.calendar.integration.dto.CreateEventDto;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.ResponseCreateEventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarClient {
    List<EventDto> getEventsBetweenDates(String calendarId, CalendarType calendarType, LocalDateTime from, LocalDateTime to);

    List<ResponseCreateEventDto> createEvents(String calendarId, List<CreateEventDto> createEventDtoList);

    void deleteEvents(String calendarId, List<String> eventIdList);
}