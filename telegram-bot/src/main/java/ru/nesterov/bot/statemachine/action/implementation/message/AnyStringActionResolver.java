package ru.nesterov.bot.statemachine.action.implementation.message;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

@Component
public class AnyStringActionResolver extends MessageActionResolver {
    @Override
    public Action resolve(StatefulCommandHandler<?, ?> handler, Update update) {
        if (handler.getStateMachine(update).getExpectedActions().contains(Action.ANY_STRING)) {
            return Action.ANY_STRING;
        }

        return null;
    }

    @Override
    public int getOrder() {
        return 299;
    }
}
