package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.utils.MonthUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty("bot.enabled")
public class GetMonthStatisticsHandler extends ClientRevenueAbstractHandler {
    private static final String[] months = {
            "Январь", "Февраль",
            "Март", "Апрель", "Май",
            "Июнь", "Июль", "Август",
            "Сентябрь", "Октябрь", "Ноябрь",
            "Декабрь"
    };

    private static final String markSymbol = "\u2B50";

    public GetMonthStatisticsHandler(ObjectMapper objectMapper, ClientRevenueAnalyzerIntegrationClient client) {
        super(objectMapper, client);
    }

    @Override
    public List<BotApiMethod<?>> handle(Update update) {
        List<BotApiMethod<?>> sendMessage = new ArrayList<>();
        if (update.getMessage() == null) {
            sendMessage.addAll(sendMonthStatistics(update));
        } else {
            sendMessage.add(sendMonthKeyboard(update.getMessage().getChatId()));
        }

        return sendMessage;
    }

    @SneakyThrows
    private List<BotApiMethod<?>> sendMonthStatistics(Update update) {
        long userId = update.getMessage().getFrom().getId();
        CallbackQuery callbackQuery = update.getCallbackQuery();
        ButtonCallback callback = objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class);
        GetIncomeAnalysisForMonthResponse response = client.getIncomeAnalysisForMonth(userId, clearFromMark(callback.getValue()));

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setText(formatIncomeAnalysis(response));

        return List.of(editMessageText);
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
                ButtonCallback callback = new ButtonCallback();
                callback.setValue(clearFromMark(monthsWithMark[j]));
                callback.setCommand(getCommand());
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
    public String getCommand() {
        return "/monthincome";
    }
}
