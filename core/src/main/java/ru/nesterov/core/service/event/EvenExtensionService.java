package ru.nesterov.core.service.event;

import ru.nesterov.calendar.integration.dto.EventDto;

public class EvenExtensionService {
    public static boolean isPlannedStatus(EventDto eventDto) {
        if (eventDto.getEventExtensionDto() == null) {
            return true;
        }

        return eventDto.getEventExtensionDto().getIsPlanned() == null || eventDto.getEventExtensionDto().getIsPlanned();
    }
}
