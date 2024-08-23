package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.AbstractHandler;
import ru.nesterov.bot.handlers.callback.GetMonthStatisticsKeyboardCallback;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.utils.MonthUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class GetMonthStatisticsHandler extends AbstractHandler {
    private final ObjectMapper objectMapper;
    private final ClientRevenueAnalyzerIntegrationClient client;

    private static final String[] months = {
            "Январь", "Февраль",
            "Март", "Апрель", "Май",
            "Июнь", "Июль", "Август",
            "Сентябрь", "Октябрь", "Ноябрь",
            "Декабрь"
    };

    private static final String markSymbol = "\u2B50";

    @Override
    public BotApiMethod<?> handle(Update update) {
        BotApiMethod<?> sendMessage;
        if (update.getMessage() == null) {
            sendMessage = sendMonthStatistics(update);
        } else {
            sendMessage = sendMonthKeyboard(update.getMessage().getChatId());
        }

        return sendMessage;
    }

    @SneakyThrows
    private BotApiMethod<?> sendMonthStatistics(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        GetMonthStatisticsKeyboardCallback callback = objectMapper.readValue(callbackQuery.getData(), GetMonthStatisticsKeyboardCallback.class);
        GetIncomeAnalysisForMonthResponse response = client.getIncomeAnalysisForMonth(clearFromMark(callback.getValue()));

        return getPlainSendMessage(update.getCallbackQuery().getMessage().getChatId(), formatIncomeAnalysis(response));
    }

    private String formatIncomeAnalysis(GetIncomeAnalysisForMonthResponse response) {
        double actualIncome = response.getActualIncome();
        double expectedIncome = response.getExpectedIncoming();
        double lostIncome = response.getLostIncome();

        return "Анализ доходов за текущий месяц:\n\n" +
                String.format("✅      Фактический доход: %.2f ₽\n", actualIncome) +
                String.format("🔮      Ожидаемый доход: %.2f ₽\n", expectedIncome) +
                String.format("⚠️      Потерянный доход: %.2f ₽\n", lostIncome);
    }

    @SneakyThrows
    private SendMessage sendMonthKeyboard(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите месяц для анализа дохода:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String[] monthsWithMark = getArrayWithCurrentMonthMark();
        for (int i = 0; i < monthsWithMark.length; i += 3) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = i; j < i + 3 && j < monthsWithMark.length; j++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(monthsWithMark[j]);
                GetMonthStatisticsKeyboardCallback callback = new GetMonthStatisticsKeyboardCallback();
                callback.setValue(clearFromMark(monthsWithMark[j]));
                callback.setCommand("/monthincome");
                button.setCallbackData(objectMapper.writeValueAsString(callback));
                row.add(button);
            }
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }

    private String clearFromMark(String string) {
        return string.replace(markSymbol, "");
    }

    private String[] getArrayWithCurrentMonthMark() {
        String[] copy = Arrays.copyOf(months, months.length);

        int currentMonth = MonthUtil.getCurrentMonth();
        copy[currentMonth] = markSymbol + copy[currentMonth];

        return copy;
    }

    @Override
    @SneakyThrows
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        boolean isCommand = message != null && "/monthincome".equals(message.getText());

        CallbackQuery callbackQuery = update.getCallbackQuery();
        boolean isCallback = callbackQuery != null && "/monthincome".equals(objectMapper.readValue(callbackQuery.getData(), GetMonthStatisticsKeyboardCallback.class).getCommand());

        return isCommand || isCallback;
    }
}
