package ru.nesterov.mapper;

import ru.nesterov.controller.response.EventBackupResponse;
import ru.nesterov.service.dto.EventBackupDto;

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
