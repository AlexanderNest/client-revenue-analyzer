package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.GetMonthStatisticsKeyboardCallback;
import ru.nesterov.dto.GetIncomeAnalysisForMonthResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.utils.MonthUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        GetMonthStatisticsHandler.class,
        ObjectMapper.class
})
class GetMonthStatisticsHandlerTest {
    private static final String markSymbol = "\u2B50";
    @Autowired
    private GetMonthStatisticsHandler handler;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;

    @BeforeEach
    void init() {

    }

    @Test
    void handleCallback() throws JsonProcessingException {
        GetIncomeAnalysisForMonthResponse response = new GetIncomeAnalysisForMonthResponse();
        response.setActualIncome(1000);
        response.setLostIncome(100);
        response.setExpectedIncoming(20000);

        when(client.getIncomeAnalysisForMonth(any())).thenReturn(response);


        Chat chat = new Chat();
        chat.setId(1L);

        Message message = new Message();
        message.setText(markSymbol + "august");
        message.setChat(chat);

        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        GetMonthStatisticsKeyboardCallback callback = new GetMonthStatisticsKeyboardCallback();
        callback.setCommand("/monthincome");
        callback.setValue(markSymbol + "august");
        callbackQuery.setMessage(message);
        callbackQuery.setData(objectMapper.writeValueAsString(callback));
        update.setCallbackQuery(callbackQuery);

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertTrue(botApiMethod instanceof SendMessage);
        SendMessage sendMessage = (SendMessage) botApiMethod;

        String expectedMessage = "Анализ доходов за текущий месяц:\n\n" +
                String.format("✅      Фактический доход: %.2f ₽\n", response.getActualIncome()) +
                String.format("🔮      Ожидаемый доход: %.2f ₽\n", response.getExpectedIncoming()) +
                String.format("⚠️      Потерянный доход: %.2f ₽\n", response.getLostIncome());

        assertEquals(expectedMessage, sendMessage.getText());
    }

    @Test
    void handlePlainCommand() {
        Chat chat = new Chat();
        chat.setId(1L);

        Message message = new Message();
        message.setText("/monthincome");
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        BotApiMethod<?> botApiMethod = handler.handle(update);

        assertTrue(botApiMethod instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выберите месяц для анализа дохода:", sendMessage.getText());
        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertTrue(markup instanceof InlineKeyboardMarkup);

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

        assertTrue(containsAllButtonsByTextWithMarkCheking(firstQuarter, List.of("Январь", "Февраль", "Март")));
        assertTrue(containsAllButtonsByTextWithMarkCheking(secondQuarter, List.of("Апрель", "Май", "Июнь")));
        assertTrue(containsAllButtonsByTextWithMarkCheking(thirdQuarter, List.of("Июль", "Август", "Сентябрь")));
        assertTrue(containsAllButtonsByTextWithMarkCheking(fourthQuarter, List.of("Октябрь", "Ноябрь", "Декабрь")));
    }

    private boolean containsAllButtonsByTextWithMarkCheking(List<InlineKeyboardButton> buttons, List<String> texts) {
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