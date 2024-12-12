package ru.nesterov.bot.handlers.implementation;

import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.utils.MonthUtil;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

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
    
    private static String formatIncomeReport(GetIncomeAnalysisForMonthResponse response) {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
        
        
        return String.format(
                "📊 *Анализ доходов за месяц*\n\n" +
                        "%-22s %10s ₽\n" +
                        "%-22s %10s ₽\n" +
                        "-----------------------------\n" +
                        "%-22s %10s ₽\n" +
                        "%-22s %10s ₽",
                "Фактический доход:", currencyFormat.format(response.getActualIncome()),
                "Ожидаемый доход:", currencyFormat.format(response.getExpectedIncome()),
                "Потенциальный доход:", currencyFormat.format(response.getPotentialIncome()),
                "Потерянный доход:", currencyFormat.format(response.getLostIncome())
        );
    }
    
    @SneakyThrows
    private BotApiMethod<?> sendMonthStatistics(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        CallbackQuery callbackQuery = update.getCallbackQuery();
        ButtonCallback callback = ButtonCallback.fromShortString(callbackQuery.getData());
        GetIncomeAnalysisForMonthResponse response = client.getIncomeAnalysisForMonth(userId, clearFromMark(callback.getValue()));
        
        return editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                formatIncomeReport(response),
                null
        );
    }
    
    @SneakyThrows
    private SendMessage sendMonthKeyboard(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите месяц для анализа дохода:");
        
        String[] monthsWithMark = getArrayWithCurrentMonthMark();
        int keyboardLength = 3;
        int keyboardHeight = 4;
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[keyboardHeight][keyboardLength];
        for (int i = 0; i < keyboardHeight; i++) {
            for (int j = 0; j < keyboardLength; j++) {
                buttons[i][j] = buildButton(
                        monthsWithMark[(i * keyboardLength) + j],
                        clearFromMark(monthsWithMark[(i * keyboardLength) + j])
                );
            }
        }
        
        message.setReplyMarkup(buildInlineKeyboardMarkup(buttons));
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
    
    @Override
    public boolean isFinished(Long userId) {
        return true;
    }
}