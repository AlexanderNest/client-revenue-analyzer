package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.dto.GetUserRequest;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class UnregisteredUserHandler extends ClientRevenueAbstractHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        return getPlainSendMessage(TelegramUpdateUtils.getUserId(update), "Воспользуйтесь командой Зарегистрироваться в боте");
    }

    @Override
    public boolean isFinished(Long userId) {
        return true;
    }

    @Override
    public boolean isApplicable(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        return isRegisteredUser(userId);
    }

    private boolean isRegisteredUser(long userId) {
        GetUserRequest getUserRequest = new GetUserRequest();
        getUserRequest.setUsername(String.valueOf(userId));
        return client.getUserByUsername(getUserRequest) != null;
    }

    @Override
    public String getCommand() {
        return null;
    }
}
