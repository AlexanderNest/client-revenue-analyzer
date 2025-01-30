package ru.nesterov.google;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.dto.CalendarServiceDto;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.dto.EventDto;
import ru.nesterov.service.CalendarService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty("app.google.calendar.integration.enabled")
@RequiredArgsConstructor
public class GoogleCalendarService implements CalendarService {
    private final GoogleCalendarClient googleCalendarClient;
    @Value("${holiday.calendar}")
    private String calendarId;

    public List<EventDto> getEventsBetweenDates(CalendarServiceDto calendarServiceDto) {
        List<EventDto> eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(calendarServiceDto.getMainCalendar(), CalendarType.MAIN, calendarServiceDto.getLeftDate(), calendarServiceDto.getRightDate());

        if (calendarServiceDto.getCancelledCalendar() != null) {
            List<EventDto> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(calendarServiceDto.getCancelledCalendar(), CalendarType.CANCELLED, calendarServiceDto.getLeftDate(), calendarServiceDto.getRightDate());
            return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
        }
        return eventsFromMainCalendar;
    }

    private List<EventDto> mergeEvents(List<EventDto> eventsFromMainCalendar, List<EventDto> eventsFromCancelledCalendar) {
        List<EventDto> eventDtos = new ArrayList<>(eventsFromMainCalendar);
        eventDtos.addAll(eventsFromCancelledCalendar);
        return eventDtos;
    }

    public List<EventDto> getHolidays(CalendarType calendarType, LocalDateTime leftDate, LocalDateTime rightDate) {
        return googleCalendarClient.getEventsBetweenDates(calendarId, calendarType, leftDate, rightDate);
    }
}
