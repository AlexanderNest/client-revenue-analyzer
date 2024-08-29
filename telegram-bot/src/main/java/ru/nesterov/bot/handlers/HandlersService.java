package ru.nesterov.bot.handlers;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.implementation.GetClientScheduleHandlerClientRevenue;
import ru.nesterov.bot.handlers.implementation.GetMonthStatisticsHandlerClientRevenue;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class HandlersService {
    private final GetMonthStatisticsHandlerClientRevenue getMonthStatisticsHandler;
    private final GetClientScheduleHandlerClientRevenue getClientScheduleHandler;

    private final List<CommandHandler> commandHandlers = new ArrayList<>();

    @PostConstruct
    private void init() {
        commandHandlers.add(getMonthStatisticsHandler);
        commandHandlers.add(getClientScheduleHandler);
    }

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
