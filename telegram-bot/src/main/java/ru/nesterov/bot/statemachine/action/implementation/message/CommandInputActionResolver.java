package ru.nesterov.bot.statemachine.action.implementation.message;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

@Component
public class CommandInputActionResolver extends MessageActionResolver {
    @Override
    public Action resolve(StatefulCommandHandler<?, ?> handler, Update update) {
        String text = update.getMessage().getText();

        if (handler.getCommand().equals(text) && handler.getStateMachine(update).getExpectedActions().contains(Action.COMMAND_INPUT)) {
            return Action.COMMAND_INPUT;
        }

        return null;
    }
}
