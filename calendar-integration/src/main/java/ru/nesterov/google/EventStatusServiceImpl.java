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
        this.successColorCodes = successColorCodes;
        nullWasUsed = addNullCode(successColorCodes, nullWasUsed);
        
        this.cancelledColorCodes = cancelledColorCodes;
        nullWasUsed = addNullCode(cancelledColorCodes, nullWasUsed);
        
        this.requiresShiftColorCodes = requiresShiftColorCodes;
        nullWasUsed = addNullCode(requiresShiftColorCodes, nullWasUsed);
        
        this.plannedColorCodes = plannedColorCodes;
        nullWasUsed = addNullCode(plannedColorCodes, nullWasUsed);
    }
    
    private boolean addNullCode(List<String> codes, boolean nullWasUsed) {
        if (codes.isEmpty()) {
            if (nullWasUsed) {
                throw new IllegalArgumentException("Default calendar color as null was used already");
            }
            codes.add(null);
            return true;
        }
        
        return false;
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
