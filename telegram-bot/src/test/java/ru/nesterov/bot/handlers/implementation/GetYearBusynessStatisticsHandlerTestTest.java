package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.dto.GetYearBusynessStatisticsResponse;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        GetYearBusynessStatisticsHandler.class,
})
class GetYearBusynessStatisticsHandlerTestTest extends RegisteredUserHandlerTest {
    @Test
    void handle() {
        Update update = createUpdateWithMessage("Анализ занятости за год");
        CommandHandler commandHandler = handlerService.getHandler(update);

        BotApiMethod<?> command = commandHandler.handle(update);

        assertInstanceOf(SendMessage.class, command);

        SendMessage sendMessage = (SendMessage) command;
        assertEquals("Введите год для расчета занятости", sendMessage.getText());

        BotApiMethod<?> wrongYearInput = commandHandler.handle(createUpdateWithMessage("fff"));
        SendMessage wrongYear = (SendMessage) wrongYearInput;
        assertEquals("Введите корректный год", wrongYear.getText());

        Map<String, Double> months = new LinkedHashMap<>();
        months.put("Август", 10.2532133123);
        months.put("Июль", 20.0);

        Map<String, Double> days = new LinkedHashMap<>();
        days.put("Среда", 7.25);
        days.put("Понедельник", 3.0);

        GetYearBusynessStatisticsResponse getYearBusynessStatisticsResponse = new GetYearBusynessStatisticsResponse();
        getYearBusynessStatisticsResponse.setMonths(months);
        getYearBusynessStatisticsResponse.setDays(days);

        when(client.getYearBusynessStatistics(anyLong(), anyInt())).thenReturn(getYearBusynessStatisticsResponse);

        BotApiMethod<?> botApiMethod = commandHandler.handle(createUpdateWithMessage("2024"));
        assertInstanceOf(SendMessage.class, botApiMethod);
        SendMessage sendStatistics = (SendMessage) botApiMethod;

        String expectedMessage = "\uD83D\uDCCA Анализ занятости за год:\n" +
                "\n" +
                "\uD83D\uDDD3\uFE0F Занятость по месяцам:\n" +
                "Август: 10.25 ч.\n" +
                "Июль: 20.00 ч.\n" +
                "\n" +
                "\uD83D\uDCC5 Занятость по дням недели:\n" +
                "Среда: 7.25 ч.\n" +
                "Понедельник: 3.00 ч.";

        assertEquals(expectedMessage, sendStatistics.getText());
    }

    private Update createUpdateWithMessage(String text) {
        Chat chat = new Chat();
        chat.setId(1L);
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setText(text);
        message.setChat(chat);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }
}