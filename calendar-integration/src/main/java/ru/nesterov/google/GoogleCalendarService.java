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

    public List<Event> getEventsBetweenDates(String mainCalendar, String cancelledCalendar, boolean isCancelledCalendarEnabled, LocalDateTime leftDate, LocalDateTime rightDate) {
        List<Event> eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(mainCalendar, false, leftDate, rightDate);

        if (cancelledCalendar != null && isCancelledCalendarEnabled) {
            List<Event> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(cancelledCalendar, true, leftDate, rightDate);
            return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
        }

        return eventsFromMainCalendar;
    }

    private List<Event> mergeEvents(List<Event> eventsFromMainCalendar, List<Event> eventsFromCancelledCalendar) {
        List<Event> events = new ArrayList<>(eventsFromMainCalendar);
        events.addAll(eventsFromCancelledCalendar);

        return events;
    }
}
