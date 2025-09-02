package ru.nesterov.bot.statemachine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.action.ActionResolver;
import ru.nesterov.bot.statemachine.dto.Action;

import java.util.Comparator;
import java.util.List;

@Service
public class ActionService {
    private final List<ActionResolver> actionResolvers;

    public ActionService(List<ActionResolver> actionResolvers) {
        this.actionResolvers = actionResolvers.stream()
                .sorted(Comparator.comparing(ActionResolver::getOrder))
                .toList();
    }

    public Action defineTheAction(StatefulCommandHandler<?, ?> handler, Update update) {
        for (ActionResolver resolver : actionResolvers) {
            if (resolver.isApplicable(update)) {
                Action action = resolver.resolve(handler, update);
                if (action != null) {
                    return action;
                }
            }
        }

        throw new IllegalArgumentException("Cannot define the action");
    }

}