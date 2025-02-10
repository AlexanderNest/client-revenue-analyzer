package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.dto.AiAnalyzerRequest;
import ru.nesterov.dto.AiAnalyzerResponse;

import java.time.LocalDate;


@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class AiAnalyzerHandler extends DisplayedCommandHandler {
    private final BotHandlersRequestsKeeper handlersKeeper;

    @Override
    public String getCommand() {
        return "Анализ клиентов ИИ";
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        AiAnalyzerRequest aiAnalyzerRequest = handlersKeeper.getRequest(userId, AiAnalyzerHandler.class, AiAnalyzerRequest.class);


        if (aiAnalyzerRequest == null) {
            String currentMonth = LocalDate.now().getMonth().name().toLowerCase();

            AiAnalyzerRequest newAiAnalyzerRequest = AiAnalyzerRequest.builder()
                    .monthName(currentMonth)
                    .build();
            handlersKeeper.putRequest(AiAnalyzerHandler.class, userId, newAiAnalyzerRequest);

            return sendAiResponse(update, newAiAnalyzerRequest);
        } else {
            if (aiAnalyzerRequest.getMonthName() == null) {
                String currentMonth = LocalDate.now().getMonth().name().toLowerCase();

                aiAnalyzerRequest.setMonthName(currentMonth);
                handlersKeeper.putRequest(AiAnalyzerHandler.class, userId, aiAnalyzerRequest);
            }

            return sendAiResponse(update, aiAnalyzerRequest);
        }
    }

    @SneakyThrows
    private BotApiMethod<?> sendAiResponse(Update update, AiAnalyzerRequest aiAnalyzerRequest) {
        long userId = TelegramUpdateUtils.getUserId(update);

        AiAnalyzerResponse response = client.getAiStatistics(userId);

        return getPlainSendMessage(update.getMessage().getChatId(), response.getContent());
    }


    @Override
    public boolean isFinished(Long userId) {
        AiAnalyzerRequest aiAnalyzerRequest = handlersKeeper.getRequest(userId, AiAnalyzerHandler.class, AiAnalyzerRequest.class);
        return aiAnalyzerRequest == null || aiAnalyzerRequest.getMonthName() != null;
    }
}
