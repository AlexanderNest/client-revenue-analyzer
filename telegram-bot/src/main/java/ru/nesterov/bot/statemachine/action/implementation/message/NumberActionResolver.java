package ru.nesterov.bot.statemachine.action.implementation.message;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

@Component
public class NumberActionResolver extends MessageActionResolver {
    @Override
    public Action resolve(StatefulCommandHandler<?, ?> handler, Update update) {
        String text = update.getMessage().getText();

        if (text.matches("[0-9]+") && handler.getStateMachine(update).getExpectedActions().contains(Action.ANY_NUMBER)) {
            return Action.ANY_NUMBER;
        }

        return null;
    }
}
