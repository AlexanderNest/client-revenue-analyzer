package ru.nesterov.bot.handlers.callback;

import lombok.Data;

@Data
public class CreateNewUserCallback {
    private String command;
    private String value;
}
