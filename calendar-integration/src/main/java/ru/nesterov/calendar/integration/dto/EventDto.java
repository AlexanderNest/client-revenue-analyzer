package ru.nesterov.calendar.integration.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class EventDto {
    private EventStatus status;
    private String summary;
    private LocalDateTime start;
    private LocalDateTime end;
    private EventExtensionDto eventExtensionDto;
}

