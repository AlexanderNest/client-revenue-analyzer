package ru.nesterov.service.mapper;

import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.entity.EventExtension;

public class EventExtensionMapper {
    public static EventExtensionDto mapToEventExtensionDto(EventExtension eventExtension) {
        EventExtensionDto eventExtensionDto = new EventExtensionDto();
        eventExtensionDto.setComment(eventExtension.getComment());
        eventExtensionDto.setIncome(eventExtension.getIncome());
        return eventExtensionDto;
    }
    
    public static EventExtension mapToEventExtension(EventExtensionDto eventExtensionDto) {
        EventExtension eventExtension = new EventExtension();
        
        if (eventExtensionDto != null) {
            eventExtension.setComment(eventExtensionDto.getComment());
            eventExtension.setIncome(eventExtensionDto.getIncome());
        }
        
        return eventExtension;
    }
}
