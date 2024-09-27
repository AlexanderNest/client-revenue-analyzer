package ru.nesterov.bot.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class HandlersService {
    private final List<CommandHandler> commandHandlers;

    @Nullable
    public CommandHandler getHandler(Update update) {
        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.isApplicable(update)) {
                return commandHandler;
            }
        }

        log.warn("Не удалось найти Handler для этого Update [{}]", update);
        return null;
    }
}