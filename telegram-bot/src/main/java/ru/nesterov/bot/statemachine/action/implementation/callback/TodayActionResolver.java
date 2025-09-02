package ru.nesterov.bot.statemachine.action.implementation.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

@Component
public class TodayActionResolver extends CallbackActionResolver {
    @Override
    protected Action resolveCallback(StatefulCommandHandler<?, ?> handler, Update update, String value) {
        if (value.equals("Today") && handler.getStateMachine(update).getExpectedActions().contains(Action.CALLBACK_TODAY)) {
            return Action.CALLBACK_TODAY;
        }

        return null;
    }
}
