package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.dto.GetYearBusynessStatisticsRequest;
import ru.nesterov.dto.GetYearBusynessStatisticsResponse;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetYearBusynessStatisticsHandler extends ClientRevenueAbstractHandler {
    private final BotHandlersRequestsKeeper handlersKeeper;

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = getUserId(update);
        long chatId = getChatId(update);

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
        long userId = getUserId(update);
        GetYearBusynessStatisticsResponse response = client.getYearBusynessStatistics(
                userId, getYearBusynessStatisticsRequest.getYear());

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
                    return String.format("Занятость по месяцам:\n" + monthName + (" - ") + hours);
                }).collect(Collectors.joining("\n\n"));

        String dayHours =  response.getDays().entrySet().stream()
                .map(dayStatistics -> {
                    String dayName = dayStatistics.getKey();
                    Double hours = dayStatistics.getValue();
                    return String.format(dayName + (" - ") + hours);
                }).collect(Collectors.joining("\n"));

        return "Анализ занятости за год:\n\n" +
                monthHours + ("\n\n") + "Занятость по дням:\n" +
                dayHours;
    }

    private Long getUserId(Update update) {
        return update.hasMessage() ? update.getMessage().getFrom().getId() : update.getCallbackQuery().getFrom().getId();
    }

    private Long getChatId(Update update) {
        return update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    public boolean isFinished(Long userId) {
        GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest = handlersKeeper.getRequest(userId, GetYearBusynessStatisticsHandler.class, GetYearBusynessStatisticsRequest.class);
        return getYearBusynessStatisticsRequest != null && getYearBusynessStatisticsRequest.getYear() != null;
    }

    @Override
    public String getCommand() {
        return "/getyearbusynessstatistics";
    }
}
