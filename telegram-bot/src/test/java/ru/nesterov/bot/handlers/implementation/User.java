package ru.nesterov.bot.handlers.implementation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private long userId;
    private String mainCalendarId;
    private String cancelledCalendarId;

}
