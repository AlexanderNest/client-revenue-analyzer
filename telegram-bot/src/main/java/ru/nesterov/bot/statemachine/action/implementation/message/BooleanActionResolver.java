package ru.nesterov.bot.statemachine.action.implementation.message;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

import java.util.List;

@Component
public class BooleanActionResolver extends MessageActionResolver {
    private final List<String> trueAliases = List.of("да", "true", "yes");
    private final List<String> falseAliases = List.of("нет", "false", "no");

    @Override
    public Action resolve(StatefulCommandHandler<?, ?> handler, Update update) {
        String text = update.getMessage().getText();

        Boolean booleanValue = getBooleanValue(text.toLowerCase());
        List<Action> expectedActions = handler.getStateMachine(update).getExpectedActions();

        if (booleanValue != null) {
            if (expectedActions.contains(booleanValue ? Action.TRUE : Action.FALSE)) {
                return booleanValue ? Action.TRUE : Action.FALSE;
            }
            if (expectedActions.contains(Action.ANY_BOOLEAN)) {
                return Action.ANY_BOOLEAN;
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
