package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.utils.MonthUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        GetMonthStatisticsCommandHandler.class,
        ObjectMapper.class
})
class GetMonthStatisticsHandlerTest {
    private static final String markSymbol = "\u2B50";
    @Autowired
    private GetMonthStatisticsCommandHandler handler;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;
    private static final String COMMAND = "Узнать доход";

    @Test
    void handleCallback() throws JsonProcessingException {
        GetIncomeAnalysisForMonthResponse response = new GetIncomeAnalysisForMonthResponse();
        response.setActualIncome(1000);
        response.setLostIncome(16200);
        response.setExpectedIncome(20000);
        response.setPotentialIncome(23000);

        when(client.getIncomeAnalysisForMonth(anyLong(), any())).thenReturn(response);

        Chat chat = new Chat();
        chat.setId(1L);

        Message message = new Message();
        message.setMessageId(1);
        message.setText(markSymbol + "august");
        message.setChat(chat);

        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        ButtonCallback callback = new ButtonCallback();
        callback.setCommand(COMMAND);
        callback.setValue(markSymbol + "august");
        callbackQuery.setMessage(message);
        callbackQuery.setData(objectMapper.writeValueAsString(callback));

        User user = new User();
        user.setId(1L);
        callbackQuery.setFrom(user);

        update.setCallbackQuery(callbackQuery);

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);
        EditMessageText editMessage = (EditMessageText) botApiMethod;

        String expected = "\uD83D\uDCCA *Анализ доходов за месяц*\n" +
                "\n" +
                "Фактический доход:          1 000 ₽\n" +
                "Ожидаемый доход:           20 000 ₽\n" +
                "-----------------------------\n" +
                "Потенциальный доход:       23 000 ₽\n" +
                "Потерянный доход:          16 200 ₽";

        assertEquals(expected, editMessage.getText());
    }

    @Test
    void handlePlainCommand() {
        Chat chat = new Chat();
        chat.setId(1L);

        Message message = new Message();
        message.setText(COMMAND);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        BotApiMethod<?> botApiMethod = handler.handle(update);

        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выберите месяц для анализа дохода:", sendMessage.getText());
        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);

        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;

        List<List<InlineKeyboardButton>> keyboard = inlineKeyboardMarkup.getKeyboard();

        assertEquals(4, keyboard.size());

        List<InlineKeyboardButton> firstQuarter = keyboard.get(0);
        List<InlineKeyboardButton> secondQuarter = keyboard.get(1);
        List<InlineKeyboardButton> thirdQuarter = keyboard.get(2);
        List<InlineKeyboardButton> fourthQuarter = keyboard.get(3);

        assertEquals(3, firstQuarter.size());
        assertEquals(3, secondQuarter.size());
        assertEquals(3, thirdQuarter.size());
        assertEquals(3, fourthQuarter.size());

        assertTrue(containsAllButtonsByTextWithMarkChecking(firstQuarter, List.of("Январь", "Февраль", "Март")));
        assertTrue(containsAllButtonsByTextWithMarkChecking(secondQuarter, List.of("Апрель", "Май", "Июнь")));
        assertTrue(containsAllButtonsByTextWithMarkChecking(thirdQuarter, List.of("Июль", "Август", "Сентябрь")));
        assertTrue(containsAllButtonsByTextWithMarkChecking(fourthQuarter, List.of("Октябрь", "Ноябрь", "Декабрь")));
    }

    private boolean containsAllButtonsByTextWithMarkChecking(List<InlineKeyboardButton> buttons, List<String> texts) {
        List<String> buttonsText = buttons.stream()
                .map(InlineKeyboardButton::getText)
                .toList();

        boolean checkWithoutMark = buttonsText.equals(texts);
        if (checkWithoutMark) {
            return true;
        }

        int currentMonthIndex = MonthUtil.getCurrentMonth() % 3;

        return buttonsText.get(currentMonthIndex).contains(markSymbol);
    }
}