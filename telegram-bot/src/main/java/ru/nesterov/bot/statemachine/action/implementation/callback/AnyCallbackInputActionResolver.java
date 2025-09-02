package ru.nesterov.bot.statemachine.action.implementation.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

@Component
public class AnyCallbackInputActionResolver extends CallbackActionResolver {
    @Override
    protected Action resolveCallback(StatefulCommandHandler<?, ?> handler, Update update, String value) {
        if (handler.getStateMachine(update).getExpectedActions().contains(Action.ANY_CALLBACK_INPUT)) {
            return Action.ANY_CALLBACK_INPUT;
        }

        return null;
    }

    @Override
    public int getOrder() {
        return 199;
    }
}
