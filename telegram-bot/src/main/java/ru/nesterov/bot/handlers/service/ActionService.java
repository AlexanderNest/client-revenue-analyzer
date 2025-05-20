package ru.nesterov.bot.handlers.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.statemachine.dto.Action;

import java.util.List;

public class ActionService {
    private final String command;
    private final List<String> trueAliases = List.of("да", "true", "yes");
    private final List<String> falseAliases = List.of("нет", "false", "no");

    public ActionService(String command) {
        this.command = command;
    }

    public Action defineTheAction(Update update, List<Action> expectedActions) {
        if (update.getMessage() != null && update.getMessage().getText() != null) {
            String text = update.getMessage().getText();

            if (text.equals(command)) {
                if (expectedActions.contains(Action.COMMAND_INPUT)) {
                    return Action.COMMAND_INPUT;
                }
            }

            boolean isBoolean = false;
            if (trueAliases.contains(text.toLowerCase())) {
                isBoolean = true;
                if (expectedActions.contains(Action.TRUE)) {
                    return Action.TRUE;
                }
            }
            if (falseAliases.contains(text.toLowerCase())) {
                isBoolean = true;
                if (expectedActions.contains(Action.FALSE)) {
                    return Action.FALSE;
                }
            }

            if (isBoolean && expectedActions.contains(Action.ANY_BOOLEAN)) {
                return Action.ANY_BOOLEAN;
            }
            if (text.matches("[0-9]+") && expectedActions.contains(Action.ANY_NUMBER)) {
                return Action.ANY_NUMBER;
            }
            if (expectedActions.contains(Action.ANY_STRING)) {
                return Action.ANY_STRING;
            }
        }
        if (update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null && expectedActions.contains(Action.ANY_CALLBACK_INPUT)) {
            return Action.ANY_CALLBACK_INPUT;
        }

        throw new IllegalArgumentException("Cannot define the action");
    }
}
