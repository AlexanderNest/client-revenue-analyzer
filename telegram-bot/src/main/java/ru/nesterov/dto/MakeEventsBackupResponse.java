package ru.nesterov.dto;

import lombok.Data;

@Data
public class MakeEventsBackupResponse {
    private Integer savedEventsCount;
    private Boolean isBackupMade;
}
