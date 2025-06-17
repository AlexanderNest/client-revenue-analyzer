package ru.nesterov.service.event;

import ru.nesterov.dto.EventDto;

public class EvenExtensionService {
    public static boolean isPlannedStatus(EventDto eventDto) {
        if (eventDto.getEventExtensionDto() == null) {
            return true;
        }

        return eventDto.getEventExtensionDto().getIsPlanned() == null || eventDto.getEventExtensionDto().getIsPlanned();
    }
}
