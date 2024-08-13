package ru.nesterov.bot.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetMonthStatisticsHandler extends AbstractHandler {
    private final ObjectMapper objectMapper;
    private final ClientRevenueAnalyzerIntegrationClient client;


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
        GetIncomeAnalysisForMonthResponse response = client.getIncomeAnalysisForMonth(callback.getValue());

        return getPlainSendMessage(update.getCallbackQuery().getMessage().getChatId(), formatIncomeAnalysis(response));
    }

    private String formatIncomeAnalysis(GetIncomeAnalysisForMonthResponse response) {
        double actualIncome = response.getActualIncome();
        double expectedIncome = response.getExpectedIncoming();
        double lostIncome = response.getLostIncome();

        StringBuilder sb = new StringBuilder();
        sb.append("Анализ доходов за текущий месяц:\n\n");

        sb.append(String.format("✅      Фактический доход: %.2f ₽\n", actualIncome));
        sb.append(String.format("🔮      Ожидаемый доход: %.2f ₽\n", expectedIncome));
        sb.append(String.format("⚠️      Потерянный доход: %.2f ₽\n", lostIncome));

        return sb.toString();
    }

    @SneakyThrows
    private SendMessage sendMonthKeyboard(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите месяц для анализа дохода:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Массив с названиями месяцев
        String[] months = {"\u2B50 Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};

        // Создание строк с кнопками
        for (int i = 0; i < months.length; i += 3) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = i; j < i + 3 && j < months.length; j++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(months[j]);
                GetMonthStatisticsKeyboardCallback callback = new GetMonthStatisticsKeyboardCallback();
                callback.setValue(months[j].replace("\u2B50 ", ""));
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
