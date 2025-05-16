package ru.nesterov.bot.handlers.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.statemachine.dto.Action;

public class ActionService {

    private final String command;

    public ActionService(String command) {
        this.command = command;
    }

    public Action defineTheAction(Update update) {

        if (update.getMessage() != null && update.getMessage().getText().equals(command)) {
            return Action.COMMAND_INPUT;
        } else if (update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null) {
            return Action.CALLBACK_INPUT;
        } else {
            return Action.ANY_STRING;
        }
    }
}
