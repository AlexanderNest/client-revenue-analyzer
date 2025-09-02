package ru.nesterov.bot.statemachine.action.implementation.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

import java.util.List;

@Component
public class NavigationButtonsActionResolver extends CallbackActionResolver {
    @Override
    protected Action resolveCallback(StatefulCommandHandler<?, ?> handler, Update update, String value) {
        List<Action> expectedActions = handler.getStateMachine(update).getExpectedActions();

        if (value.equals("Prev") && expectedActions.contains(Action.CALLBACK_PREV)) {
            return Action.CALLBACK_PREV;
        }

        if (value.equals("Next") && expectedActions.contains(Action.CALLBACK_NEXT)) {
            return Action.CALLBACK_NEXT;
        }

        return null;
    }
}
