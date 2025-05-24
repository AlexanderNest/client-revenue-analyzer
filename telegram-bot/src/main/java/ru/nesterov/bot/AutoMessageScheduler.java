package ru.nesterov.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.service.user.UserServiceImpl;

import java.util.List;

@Component
@Slf4j
public class AutoMessageScheduler {
    @Autowired
    private RevenueAnalyzerBot revenueAnalyzerBot;
    @Autowired
    private UserServiceImpl userService;


    @Scheduled(cron = "0 24 21 * * ?")
    public void sendDailyNotification() {
        List<Long> usersId = userService.getAllUsersId();

        for (Long userId : usersId) {
            SendMessage message = new SendMessage();
            message.setChatId(userId.toString());
            message.setText("Это автоматическое сообщение.");
            try {
                revenueAnalyzerBot.execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки сообщения", e);
            }
        }
    }
}
