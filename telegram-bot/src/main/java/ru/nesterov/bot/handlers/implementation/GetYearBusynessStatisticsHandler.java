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
        super(objectMapper, client);
        this.handlersKeeper = handlersKeeper;
    }

    //    @Override
//    public BotApiMethod<?> handle(Update update) {
//        long userId = getUserId(update);
//        GetYearBusynessStatisticsRequest request = handlersKeeper.getRequest(userId, GetYearBusynessStatisticsHandler.class, GetYearBusynessStatisticsRequest.class);
//        if (request == null) {
//        }
//    }
    @Override
    public BotApiMethod<?> handle(Update update) {
        BotApiMethod<?> sendMessage;
        long chatId = getChatId(update);
        if (update.getMessage() == null) {
            sendMessage = sendYearStatistics(update);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Введите год для анализа занятости:");
            sendMessage = message;
        }

        return sendMessage;
    }

    @SneakyThrows
    private BotApiMethod<?> sendYearStatistics(Update update, GetYearBusynessStatisticsRequest getYearBusynessStatisticsRequest) {
        long userId = getUserId(update);
        CallbackQuery callbackQuery = update.getCallbackQuery();
        GetYearBusynessStatisticsResponse response = client.getYearBusynessStatistics(
                userId, getYearBusynessStatisticsRequest.getYear());

        return editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                formatYearStatistics(response),
                null
        );
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
