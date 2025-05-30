package ru.nesterov.bot.handlers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.statemachine.dto.Action;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ActionService {
    private final List<String> trueAliases = List.of("да", "true", "yes");
    private final List<String> falseAliases = List.of("нет", "false", "no");

    @Autowired
    @Lazy
    private ButtonCallbackService buttonCallbackService;

    public Action defineTheAction(String command, Update update, List<Action> expectedActions) {
        if (update.getMessage() != null && update.getMessage().getText() != null) {
            String text = update.getMessage().getText();

            if (text.equals(command) && expectedActions.contains(Action.COMMAND_INPUT)) {
                return Action.COMMAND_INPUT;
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

        if (update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null && (expectedActions.contains(Action.CALLBACK_FALSE) || expectedActions.contains(Action.CALLBACK_TRUE))) { //TODO тут скорее всего надо сделать как в случае с обычными булинами
            ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
            boolean value = Boolean.parseBoolean(buttonCallback.getValue());

            if (value) {
                return Action.CALLBACK_TRUE;
            } else {
                return Action.CALLBACK_FALSE;
            }

            //TODO возможно тут надо добавить ANYCALLBACK
        }

        throw new IllegalArgumentException("Cannot define the action");
    }
}
