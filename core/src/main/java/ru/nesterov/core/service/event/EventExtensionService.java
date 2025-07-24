package ru.nesterov.core.service.event;

import ru.nesterov.calendar.integration.dto.EventDto;

public class EventExtensionService {
    public static boolean isPlannedStatus(EventDto eventDto) {
        if (eventDto.getEventExtensionDto() == null) {
            return false;
        }

        return eventDto.getEventExtensionDto().getIsPlanned() == null || eventDto.getEventExtensionDto().getIsPlanned();
    }
}
