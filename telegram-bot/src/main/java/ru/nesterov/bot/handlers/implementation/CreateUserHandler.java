package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetUserRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Процесс создания нового клиента для зарегистрированного пользователя
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateUserHandler extends DisplayedCommandHandler {
    private final BotHandlersRequestsKeeper keeper;

    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = TelegramUpdateUtils.getChatId(update);
        long userId = TelegramUpdateUtils.getUserId(update);
        String text = null;
        if (update.getMessage() != null) {
            text = update.getMessage().getText();
        }

        CreateUserRequest createUserRequest = keeper.getRequest(userId, CreateUserHandler.class, CreateUserRequest.class);

        if (getCommand().equals(text)) {
            CreateUserRequest newRequest = CreateUserRequest.builder().build();
            keeper.putRequest(CreateUserHandler.class, userId, newRequest);
            return handleRegisterCommand(userId, chatId);
        } else if (createUserRequest != null && createUserRequest.getMainCalendarId() == null) {
            return handleMainCalendarInput(createUserRequest, update);
        } else if (createUserRequest != null && createUserRequest.getIsCancelledCalendarEnabled() == null) {
            return handleCancelledCalendarEnabledInput(update, createUserRequest);
        } else if (createUserRequest != null && createUserRequest.getCancelledCalendarId() == null && createUserRequest.getIsCancelledCalendarEnabled()){
            return handleCancelledCalendarInput(createUserRequest, update);
        }

        log.info("CreateUserHandler cannot handle this update [{}]", update);
        throw new RuntimeException("CreateUserHandler cannot handle this update");
    }

    private String getButtonCallbackValue(Update update) {
        String telegramCallbackString = update.getCallbackQuery().getData();
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(telegramCallbackString);

        return buttonCallback.getValue();
    }

    @SneakyThrows
    private BotApiMethod<?> handleCancelledCalendarEnabledInput(Update update, CreateUserRequest createUserRequest) {
        createUserRequest.setIsCancelledCalendarEnabled(Boolean.valueOf(getButtonCallbackValue(update)));

        long chatId = TelegramUpdateUtils.getChatId(update);
        if (createUserRequest.getIsCancelledCalendarEnabled()) {
            return getPlainSendMessage(chatId, "Введите ID календаря с отмененными мероприятиями:");
        } else {
            return registerUser(update, createUserRequest);
        }
    }

    private BotApiMethod<?> handleRegisterCommand(long userId, long chatId) {
        if (isUserExists(String.valueOf(userId))) {
            return getPlainSendMessage(chatId, "Введите ID основного календаря:");
        } else {
            return getPlainSendMessage(chatId, "Вы уже зарегистрированы и можете пользоваться функциями бота");
        }
    }

    private BotApiMethod<?> handleMainCalendarInput(CreateUserRequest createUserRequest, Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        createUserRequest.setUserIdentifier(String.valueOf(userId));
        createUserRequest.setMainCalendarId(update.getMessage().getText());

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Вы хотите сохранять информацию об отмененных мероприятиях с использованием второго календаря?", keyboardMarkup);
    }

    private BotApiMethod<?> handleCancelledCalendarInput(CreateUserRequest createUserRequest, Update update) {
        createUserRequest.setCancelledCalendarId(update.getMessage().getText());
        return registerUser(update, createUserRequest);
    }

    private boolean isUserExists(String userIdentifier) {
        GetUserRequest request = new GetUserRequest();
        request.setUsername(userIdentifier);

        return client.getUserByUsername(request) == null;
    }

    private BotApiMethod<?> registerUser(Update update, CreateUserRequest createUserRequest) {
        CreateUserResponse response = client.createUser(createUserRequest);
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), formatCreateUserResponse(response));
    }

    private String formatCreateUserResponse(CreateUserResponse createUserResponse) {
        return String.join(System.lineSeparator(),
                "Вы успешно зарегистрированы! ",
                "ID пользователя: " + createUserResponse.getUserIdentifier(),
                "ID основного календаря: " + createUserResponse.getMainCalendarId(),
                "ID календаря с отмененными мероприятиями: " + createUserResponse.getCancelledCalendarId());
    }

    @Override
    public String getCommand() {
        return "Зарегистрироваться в боте";
    }

    @Override
    public boolean isFinished(Long userId) {
        if (!isUserExists(String.valueOf(userId))) {
            return true;
        }

        CreateUserRequest createUserRequest = keeper.getRequest(userId, CreateUserHandler.class, CreateUserRequest.class);
        return createUserRequest != null && createUserRequest.isFilled();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
