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
    private final List<String> promoColorCodes;

    public EventStatusServiceImpl(@Value("${app.calendar.color.successful}") List<String> successColorCodes,
                                  @Value("${app.calendar.color.cancelled.planned}") List<String> plannedCancelledColorCodes,
                                  @Value("${app.calendar.color.cancelled.unplanned}") List<String> unplannedCancelledColorCodes,
                                  @Value("${app.calendar.color.requires.shift}") List<String> requiresShiftColorCodes,
                                  @Value("${app.calendar.color.planned}") List<String> plannedColorCodes,
                                  @Value("${app.calendar.color.promo}") List<String> promoColorCodes) {

        boolean nullWasUsed = false;

        this.successColorCodes = successColorCodes;
        if (addNullCode(successColorCodes, nullWasUsed)) {
            nullWasUsed = true;
        }

        this.plannedCancelledColorCodes = plannedCancelledColorCodes;
        if (addNullCode(plannedCancelledColorCodes, nullWasUsed)) {
            nullWasUsed = true;
        }

        this.requiresShiftColorCodes = requiresShiftColorCodes;
        if (addNullCode(requiresShiftColorCodes, nullWasUsed)) {
            nullWasUsed = true;
        }

        this.plannedColorCodes = plannedColorCodes;
        if (addNullCode(plannedColorCodes, nullWasUsed)) {
            nullWasUsed = true;
        }

        this.unplannedCancelledColorCodes = unplannedCancelledColorCodes;
        if (addNullCode(unplannedCancelledColorCodes, nullWasUsed)) {
            nullWasUsed = true;
        }

        this.promoColorCodes = promoColorCodes;
        if (addNullCode(promoColorCodes, nullWasUsed)) {
            nullWasUsed = true;
        }
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
        } else if (promoColorCodes.contains(primaryEventData.getColorId())) {
            return EventStatus.PROMO;
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
