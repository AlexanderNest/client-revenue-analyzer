package ru.nesterov.calendar.integration.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.PrimaryEventData;
import ru.nesterov.calendar.integration.exception.UnknownEventColorIdIntegrationException;
import ru.nesterov.calendar.integration.service.EventStatusService;

import java.util.List;

@Service
public class EventStatusServiceImpl implements EventStatusService {
    private final List<String> successColorCodes;
    private final List<String> plannedCancelledColorCodes;
    private final List<String> requiresShiftColorCodes;
    private final List<String> plannedColorCodes;
    private final List<String> unplannedCancelledColorCodes;

    public EventStatusServiceImpl(@Value("${app.calendar.color.successful}") List<String> successColorCodes,
                                  @Value("${app.calendar.color.cancelled.planned}") List<String> plannedCancelledColorCodes,
                                  @Value("${app.calendar.color.cancelled.unplanned}") List<String> unplannedCancelledColorCodes,
                                  @Value("${app.calendar.color.requires.shift}") List<String> requiresShiftColorCodes,
                                  @Value("${app.calendar.color.planned}") List<String> plannedColorCodes) {

        boolean nullWasUsed = false;
        this.successColorCodes = successColorCodes;
        nullWasUsed = addNullCode(successColorCodes, nullWasUsed);
        
        this.plannedCancelledColorCodes = plannedCancelledColorCodes;
        nullWasUsed = addNullCode(plannedCancelledColorCodes, nullWasUsed);
        
        this.requiresShiftColorCodes = requiresShiftColorCodes;
        nullWasUsed = addNullCode(requiresShiftColorCodes, nullWasUsed);

        this.plannedColorCodes = plannedColorCodes;
        nullWasUsed = addNullCode(plannedColorCodes, nullWasUsed);

        this.unplannedCancelledColorCodes = unplannedCancelledColorCodes;
        nullWasUsed = addNullCode(unplannedCancelledColorCodes, nullWasUsed);

    }

    public EventStatus getEventStatus(PrimaryEventData primaryEventData) {
        if (successColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.SUCCESS;
        } else if (plannedColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.PLANNED;
        } else if (plannedCancelledColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.PLANNED_CANCELLED;
        } else if (requiresShiftColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.REQUIRES_SHIFT;
        } else if (unplannedCancelledColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.UNPLANNED_CANCELLED;
        }

        throw new UnknownEventColorIdIntegrationException(primaryEventData.getColorId(), primaryEventData.getName(), primaryEventData.getEventStart());
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
}
