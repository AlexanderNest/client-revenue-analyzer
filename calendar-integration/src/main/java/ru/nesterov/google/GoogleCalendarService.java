package ru.nesterov.google;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
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

    public List<EventDto> getEventsBetweenDates(String mainCalendar, String cancelledCalendar, boolean isCancelledCalendarEnabled, LocalDateTime leftDate, LocalDateTime rightDate) {
        List<EventDto> eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(mainCalendar, false, leftDate, rightDate, null);

        if (cancelledCalendar != null && isCancelledCalendarEnabled) {
            List<EventDto> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(cancelledCalendar, true, leftDate, rightDate, null);
            return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
        }

        return eventsFromMainCalendar;
    }

    public void transferCancelledEventsToCancelledCalendar(String mainCalendar, String cancelledCalendar) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime oneMonthFuture = now.plusMonths(1);
        googleCalendarClient.copyCancelledEventsToCancelledCalendar(mainCalendar, cancelledCalendar, oneMonthAgo, oneMonthFuture);
    }

    private List<EventDto> mergeEvents(List<EventDto> eventsFromMainCalendar, List<EventDto> eventsFromCancelledCalendar) {
        List<EventDto> eventDtos = new ArrayList<>(eventsFromMainCalendar);
        eventDtos.addAll(eventsFromCancelledCalendar);

        return eventDtos;
    }
}
