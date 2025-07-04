package ru.nesterov.core.service.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {
    private long id;
    private String username;
    private String mainCalendar;
    private String cancelledCalendar;
    private boolean isCancelledCalendarEnabled;
}
