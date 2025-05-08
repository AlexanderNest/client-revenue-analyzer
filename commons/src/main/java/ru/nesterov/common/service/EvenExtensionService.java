package ru.nesterov.common.service;

import ru.nesterov.common.dto.EventDto;

public class EvenExtensionService {
    public static boolean isPlannedStatus(EventDto eventDto) {
        if (eventDto.getEventExtensionDto() == null) {
            return true;
        }

        return eventDto.getEventExtensionDto().getIsPlanned() == null || eventDto.getEventExtensionDto().getIsPlanned();
    }
}
