package ru.nesterov.core.service.mapper;

import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.core.entity.Event;

public class EventMapper {
    public static EventDto mapToEventDto(Event event) {
        return EventDto.builder()
                .status(event.getStatus())
                .summary(event.getSummary())
                .start(event.getStart())
                .end(event.getEnd())
                .eventExtensionDto(EventExtensionMapper.mapToEventExtensionDto(event.getEventExtension()))
                .build();
    }
    
    public static Event mapToEvent(EventDto eventDto) {
        Event event = new Event();
        event.setStatus(eventDto.getStatus());
        event.setSummary(eventDto.getSummary());
        event.setStart(eventDto.getStart());
        event.setEnd(eventDto.getEnd());
        event.setEventExtension(EventExtensionMapper.mapToEventExtension(eventDto.getEventExtensionDto()));
        return event;
    }
}
