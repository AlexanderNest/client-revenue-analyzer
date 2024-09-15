package ru.nesterov.bot.handlers.callback;

import lombok.Data;

@Data
public class CalendarIntegrationCallback {
    private String command;
    private String value;
}
