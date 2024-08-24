package ru.nesterov.google;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.dto.Event;
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
    private final GoogleCalendarProperties properties;

    public List<Event> getEventsBetweenDates(LocalDateTime leftDate, LocalDateTime rightDate) {
        List<Event> eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(properties.getMainCalendarId(), false,leftDate, rightDate);
        List<Event> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(properties.getCancelledCalendarId(), true, leftDate, rightDate);

        return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
    }

    private List<Event> mergeEvents(List<Event> eventsFromMainCalendar, List<Event> eventsFromCancelledCalendar) {
        List<Event> events = new ArrayList<>(eventsFromMainCalendar);
        if (properties.getCancelledCalendarEnabled()) {
            events.addAll(eventsFromCancelledCalendar);
        }
        return events;
    }
}
