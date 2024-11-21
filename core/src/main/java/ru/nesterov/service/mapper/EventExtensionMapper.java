package ru.nesterov.service.mapper;

import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.entity.EventExtension;

public class EventExtensionMapper {
    public static EventExtensionDto mapToEventExtensionDto(EventExtension eventExtension) {
        EventExtensionDto eventExtensionDto = new EventExtensionDto();
        eventExtensionDto.setComment(eventExtension.getComment());
        eventExtensionDto.setIncome(eventExtension.getIncome());
        eventExtensionDto.setIsPlanned(eventExtension.getIsPlanned());
        eventExtensionDto.setPreviousDate(eventExtension.getPreviousDate());
        return eventExtensionDto;
    }
    
    public static EventExtension mapToEventExtension(EventExtensionDto eventExtensionDto) {
        if (eventExtensionDto == null) {
            return null;
        }
        
        EventExtension eventExtension = new EventExtension();
        eventExtension.setComment(eventExtensionDto.getComment());
        eventExtension.setIncome(eventExtensionDto.getIncome());
        eventExtension.setIsPlanned(eventExtensionDto.getIsPlanned());
        eventExtension.setPreviousDate(eventExtensionDto.getPreviousDate());
        return eventExtension;
    }
}
