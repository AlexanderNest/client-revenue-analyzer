package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.dto.MakeEventsBackupRequest;
import ru.nesterov.dto.MakeEventsBackupResponse;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
//@ConditionalOnProperty("bot.enabled")
@RequiredArgsConstructor
@Slf4j
public class MakeEventsBackupHandler extends DisplayedCommandHandler {
    private final BotHandlersRequestsKeeper keeper;
    
    @Override
    public String getCommand() {
        return "Создать бэкап событий";
    }
    
    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        long chatId = TelegramUpdateUtils.getChatId(update);
        String text = null;

        if (update.getMessage() != null) {
            text = update.getMessage().getText();
        }
        
        MakeEventsBackupRequest makeEventsBackupRequest
                = keeper.getRequest(userId, MakeEventsBackupHandler.class, MakeEventsBackupRequest.class);
        
        if (getCommand().equals(text)) {
            MakeEventsBackupRequest newRequest = MakeEventsBackupRequest.builder().build();
            keeper.putRequest(MakeEventsBackupHandler.class, userId, newRequest);
            
            return requestConfirmation(chatId);
        } else if (makeEventsBackupRequest != null && makeEventsBackupRequest.getIsEventsBackupMade() == null) {
            return handleEventsBackupMadeInput(update, makeEventsBackupRequest);
        }
        
        log.info("MakeEventsBackupHandler cannot handle this update [{}]", update);
        throw new RuntimeException("MakeEventsBackupHandler cannot handle this update");
    }
    
    @Override
    public boolean isFinished(Long userId) {
        MakeEventsBackupRequest makeEventsBackupRequest
                = keeper.getRequest(userId, MakeEventsBackupHandler.class, MakeEventsBackupRequest.class);
        return makeEventsBackupRequest == null || makeEventsBackupRequest.getIsEventsBackupMade() != null;
    }
    
    private BotApiMethod<?> requestConfirmation(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(
                chatId,
                "Выполнить резервное копирование событий?",
                keyboardMarkup
        );
    }
    
    private BotApiMethod<?> handleEventsBackupMadeInput(Update update, MakeEventsBackupRequest makeEventsBackupRequest) {
        makeEventsBackupRequest.setIsEventsBackupMade(Boolean.valueOf(getButtonCallbackValue(update)));
        
        if (makeEventsBackupRequest.getIsEventsBackupMade()) {
            return makeEventsBackup(update);
        } else {
            long chatId = TelegramUpdateUtils.getChatId(update);
            return editMessage(
                    chatId,
                    TelegramUpdateUtils.getMessageId(update),
                    "Вы отказались от выполнения резервного копирования событий",
                    null);
        }
    }
    
    private String getButtonCallbackValue(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(callbackData);
        return buttonCallback.getValue();
    }
    
    private BotApiMethod<?> makeEventsBackup(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        long chatId = TelegramUpdateUtils.getChatId(update);
        MakeEventsBackupResponse response = client.makeEventsBackup(userId);
        
        String message;
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        if (response.getIsBackupMade()) {
            message = String.format(
                    "Резервная копия событий (%d шт.) в период с %s по %s сохранена",
                    response.getSavedEventsCount(),
                    response.getFrom().format(dateTimeFormatter),
                    response.getTo().format(dateTimeFormatter)
            );
        } else {
            message = String.format(
                    "Выполнить резервное копирование событий возможно по прошествии %d минут(ы)",
                    response.getCooldownMinutes()
            );
        }
        
        return editMessage(
                chatId,
                TelegramUpdateUtils.getMessageId(update),
                message,
                null);
    }
}
