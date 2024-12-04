package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.RegisteredUserHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.MakeEventsBackupRequest;
import ru.nesterov.dto.MakeEventsBackupResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        MakeEventsBackupHandler.class
})
class MakeEventsBackupHandlerTest extends RegisteredUserHandler {
    @Autowired
    private MakeEventsBackupHandler handler;
    
    private String command;
    private final long CHAT_ID = 1L;
    private final long USER_ID = 1L;
    
    @BeforeEach
    public void setUp() {
        command = handler.getCommand();
    }
    
    @Test
    void handle() {
        MakeEventsBackupRequest request = MakeEventsBackupRequest.builder()
                .isEventsBackupMade(true)
                .build();
        
        MakeEventsBackupResponse response = MakeEventsBackupResponse.builder()
                .savedEventsCount(2)
                .isBackupMade(request.getIsEventsBackupMade())
                .build();
        
        when(client.makeEventsBackup(anyLong())).thenReturn(response);
        
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        User user = new User();
        user.setId(USER_ID);
        
        Message message = new Message();
        message.setText(command);
        message.setChat(chat);
        message.setFrom(user);
        
        Update update = new Update();
        update.setMessage(message);
        
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выполнить бэкап событий?", sendMessage.getText());
        
        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);
        
        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;
        List<List<InlineKeyboardButton>> yesNoKeyboard = inlineKeyboardMarkup.getKeyboard();
        assertEquals(1, yesNoKeyboard.size());
        
        String firstButtonText = yesNoKeyboard.get(0).get(0).getText();
        String secondButtonText = yesNoKeyboard.get(0).get(1).getText();
        assertEquals("Да", firstButtonText);
        assertEquals("Нет", secondButtonText);
        
        message.setText("Да");
        
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        ButtonCallback callback = new ButtonCallback();
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        callback.setCommand(command);
        callback.setValue(request.getIsEventsBackupMade().toString());
        String callbackData = callback.toShortString();
        callbackQuery.setData(callbackData);
        
        update.setCallbackQuery(callbackQuery);
        
        botApiMethod = handler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        
        assertEquals("Событий сохранено: " + response.getSavedEventsCount(), sendMessage.getText());
    }
    
    @Test
    void handleWithoutBackup() {
        MakeEventsBackupRequest request = MakeEventsBackupRequest.builder()
                .isEventsBackupMade(true)
                .build();
        
        MakeEventsBackupResponse response = MakeEventsBackupResponse.builder()
                .savedEventsCount(2)
                .isBackupMade(request.getIsEventsBackupMade())
                .build();
        
        when(client.makeEventsBackup(anyLong())).thenReturn(response);
        
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        User user = new User();
        user.setId(USER_ID);
        
        Message message = new Message();
        message.setText(command);
        message.setChat(chat);
        message.setFrom(user);
        
        Update update = new Update();
        update.setMessage(message);
        
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выполнить бэкап событий?", sendMessage.getText());
        
        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);
        
        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;
        List<List<InlineKeyboardButton>> yesNoKeyboard = inlineKeyboardMarkup.getKeyboard();
        assertEquals(1, yesNoKeyboard.size());
        
        String firstButtonText = yesNoKeyboard.get(0).get(0).getText();
        String secondButtonText = yesNoKeyboard.get(0).get(1).getText();
        assertEquals("Да", firstButtonText);
        assertEquals("Нет", secondButtonText);
        
        message.setText("Нет");
        
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        ButtonCallback callback = new ButtonCallback();
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        callback.setCommand(command);
        callback.setValue(request.getIsEventsBackupMade().toString());
        String callbackData = callback.toShortString();
        callbackQuery.setData(callbackData);
        
        update.setCallbackQuery(callbackQuery);
        
        botApiMethod = handler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        
        assertEquals("Как хотите", sendMessage.getText());
    }
}
