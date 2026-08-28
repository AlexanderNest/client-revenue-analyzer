package ru.nesterov.calendar.integration.service;

import ru.nesterov.calendar.integration.dto.CreateEventDto;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.dto.ResponseCreateEventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarService {
    List<EventDto> getEventsBetweenDates(EventsFilter eventsFilter);

    List<EventDto> getHolidays(LocalDateTime leftDate, LocalDateTime rightDate);

    List<ResponseCreateEventDto> createEvents(String calendarId, List<CreateEventDto> createEventDtoList);

    void deleteEvents(String calendarId, List<String> eventIdList);
}
