package ru.nesterov.controller.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventBackupResponse {
    private Integer savedEventsCount;
    private Boolean isBackupMade;
    private LocalDateTime from;
    private LocalDateTime to;
    private Integer cooldownMinutes;
}
