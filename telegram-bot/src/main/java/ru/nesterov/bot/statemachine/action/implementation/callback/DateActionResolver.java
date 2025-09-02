package ru.nesterov.bot.statemachine.action.implementation.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.statemachine.dto.Action;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class DateActionResolver extends CallbackActionResolver {

    @Override
    protected Action resolveCallback(StatefulCommandHandler<?, ?> handler, Update update, String value) {
        if (handler.getStateMachine(update).getExpectedActions().contains(Action.CALLBACK_DATE)) {
            return Action.CALLBACK_DATE;
        }

        return null;
    }

    @Override
    public boolean isApplicable(Update update) {
        return super.isApplicable(update) && isValidDate(update);
    }

    private boolean isValidDate(Update update) {
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
        String possibleDateStr = buttonCallback.getValue();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(possibleDateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
