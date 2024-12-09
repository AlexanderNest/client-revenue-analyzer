package ru.nesterov.service.dto;

import lombok.Builder;
import lombok.Data;
import ru.nesterov.entity.UserSettings;

@Builder
@Data
public class UserDto {
    private long id;
    private String username;
    private String mainCalendar;
    private String cancelledCalendar;
    private UserSettingsDto userSettings;
}
