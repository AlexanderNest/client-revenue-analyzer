package ru.nesterov.service.dto;


import lombok.Builder;
import lombok.Data;
import ru.nesterov.entity.User;

@Builder
@Data
public class UserSettingsDto {
    private Long id;
    private boolean isCancelledCalendarEnabled;
    private boolean isEventsBackupEnabled;
}
