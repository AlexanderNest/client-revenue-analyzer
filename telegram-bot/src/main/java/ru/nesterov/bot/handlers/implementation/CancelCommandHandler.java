package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;

import java.util.Map;

@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
@Component
public class CancelCommandHandler extends InvocableCommandHandler {
    private final BotHandlersRequestsKeeper keeper;

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

//        Class<?> buf;
//        Map<Class<?>, Map<Long, Object>> requestMap = keeper.getMap();
//        for(Map.Entry<Class<?>, Map<Long, Object>> externalMap : requestMap.entrySet()) {
//            Map<Long, Object> internalMap = externalMap.getValue();
//            for (Map.Entry<Long, Object> entry : internalMap.entrySet()) {
//                if (userId == entry.getKey()) {
//                    buf = externalMap.getKey();
//                }
//            }
//            requestMap.remove(buf);
//        }

        keeper.removeRequest(CommandHandler.class, userId);
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
