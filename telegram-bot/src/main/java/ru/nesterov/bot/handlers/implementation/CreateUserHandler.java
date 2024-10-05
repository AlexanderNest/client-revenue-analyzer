package ru.nesterov.bot.handlers.implementation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetUserRequest;

@Component
@Data
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class CreateUserHandler extends ClientRevenueAbstractHandler {
    private final BotHandlersRequestsKeeper keeper;

    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();

        CreateUserRequest createUserRequest = keeper.getRequest(userId, CreateUserHandler.class, CreateUserRequest.class);

        if ("/register".equals(text)) {
            return handleRegisterCommand(userId, chatId);
        } else if (createUserRequest == null) {
            return handleMainCalendarInput(update);
        } else if (createUserRequest.getIsCancelledCalendarEnabled() == null) {
            return handleCancelledCalendarIdInput(update, createUserRequest);
        } else {
            return registerUser(update,createUserRequest);
        }
    }

    private BotApiMethod<?> handleCancelledCalendarIdInput(Update update, CreateUserRequest createUserRequest) {
        createUserRequest.setIsCancelledCalendarEnabled(Boolean.valueOf(update.getMessage().getText()));

        long chatId = update.getMessage().getChatId();
        if (createUserRequest.getIsCancelledCalendarEnabled()) {
            return getPlainSendMessage(chatId, "Введите ID календаря с отмененными мероприятиями:");
        } else {
            return registerUser(update, createUserRequest);
        }
    }

    private BotApiMethod<?> handleRegisterCommand(long userId, long chatId) {
        if (checkUserForExistence(String.valueOf(userId))) {
            return getPlainSendMessage(chatId, "Введите ID основного календаря:");
        } else {
            return getPlainSendMessage(chatId, "Вы уже зарегистрированы и можете пользоваться функциями бота");
        }
    }

    private BotApiMethod<?> handleMainCalendarInput(Update update) {
        long userId = update.getMessage().getFrom().getId();

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .userIdentifier(String.valueOf(userId))
                .mainCalendarId( update.getMessage().getText())
                .build();

        keeper.putRequest(CreateUserHandler.class, userId, createUserRequest);

        return getPlainSendMessage(update.getMessage().getChatId(), "Вы хотите сохранять информацию об отмененных мероприятиях с использованием второго календаря?");
    }

    private boolean checkUserForExistence(String userIdentifier) {
        GetUserRequest request = new GetUserRequest();
        request.setUsername(userIdentifier);

        return client.getUserByUsername(request) == null;
    }

    private BotApiMethod<?> registerUser(Update update, CreateUserRequest createUserRequest) {
        createUserRequest.setCancelledCalendarId(update.getMessage().getText());

        CreateUserResponse response = client.createUser(createUserRequest);
        return getPlainSendMessage(update.getMessage().getChatId(), formatCreateUserResponse(response));
    }

    private String formatCreateUserResponse(CreateUserResponse createUserResponse) {
        return String.join(System.lineSeparator(),
                "Вы успешно зарегистрированы!",
                " ",
                "ID пользователя: " + createUserResponse.getUserIdentifier(),
                "ID основного календаря: " + createUserResponse.getMainCalendarId(),
                "ID календаря с отмененными мероприятиями: " + createUserResponse.getCancelledCalendarId());
    }

    @Override
    public String getCommand() {
        return "/register";
    }

    @Override
    public boolean isFinished(Long userId) {
        CreateUserRequest createUserRequest = keeper.getRequest(userId, CreateUserHandler.class, CreateUserRequest.class);
        return createUserRequest != null && createUserRequest.isFilled();
    }
}
