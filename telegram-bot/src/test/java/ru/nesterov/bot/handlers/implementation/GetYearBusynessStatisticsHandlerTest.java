package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.dto.GetYearBusynessStatisticsResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        GetYearBusynessStatisticsHandler.class,
        ObjectMapper.class,
        InlineCalendarBuilder.class,
        BotHandlersRequestsKeeper.class
})
class GetYearBusynessStatisticsHandlerTest {
    @Autowired
    private GetYearBusynessStatisticsHandler handler;
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;

    @Test
    void handle() {
        BotApiMethod<?> command = handler.handle(createUpdateWithMessage("/getyearbusynessstatistics"));
        assertInstanceOf(SendMessage.class, command);

        SendMessage sendMessage = (SendMessage) command;
        assertEquals("Введите год для расчета занятости", sendMessage.getText());

        BotApiMethod<?> wrongYearInput = handler.handle(createUpdateWithMessage("fff"));
        SendMessage wrongYear = (SendMessage) wrongYearInput;
        assertEquals("Введите корректный год", wrongYear.getText());

        Map<String, Double> months = new LinkedHashMap<>();
        months.put("Август", 10.25);

        Map<String, Double> days = new LinkedHashMap<>();
        days.put("Среда", 7.25);
        days.put("Понедельник", 3.0);

        GetYearBusynessStatisticsResponse getYearBusynessStatisticsResponse = new GetYearBusynessStatisticsResponse();
        getYearBusynessStatisticsResponse.setMonths(months);
        getYearBusynessStatisticsResponse.setDays(days);

        when(client.getYearBusynessStatistics(anyLong(), anyInt())).thenReturn(getYearBusynessStatisticsResponse);

        BotApiMethod<?> botApiMethod = handler.handle(createUpdateWithMessage("2024"));
        assertInstanceOf(SendMessage.class, botApiMethod);
        SendMessage sendStatistics = (SendMessage) botApiMethod;

        String expectedMessage = "Анализ занятости за год:\n\n" +
        "Занятость по месяцам:\n" +
        "Август - 10.25" + ("\n\n") +
        "Занятость по дням:\n" +
        "Среда - 7.25\n" +
        "Понедельник - 3.0";
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