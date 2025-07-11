package ru.nesterov.calendar.integration.google;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.calendar.integration.dto.CalendarType;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.service.CalendarService;

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
    public List<EventDto> getEventsBetweenDates(EventsFilter eventsFilter) {
        List<EventDto> eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(eventsFilter.getMainCalendar(), CalendarType.MAIN, eventsFilter.getLeftDate(), eventsFilter.getRightDate(), eventsFilter.getClientName());

        if (eventsFilter.getCancelledCalendar() != null && eventsFilter.isCancelledCalendarEnabled()) {
            List<EventDto> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(eventsFilter.getCancelledCalendar(), CalendarType.CANCELLED, eventsFilter.getLeftDate(), eventsFilter.getRightDate(), eventsFilter.getClientName());
            return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
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
