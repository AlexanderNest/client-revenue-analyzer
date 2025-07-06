package ru.nesterov.core.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventBackupDto {
    private Integer savedEventsCount;
    private Boolean isBackupMade;
    private LocalDateTime from;
    private LocalDateTime to;
    private Integer cooldownMinutes;
}
