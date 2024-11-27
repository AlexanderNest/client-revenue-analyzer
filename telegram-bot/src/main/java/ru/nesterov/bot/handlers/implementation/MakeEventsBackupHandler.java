package ru.nesterov.bot.handlers.implementation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.dto.MakeEventsBackupResponse;

@Component
@ConditionalOnProperty("bot.enabled")
public class MakeEventsBackupHandler extends ClientRevenueAbstractHandler {
    @Override
    public String getCommand() {
        return "/backup";
    }
    
    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = update.getMessage().getFrom().getId();
        MakeEventsBackupResponse response = client.makeEventsBackup(userId);
        
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText(response.getMessage());
        return message;
    }
    
    @Override
    public boolean isFinished(Long userId) {
        return true;
    }
}
