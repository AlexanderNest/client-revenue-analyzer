package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.dto.AiAnalyzerResponse;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class AiAnalyzerHandler extends DisplayedCommandHandler {
    @Override
    public String getCommand() {
        return "Анализ клиентов ИИ";
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        AiAnalyzerResponse response = client.getAiStatistics(userId);

        return getPlainSendMessage(update.getMessage().getChatId(), response.getContent());
    }

    @Override
    public boolean isFinished(Long userId) {
        return true;
    }
}
