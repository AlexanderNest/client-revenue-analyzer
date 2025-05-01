package ru.nesterov.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Builder
@Data
public class EventDto {

    private EventStatus status;
    private String summary;

    private LocalDateTime start;

    private LocalDateTime end;

    @EqualsAndHashCode.Exclude
    private EventExtensionDto eventExtensionDto;
}

