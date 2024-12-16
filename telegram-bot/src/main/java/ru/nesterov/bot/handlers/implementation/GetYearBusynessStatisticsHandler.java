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
import ru.nesterov.dto.GetYearBusynessStatisticsRequest;
import ru.nesterov.dto.GetYearBusynessStatisticsResponse;

import java.util.Locale;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class GetYearBusynessStatisticsHandler extends DisplayedCommandHandler {
    private final BotHandlersRequestsKeeper handlersKeeper;

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        long chatId = TelegramUpdateUtils.getChatId(update);

        GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest = handlersKeeper.getRequest(userId, GetYearBusynessStatisticsHandler.class, GetYearBusynessStatisticsRequest.class);

        if (getYearBusynessStatisticsRequest == null) {
            GetYearBusynessStatisticsRequest newGetYearBusynessStatisticsRequest = GetYearBusynessStatisticsRequest.builder().build();
            handlersKeeper.putRequest(GetYearBusynessStatisticsHandler.class, userId, newGetYearBusynessStatisticsRequest);
            return getPlainSendMessage(chatId, "Введите год для расчета занятости");
        } else if (getYearBusynessStatisticsRequest.getYear() == null) {
            return handleYearInput(update, getYearBusynessStatisticsRequest);
        } else {
            return sendYearStatistics(update, getYearBusynessStatisticsRequest);
        }
    }

    private BotApiMethod<?> handleYearInput(Update update, GetYearBusynessStatisticsRequest request) {
        int year;

        try {
            year = Integer.parseInt(update.getMessage().getText());
        } catch (NumberFormatException e) {
            return getPlainSendMessage(update.getMessage().getChatId(), "Введите корректный год");
        }

        request.setYear(year);
        return sendYearStatistics(update, request);
    }

    @SneakyThrows
    private BotApiMethod<?> sendYearStatistics(Update update, GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest) {
        long userId = TelegramUpdateUtils.getUserId(update);

        GetYearBusynessStatisticsResponse response = client.getYearBusynessStatistics(userId, getYearBusynessStatisticsRequest.getYear());

        return getPlainSendMessage(update.getMessage().getChatId(), formatYearStatistics(response));
    }

    private String formatYearStatistics(GetYearBusynessStatisticsResponse response) {
        if (response.getMonths().isEmpty()) {
            return "📅 Встречи не запланированы";
        }

        String monthHours = response.getMonths().entrySet().stream()
                .map(monthStatistics -> {
                    String monthName = monthStatistics.getKey();
                    Double hours = monthStatistics.getValue();
                    return String.format(Locale.US, "%s: %.2f ч.", monthName, hours);
                })
                .collect(Collectors.joining("\n"));

        String dayHours = response.getDays().entrySet().stream()
                .map(dayStatistics -> {
                    String dayName = dayStatistics.getKey();
                    Double hours = dayStatistics.getValue();
                    return String.format(Locale.US, "%s: %.2f ч.", dayName, hours);
                })
                .collect(Collectors.joining("\n"));

        return "📊 Анализ занятости за год:\n\n" +
                "🗓️ Занятость по месяцам:\n" + monthHours + "\n\n" +
                "📅 Занятость по дням недели:\n" + dayHours;
    }

    @Override
    public boolean isFinished(Long userId) {
        GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest = handlersKeeper.getRequest(userId, GetYearBusynessStatisticsHandler.class, GetYearBusynessStatisticsRequest.class);
        return getYearBusynessStatisticsRequest == null || getYearBusynessStatisticsRequest.getYear() != null;
    }

    @Override
    public String getCommand() {
        return "Анализ занятости за год";
    }
}
