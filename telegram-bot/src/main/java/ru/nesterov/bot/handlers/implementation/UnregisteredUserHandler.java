package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.SendingMessageCommandHandler;
import ru.nesterov.dto.GetUserRequest;

/**
 * Отлавливает незарегистрированного пользователя
 */

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class UnregisteredUserHandler extends SendingMessageCommandHandler {
    private final CreateUserHandler createUserHandler;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String message = "Воспользуйтесь командой '%s'".formatted(createUserHandler.getCommand());
        return getPlainSendMessage(TelegramUpdateUtils.getUserId(update), message);
    }

    @Override
    public boolean isFinished(Long userId) {
        return true;
    }

    @Override
    public boolean isApplicable(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        String text = null;
        if (update.getMessage() != null) {
            text = update.getMessage().getText();
        }

        return !(StringUtils.isNotBlank(text) && createUserHandler.getCommand().equals(text))
                && isUnregisteredUser(userId);
    }

    private boolean isUnregisteredUser(long userId) {
        GetUserRequest getUserRequest = new GetUserRequest();
        getUserRequest.setUsername(String.valueOf(userId));
        return client.getUserByUsername(getUserRequest) == null;
    }
}
