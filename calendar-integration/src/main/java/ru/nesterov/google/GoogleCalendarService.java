package ru.nesterov.google;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.common.dto.CalendarServiceDto;
import ru.nesterov.common.dto.CalendarType;
import ru.nesterov.common.dto.EventDto;
import ru.nesterov.common.service.CalendarService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty("app.google.calendar.integration.enabled")
public class GoogleCalendarService implements CalendarService {
    private final GoogleCalendarClient googleCalendarClient;
    private final String calendarId;

    public GoogleCalendarService(GoogleCalendarClient googleCalendarClient,
                                 @Value("${app.google.calendar.holiday.calendar}") String calendarId) {
        this.googleCalendarClient = googleCalendarClient;
        this.calendarId = calendarId;
    }

    @Override
    public List<EventDto> getEventsBetweenDates(CalendarServiceDto calendarServiceDto) {
        List<EventDto> eventsFromMainCalendar;
        if(calendarServiceDto.getClientName() == null) {
            eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(calendarServiceDto.getMainCalendar(), CalendarType.MAIN, calendarServiceDto.getLeftDate(), calendarServiceDto.getRightDate());

            if (calendarServiceDto.getCancelledCalendar() != null && calendarServiceDto.isCancelledCalendarEnabled()) {
                List<EventDto> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(calendarServiceDto.getCancelledCalendar(), CalendarType.CANCELLED, calendarServiceDto.getLeftDate(), calendarServiceDto.getRightDate());
                return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
            }
        } else {
            eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(calendarServiceDto.getMainCalendar(), CalendarType.MAIN, calendarServiceDto.getLeftDate(), calendarServiceDto.getRightDate(), calendarServiceDto.getClientName());

            if (calendarServiceDto.getCancelledCalendar() != null && calendarServiceDto.isCancelledCalendarEnabled()) {
                List<EventDto> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(calendarServiceDto.getCancelledCalendar(), CalendarType.CANCELLED, calendarServiceDto.getLeftDate(), calendarServiceDto.getRightDate(), calendarServiceDto.getClientName());
                return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
            }
        }
        return eventsFromMainCalendar;
    }

    @Override
    public List<EventDto> getHolidays(LocalDateTime leftDate, LocalDateTime rightDate) {
        return googleCalendarClient.getEventsBetweenDates(calendarId, CalendarType.PLAIN, leftDate, rightDate);
    }

    private List<EventDto> mergeEvents(List<EventDto> eventsFromMainCalendar, List<EventDto> eventsFromCancelledCalendar) {
        List<EventDto> eventDtos = new ArrayList<>(eventsFromMainCalendar);
        eventDtos.addAll(eventsFromCancelledCalendar);
        return eventDtos;
    }
}
