package ru.nesterov.bot.statemachine.action.implementation.callback;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

import java.util.List;

@Component
public class BooleanCallbackActionResolver extends CallbackActionResolver {
    private final List<String> trueAliases = List.of("да", "true", "yes");
    private final List<String> falseAliases = List.of("нет", "false", "no");

    @Override
    protected Action resolveCallback(StatefulCommandHandler<?, ?> handler, Update update, String value) {
        Boolean booleanValue = getBooleanValue(value);

        if (booleanValue != null) {
            List<Action> expectedActions = handler.getStateMachine(update).getExpectedActions();

            if (expectedActions.contains(booleanValue ? Action.CALLBACK_TRUE : Action.CALLBACK_FALSE)) {
                return booleanValue ? Action.CALLBACK_TRUE : Action.CALLBACK_FALSE;
            }
            if (expectedActions.contains(Action.ANY_CALLBACK_BOOLEAN)) {
                return Action.ANY_CALLBACK_BOOLEAN;
            }
        }

        return null;
    }

    @Nullable
    private Boolean getBooleanValue(String input) {
        if (trueAliases.contains(input)) {
            return true;
        } else if (falseAliases.contains(input)) {
            return false;
        }

        return null;
    }
}
