package ru.nesterov.service.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserSettingsDto {
    private Long id;
    private boolean isCancelledCalendarEnabled;
    private boolean isEventsBackupEnabled;
}
