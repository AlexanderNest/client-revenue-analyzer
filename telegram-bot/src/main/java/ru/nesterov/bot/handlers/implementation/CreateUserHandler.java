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
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class CreateUserHandler extends AbstractHandler {
    private final Map<Long, CreateUserRequest> createUserRequests = new ConcurrentHashMap<>(); // надо сделать потокобезопасным
    private final ClientRevenueAnalyzerIntegrationClient client;
    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();
        CreateUserRequest createUserRequest = createUserRequests.get(userId);
        if (text.equals("/register")) {
            return getPlainSendMessage(chatId, "Чтобы зарегистрироваться в Анализаторе клиентов, понадобится " +
                    "id основного календаря и календаря, в котором будут сохраняться отмененные мероприятия.\n\n Пришлите id основного календаря: " );
        } else if (createUserRequest == null) {
            createUserRequest = CreateUserRequest.builder()
                    .userIdentifier(String.valueOf(userId))
                    .mainCalendarId(text)
                    .build();
            createUserRequests.put(userId, createUserRequest);

            return getPlainSendMessage(chatId, "Пришлите id календаря, в котором будут храниться отмененные мероприятия: ");
        } else {
            createUserRequest.setCancelledCalendarId(text);

            return registerUser(chatId, createUserRequest);
        }
    }

    private BotApiMethod<?> registerUser(long chatId, CreateUserRequest createUserRequest) {
        CreateUserResponse response = client.createUser(createUserRequest);

        return getPlainSendMessage(chatId, formatCreateUserResponse(response));
    }

    private String formatCreateUserResponse(CreateUserResponse createUserResponse) {
        return "Вы успешно зарегистрированы!\n\nUSER ID: " + createUserResponse.getUserId() +
                "\n\nMAIN CALENDAR ID: " + createUserResponse.getMainCalendarId() +
                "\n\nCANCELLED CALENDAR ID: " + createUserResponse.getCancelledCalendarId();
    }

    @Override
    @SneakyThrows
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();

        return message != null && "/register".equals(message.getText());
    }

    @Override
    public boolean isFinished(Long userId) {
        return createUserRequests.get(userId) != null && createUserRequests.get(userId).getMainCalendarId() != null && createUserRequests.get(userId).getCancelledCalendarId() != null && createUserRequests.get(userId).getUserIdentifier() != null;
    }

}
