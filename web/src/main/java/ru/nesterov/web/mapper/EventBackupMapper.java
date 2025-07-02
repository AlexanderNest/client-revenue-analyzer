package ru.nesterov.web.mapper;

import ru.nesterov.core.service.dto.EventBackupDto;
import ru.nesterov.web.controller.response.EventBackupResponse;

public class EventBackupMapper {
    public static EventBackupResponse mapToEventBackupResponse(EventBackupDto eventBackupDto) {
        EventBackupResponse response = new EventBackupResponse();
        response.setIsBackupMade(eventBackupDto.getIsBackupMade());
        response.setSavedEventsCount(eventBackupDto.getSavedEventsCount());
        response.setFrom(eventBackupDto.getFrom());
        response.setTo(eventBackupDto.getTo());
        response.setCooldownMinutes(eventBackupDto.getCooldownMinutes());
        return response;
    }
}
