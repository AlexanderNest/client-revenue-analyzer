package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.implementation.stateful.makeEventsBackupHandler.MakeEventsBackupHandler;
import ru.nesterov.dto.MakeEventsBackupRequest;
import ru.nesterov.dto.MakeEventsBackupResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        MakeEventsBackupHandler.class
})
class MakeEventsBackupHandlerTest extends RegisteredUserHandlerTest {
    @Autowired
    private MakeEventsBackupHandler handler;
    
    private String command;
    private static final long CHAT_ID = 1L;
    private static final long USER_ID = 1L;
    
    @Value("${app.calendar.events.backup.dates-range-for-backup}")
    private int datesRangeForBackup;
    
    @Value("${app.calendar.events.backup.delay-between-manual-backups}")
    private int delayBetweenManualBackups;
    
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
                .isBackupMade(true)
                .from(LocalDateTime.now().minusDays(datesRangeForBackup))
                .to(LocalDateTime.now().plusDays(datesRangeForBackup))
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
        message.setMessageId(1);
        
        Update update = new Update();
        update.setMessage(message);
        
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выполнить резервное копирование событий?", sendMessage.getText());
        
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
        String callbackData = buttonCallbackService.getTelegramButtonCallbackString(callback);
        callbackQuery.setData(callbackData);
        
        update.setCallbackQuery(callbackQuery);
        
        botApiMethod = handler.handle(update);
        EditMessageText editMessageText = (EditMessageText) botApiMethod;
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        assertEquals(
                String.format("Резервная копия событий (%d шт.) в период с %s по %s сохранена", response.getSavedEventsCount(), response.getFrom().format(dateTimeFormatter), response.getTo().format(dateTimeFormatter)),
                editMessageText.getText()
        );
    }
    
    @Test
    void handleAlreadyMadeBackup() {
        MakeEventsBackupRequest request = MakeEventsBackupRequest.builder()
                .isEventsBackupMade(true)
                .build();
        
        MakeEventsBackupResponse response = MakeEventsBackupResponse.builder()
                .isBackupMade(false)
                .cooldownMinutes(delayBetweenManualBackups)
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
        message.setMessageId(1);
        
        Update update = new Update();
        update.setMessage(message);
        
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выполнить резервное копирование событий?", sendMessage.getText());
        
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
        String callbackData = buttonCallbackService.getTelegramButtonCallbackString(callback);
        callbackQuery.setData(callbackData);
        
        update.setCallbackQuery(callbackQuery);
        
        botApiMethod = handler.handle(update);
        EditMessageText editMessageText = (EditMessageText) botApiMethod;
        
        assertEquals(
                String.format("Выполнить резервное копирование событий возможно по прошествии %d минут(ы)", delayBetweenManualBackups),
                editMessageText.getText()
        );
    }
    
    @Test
    void handleWithoutBackup() {
        MakeEventsBackupRequest request = MakeEventsBackupRequest.builder()
                .isEventsBackupMade(false)
                .build();
        
        MakeEventsBackupResponse response = MakeEventsBackupResponse.builder()
                .savedEventsCount(2)
                .isBackupMade(request.getIsEventsBackupMade())
                .from(LocalDateTime.now().minusDays(datesRangeForBackup))
                .to(LocalDateTime.now().plusDays(datesRangeForBackup))
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
        message.setMessageId(1);
        
        Update update = new Update();
        update.setMessage(message);
        
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выполнить резервное копирование событий?", sendMessage.getText());
        
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
        String callbackData = buttonCallbackService.getTelegramButtonCallbackString(callback);
        callbackQuery.setData(callbackData);
        
        update.setCallbackQuery(callbackQuery);
        
        botApiMethod = handler.handle(update);
        EditMessageText editMessageText = (EditMessageText) botApiMethod;
        
        assertEquals("Вы отказались от выполнения резервного копирования событий", editMessageText.getText());
    }
}
