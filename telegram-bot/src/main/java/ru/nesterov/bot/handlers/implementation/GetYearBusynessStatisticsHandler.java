package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.GetActiveClientResponse;
import ru.nesterov.dto.GetClientScheduleRequest;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.dto.GetYearBusynessStatisticsRequest;
import ru.nesterov.dto.GetYearBusynessStatisticsResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class GetYearBusynessStatisticsHandler extends ClientRevenueAbstractHandler {
    private final BotHandlersRequestsKeeper handlersKeeper;

    public GetYearBusynessStatisticsHandler(ObjectMapper objectMapper, ClientRevenueAnalyzerIntegrationClient client, BotHandlersRequestsKeeper handlersKeeper) {
//        super(objectMapper, client);
        this.handlersKeeper = handlersKeeper;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        String text = update.getMessage().getText();
        long userId = getUserId(update);
        GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest = handlersKeeper.getRequest(userId, GetYearBusynessStatisticsHandler.class, GetYearBusynessStatisticsRequest.class);
        long chatId = getChatId(update);
        if ("/getyearbusynessstatistics".equals(text)) {
            return getPlainSendMessage(chatId, "Введите год для анализа занятости");
        } else if (getYearBusynessStatisticsRequest == null) {
            return handleYearInput(update);
        } else {
            return sendYearStatistics(update, getYearBusynessStatisticsRequest);
        }
    }

    private BotApiMethod<?> handleYearInput(Update update) {
        long userId = getUserId(update);
        int year;

        try {
            year = Integer.parseInt(update.getMessage().getText());
        } catch (NumberFormatException e) {
            return getPlainSendMessage(update.getMessage().getChatId(), "Пожалуйста, введите корректный год.");
        }
        GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest = GetYearBusynessStatisticsRequest.builder()
                .userId(userId)
                .year(year)
                .build();

        handlersKeeper.putRequest(GetYearBusynessStatisticsHandler.class, userId, getYearBusynessStatisticsRequest);

        return getPlainSendMessage(update.getMessage().getChatId(), "Идет анализ");
    }


    @Override
    public boolean isFinished(Long userId) {
        GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest = handlersKeeper.getRequest(userId, GetYearBusynessStatisticsHandler.class, GetYearBusynessStatisticsRequest.class);
        return getYearBusynessStatisticsRequest != null;
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

        return "Анализ занятости за год:" +
                String.format(String.valueOf(response));
    }

    private Long getUserId(Update update) {
        return update.hasMessage() ? update.getMessage().getFrom().getId() : update.getCallbackQuery().getFrom().getId();
    }

    private Long getChatId(Update update) {
        return update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    public String getCommand() {
        return "/getyearbusynessstatistics";
    }
}
