package ru.nesterov.bot.statemachine.action.implementation.message;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.statemachine.action.implementation.AbstractActionResolver;

public abstract class MessageActionResolver extends AbstractActionResolver {
    @Override
    public boolean isApplicable(Update update) {
        return update.getMessage() != null && update.getMessage().getText() != null;
    }

    @Override
    public int getOrder() {
        return 250;   //  2XX будут сообщения по умолчанию. Их проверяем вторыми
    }
}
