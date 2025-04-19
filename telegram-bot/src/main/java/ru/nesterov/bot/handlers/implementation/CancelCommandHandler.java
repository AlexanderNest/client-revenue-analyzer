package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;

@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
@Component
public class CancelCommandHandler extends InvocableCommandHandler {
    private final BotHandlersRequestsKeeper keeper;
    @Override
    public BotApiMethod<?> handle(Update update) {
        keeper.removeContext();
        return null;
    }

    @Override
    public boolean isFinished(Long userId) {
        return false;
    }

    @Override
    public Priority getPriority() {
        return super.getPriority();
    }

    @Override
    public String getCommand() {
        return "/cancel";
    }
}
