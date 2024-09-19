package ru.nesterov.google;

import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.google.exception.UnknownEventColorIdException;

import java.util.List;

@Service
public class EventStatusServiceImpl implements EventStatusService {
    private final List<String> successColorCodes;
    private final List<String> cancelledColorCodes;
    private final List<String> requiresShiftColorCodes;
    private final List<String> plannedColorCodes;

    public EventStatusServiceImpl(@Value("${app.calendar.color.successful}") List<String> successColorCodes,
                                  @Value("${app.calendar.color.cancelled}") List<String> cancelledColorCodes,
                                  @Value("${app.calendar.color.requires.shift}") List<String> requiresShiftColorCodes,
                                  @Value("${app.calendar.color.planned}") List<String> plannedColorCodes) {

        boolean nullWasUsed = false;
        this.plannedColorCodes = plannedColorCodes;
        if (plannedColorCodes.isEmpty()) {
            this.plannedColorCodes.add(null);
            nullWasUsed = true;
        }
        this.cancelledColorCodes = cancelledColorCodes;
        if (cancelledColorCodes.isEmpty()) {
            if (nullWasUsed) {
                throw new IllegalArgumentException("Default calendar color as null was used already");
            }
            this.cancelledColorCodes.add(null);
            nullWasUsed = true;
        }
        this.requiresShiftColorCodes = requiresShiftColorCodes;
        if (requiresShiftColorCodes.isEmpty()) {
            if (nullWasUsed) {
                throw new IllegalArgumentException("Default calendar color as null was used already");
            }
            this.requiresShiftColorCodes.add(null);
            nullWasUsed = true;
        }
        this.successColorCodes = successColorCodes;
        if (successColorCodes.isEmpty()) {
            if (nullWasUsed) {
                throw new IllegalArgumentException("Default calendar color as null was used already");
            }
            this.successColorCodes.add(null);
        }
    }

    public EventStatus getEventStatus(Event event) {
        String eventColorId = event.getColorId();
        if (successColorCodes.contains(eventColorId)) {
            return EventStatus.SUCCESS;
        } else if (plannedColorCodes.contains(eventColorId)) {
            return EventStatus.PLANNED;
        } else if (cancelledColorCodes.contains(eventColorId)) {
            return EventStatus.CANCELLED;
        } else if (requiresShiftColorCodes.contains(eventColorId)) {
            return EventStatus.REQUIRES_SHIFT;
        }

        throw new UnknownEventColorIdException(eventColorId, event.getSummary(), event.getStart());
    }
}
