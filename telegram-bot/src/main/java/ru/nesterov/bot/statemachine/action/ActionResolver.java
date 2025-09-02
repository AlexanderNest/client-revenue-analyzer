package ru.nesterov.bot.statemachine.action;

import org.springframework.core.Ordered;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;

public interface ActionResolver extends Ordered {
    Action resolve(StatefulCommandHandler<?, ?> handler, Update update);
    boolean isApplicable(Update update);
}
