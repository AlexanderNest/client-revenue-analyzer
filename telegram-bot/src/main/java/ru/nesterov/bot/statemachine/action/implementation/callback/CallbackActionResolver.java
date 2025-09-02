package ru.nesterov.bot.statemachine.action.implementation.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.service.ButtonCallbackService;
import ru.nesterov.bot.statemachine.action.implementation.AbstractActionResolver;
import ru.nesterov.bot.statemachine.dto.Action;

public abstract class CallbackActionResolver extends AbstractActionResolver {
    @Autowired
    @Lazy
    protected ButtonCallbackService buttonCallbackService;

    @Override
    public Action resolve(StatefulCommandHandler<?, ?> handler, Update update) {
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
        String value = buttonCallback.getValue();

        return resolveCallback(handler, update, value);
    }

    protected abstract Action resolveCallback(StatefulCommandHandler<?, ?> handler, Update update, String value);

    @Override
    public boolean isApplicable(Update update) {
        return update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null;
    }

    @Override
    public int getOrder() {
        return 150;  //  1XX будут колбеки по умолчанию. Их проверяем первыми
    }
}
