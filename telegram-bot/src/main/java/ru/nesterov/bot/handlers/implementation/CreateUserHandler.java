package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.BotConfig;
import ru.nesterov.bot.handlers.AbstractHandler;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class CreateUserHandler extends AbstractHandler {
    private final Map<Long, CreateUserRequest> createUserRequests = new HashMap<>(); // надо сделать потокобезопасным
    private final ClientRevenueAnalyzerIntegrationClient client;
    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        CreateUserRequest createUserRequest = createUserRequests.get(update.getMessage().getFrom().getId());
        if (createUserRequest == null) {
            if (text == null) {
            return getPlainSendMessage(chatId, "Чтобы зарегистрироваться в Анализаторе клиентов, понадобится " +
                    "id основного календаря и календаря, в котором будут сохраняться отмененные мероприятия.\n\n Пришлите id основного календаря: " );
            } else {
                createUserRequest = new CreateUserRequest();
                createUserRequest.setMainCalendarId(text);
                createUserRequest.setUserIdentifier(String.valueOf(update.getMessage().getFrom().getId()));
                return getPlainSendMessage(chatId, "Пришлите id календаря, в котором будут храниться отмененные мероприятия: ");
            }
        } else {
            createUserRequest.setCancelledCalendarId(text);

            return registerUser(chatId, createUserRequest);
        }
    }

    // если это стринг просто
    private BotApiMethod<?> registerUser(long chatId, CreateUserRequest createUserRequest) {
        String response = client.createUser(createUserRequest);

        return getPlainSendMessage(chatId, response);
    }

    @Override
    @SneakyThrows
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();

        return message != null && "/register".equals(message.getText());
    }

    @Override
    public boolean isFinished(Long userId) {
        return createUserRequests.get(userId).getMainCalendarId() != null && createUserRequests.get(userId).getCancelledCalendarId() != null && createUserRequests.get(userId).getUserIdentifier() != null;
    }

}
