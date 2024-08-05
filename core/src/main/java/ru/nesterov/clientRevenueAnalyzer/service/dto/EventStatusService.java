package ru.nesterov.clientRevenueAnalyzer.service.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nesterov.clientRevenueAnalyzer.dto.EventColor;

import java.util.List;

@Service
public class EventStatusService {
    private final List<String> successColorCodes;
    private final List<String> cancelledColorCodes;
    private final List<String> requiresShiftColorCodes;
    private final List<String> plannedColorCodes;

    public EventStatusService(@Value("${app.calendar.color.successful}") List<String> successColorCodes,
                              @Value("${app.calendar.color.cancelled}") List<String> cancelledColorCodes,
                              @Value("${app.calendar.color.requires.shift}") List<String> requiresShiftColorCodes,
                              @Value("${app.calendar.color.planned}") List<String> plannedColorCodes) {
        this.plannedColorCodes = plannedColorCodes;
        this.cancelledColorCodes = cancelledColorCodes;
        this.requiresShiftColorCodes = requiresShiftColorCodes;
        this.successColorCodes = successColorCodes;
    }


    public EventStatus getEventStatus(String eventColorId) {
        if (successColorCodes.contains(eventColorId)) {
            return EventStatus.SUCCESS;
        } else if (plannedColorCodes.contains(eventColorId)) {
            return EventStatus.PLANNED;
        } else if (cancelledColorCodes.contains(eventColorId)) {
            return EventStatus.CANCELLED;
        } else if (requiresShiftColorCodes.contains(eventColorId)) {
            return EventStatus.REQUIRES_SHIFT;
        }

        throw new RuntimeException("Unknown eventColorId: " + eventColorId);
    }
}
