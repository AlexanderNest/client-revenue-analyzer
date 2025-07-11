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

    public EventStatus getEventStatus(PrimaryEventData primaryEventData) {
        if (successColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.SUCCESS;
        } else if (plannedColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.PLANNED;
        } else if (cancelledColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.CANCELLED;
        } else if (requiresShiftColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.REQUIRES_SHIFT;
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
