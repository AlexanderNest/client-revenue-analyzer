package ru.nesterov.bot.handlers.implementation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.AbstractHandler;
import ru.nesterov.dto.CheckUserForExistenceRequest;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class CreateUserHandler extends AbstractHandler {
    private final Map<Long, CreateUserRequest> createUserRequests = new ConcurrentHashMap<>();
    private final ClientRevenueAnalyzerIntegrationClient client;

    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();

        CreateUserRequest createUserRequest = createUserRequests.get(userId);

        if (text.equals("/register")) {

            if (checkUserForExistence(String.valueOf(userId))) {
                return getPlainSendMessage(chatId, "Вы уже зарегистрированы и можете пользоваться функциями бота");
            } else {
                return getPlainSendMessage(chatId, "Введите ID основного календаря:");
            }

        } else if (createUserRequest == null) {
            createUserRequest = CreateUserRequest.builder()
                    .userIdentifier(String.valueOf(userId))
                    .mainCalendarId(text)
                    .build();

            createUserRequests.put(userId, createUserRequest);

            return getPlainSendMessage(chatId, "Вы хотите сохранять информацию об отмененных мероприятиях с использованием второго календаря?");
        } else if (createUserRequest.getIsCancelledCalendarEnabled() == null) {
            createUserRequest.setIsCancelledCalendarEnabled(Boolean.valueOf(text));

            if (createUserRequest.getIsCancelledCalendarEnabled()) {
                return getPlainSendMessage(chatId, "Введите ID клендаря с отмененными мероприятиями:");
            } else {
                return registerUser(chatId, createUserRequest);
            }

        } else {
            createUserRequest.setCancelledCalendarId(text);

            return registerUser(chatId, createUserRequest);
        }
    }

    private boolean checkUserForExistence(String userIdentifier) {
        CheckUserForExistenceRequest request = new CheckUserForExistenceRequest();
        request.setUserIdentifier(userIdentifier);

        return client.checkUserForExistence(request).isPresent();
    }

    private BotApiMethod<?> registerUser(long chatId, CreateUserRequest createUserRequest) {
        CreateUserResponse response = client.createUser(createUserRequest);

        return getPlainSendMessage(chatId, formatCreateUserResponse(response));
    }

    private String formatCreateUserResponse(CreateUserResponse createUserResponse) {
        return "Вы успешно зарегистрированы!\n\nUSER ID: " + createUserResponse.getUserIdentifier() +
                    "\n\nMAIN CALENDAR ID: " + createUserResponse.getMainCalendarId() +
                    "\n\nCANCELLED CALENDAR ID: " + createUserResponse.getCancelledCalendarId();
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();

        return message != null && "/register".equals(message.getText());
    }

    @Override
    public boolean isFinished(Long userId) {
        return checkUserForExistence(String.valueOf(userId));
    }
}
