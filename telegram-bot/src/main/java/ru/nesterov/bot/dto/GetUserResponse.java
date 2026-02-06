package ru.nesterov.bot.dto;

import lombok.Builder;
import lombok.Data;
import ru.nesterov.core.entity.Role;

import java.io.Serializable;

@Data
@Builder
public class GetUserResponse implements Serializable {
    private long userId;
    private String username;
    private String mainCalendarId;
    private Boolean isCancelledCalendarEnabled;
    private String cancelledCalendarId;
    private Role role;
    private String source;
}
