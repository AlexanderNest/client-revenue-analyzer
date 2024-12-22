package ru.nesterov.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.google.exception.CannotBuildEventException;
import ru.nesterov.service.CalendarService;
import ru.nesterov.util.PlainTextMapper;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty("app.google.calendar.integration.enabled")
@RequiredArgsConstructor
public class GoogleCalendarService implements CalendarService {
    private final GoogleCalendarClient googleCalendarClient;
    private final ObjectMapper objectMapper;
    private final EventStatusService eventStatusService;

    @Value("${app.calendar.color.cancelled}")
    private List<String> cancelledColorCodes;

    public List<EventDto> getEventsBetweenDates(String mainCalendar, String cancelledCalendar, boolean isCancelledCalendarEnabled, LocalDateTime leftDate, LocalDateTime rightDate) {
        List<EventDto> eventsFromMainCalendar = googleCalendarClient.getEventsBetweenDates(mainCalendar, false, leftDate, rightDate, null)
                .stream()
                .map(event -> buildEvent(event, false))
                .toList();

        if (cancelledCalendar != null && isCancelledCalendarEnabled) {
            List<EventDto> eventsFromCancelledCalendar = googleCalendarClient.getEventsBetweenDates(cancelledCalendar, true, leftDate, rightDate, null).stream()
                    .map(event -> buildEvent(event, true))
                    .toList();

            return mergeEvents(eventsFromMainCalendar, eventsFromCancelledCalendar);
        }

        return eventsFromMainCalendar;
    }

    public void transferCancelledEventsToCancelledCalendar(String mainCalendar, String cancelledCalendar) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime oneMonthFuture = now.plusMonths(1);

        googleCalendarClient.copyCancelledEventsToCancelledCalendar(mainCalendar, cancelledCalendar, oneMonthAgo, oneMonthFuture, cancelledColorCodes);
    }

    private EventDto buildEvent(com.google.api.services.calendar.model.Event event, boolean isCancelledCalendar) {
        try {
            return EventDto.builder()
                    .status(isCancelledCalendar ? EventStatus.CANCELLED : eventStatusService.getEventStatus(event))
                    .summary(event.getSummary())
                    .start(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getStart().getDateTime().getValue()), ZoneId.systemDefault()))
                    .end(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()), ZoneId.systemDefault()))
                    .eventExtensionDto(buildEventExtension(event))
                    .build();
        } catch (Exception e) {
            throw new CannotBuildEventException(event.getSummary(), event.getStart(), e);
        }
    }

    @Nullable
    private EventExtensionDto buildEventExtension(com.google.api.services.calendar.model.Event event) {
        log.trace("Сборка EventExtensionDto для EventDto с названием [{}] and date [{}]", event.getSummary(), event.getStart());

        if (event.getDescription() == null) {
            log.trace("EventDto не содержит EventExtensionDto");
            return null;
        }

        EventExtensionDto eventExtensionDto = buildFromPlainText(event.getDescription());
        if (eventExtensionDto != null) {
            return eventExtensionDto;
        }

        return buildFromJson(event.getDescription());
    }

    private EventExtensionDto buildFromJson(String description) {
        try {
            return objectMapper.readValue(description, EventExtensionDto.class);
        } catch (Exception e) {
            log.trace("Не удалось собрать EventExtensionDto в виде JSON, неверный формат", e);
            return null;
        }
    }

    private EventExtensionDto buildFromPlainText(String description) {
        try {
            return PlainTextMapper.fillFromString(description, EventExtensionDto.class);
        } catch (Exception e) {
            log.trace("Не удалось собрать EventExtensionDto в виде PLAIN TEXT, неверный формат", e);
            return null;
        }
    }

    private List<EventDto> mergeEvents(List<EventDto> eventsFromMainCalendar, List<EventDto> eventsFromCancelledCalendar) {
        List<EventDto> eventDtos = new ArrayList<>(eventsFromMainCalendar);
        eventDtos.addAll(eventsFromCancelledCalendar);

        return eventDtos;
    }
}
