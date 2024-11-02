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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
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
    void handleCommand() {

        Update update = createUpdateWithMessage();

        Chat chat = new Chat();
        chat.setId(1L);

        User user = new User();
        user.setId(1L);

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите год для расчета занятости", sendMessage.getText());
    }

    @Test
    void handle(){
        Map<String, Double> months = new HashMap<>();
        months.put("Август", 10.25);

        Map<String, Double> days = new HashMap<>();
        days.put("Среда", 7.25);
        days.put("Понедельник", 3.0);

        GetYearBusynessStatisticsResponse getYearBusynessStatisticsResponse = new GetYearBusynessStatisticsResponse();
        getYearBusynessStatisticsResponse.setMonths(months);
        getYearBusynessStatisticsResponse.setDays(days);

        when(client.getYearBusynessStatistics(anyLong(), anyInt())).thenReturn(getYearBusynessStatisticsResponse);
        String monthHours = getYearBusynessStatisticsResponse.getMonths().entrySet().stream()
                .map(monthStatistics -> {
                    String monthName = monthStatistics.getKey();
                    Double hours = monthStatistics.getValue();
                    return String.format("Занятость по месяцам:\n" + monthName + (" - ") + hours);
                }).collect(Collectors.joining("\n\n"));

        String dayHours =  getYearBusynessStatisticsResponse.getDays().entrySet().stream()
                .map(dayStatistics -> {
                    String dayName = dayStatistics.getKey();
                    Double hours = dayStatistics.getValue();
                    return String.format(dayName + (" - ") + hours);
                }).collect(Collectors.joining("\n"));
        Chat chat = new Chat();
        chat.setId(1L);

        User user = new User();
        user.setId(1L);
        Update update = new Update();
        Message message = new Message();
        message.setChat(chat);
        message.setText("2024");
        message.setFrom(user);
        update.setMessage(message);
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String expectedMessage = String.format("Анализ занятости за год:\n\n" +
                monthHours + ("\n\n") + "Занятость по дням:\n" +
                dayHours);
        assertEquals(expectedMessage, sendMessage.getText());
    }
    @Test
    void handleInvalidYear() {
        Chat chat = new Chat();
        chat.setId(1L);
        User user = new User();
        user.setId(1L);
        Update update = new Update();
        Message message = new Message();
        message.setChat(chat);
        message.setText("fff");
        message.setFrom(user);
        update.setMessage(message);
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String expectedMessage = "Введите корректный год";
        assertEquals(expectedMessage, sendMessage.getText());
    }

    private Update createUpdateWithMessage() {
        Chat chat = new Chat();
        chat.setId(1L);
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setText("/getyearbusynessstatistics");
        message.setChat(chat);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }
}