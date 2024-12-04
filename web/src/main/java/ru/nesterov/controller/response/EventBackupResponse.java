package ru.nesterov.controller.response;

import lombok.Data;

@Data
public class EventBackupResponse {
    private Integer savedEventsCount;
    private Boolean isBackupMade;
}
