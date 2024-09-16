package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.AbstractHandler;
import ru.nesterov.bot.handlers.CommandHandler;
import ru.nesterov.bot.handlers.callback.CreateNewUserCallback;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.entity.User;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class CreateUserHandler extends AbstractHandler {
    private final Map<Long, CreateUserRequest> createUserRequests = new HashMap<>(); // надо сделать потокобезопасным
    private final ObjectMapper objectMapper;
    private final ClientRevenueAnalyzerIntegrationClient client;
    private final UserRepository userRepository;
    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        CreateUserRequest createUserRequest = createUserRequests.get(update.getMessage().getFrom().getId());
        if (createUserRequest == null) {
            getPlainSendMessage(chatId, "")
        }
    }

    private SendMessage registerUser(long chatId, String googleCalendarId) {
        String userId = String.valueOf(chatId);
        User existingUser = userRepository.findUserByTelegramId(userId);
        if (existingUser != null) {
            return createResponseMessage(chatId, "Вы уже зарегистрированы.");
        }
        User user = User.builder()
                    .telegramId(String.valueOf(chatId))
                    .mainCalendarId(googleCalendarId)
                    .build();
        userRepository.save(user);
        return createResponseMessage(chatId, "Вы успешно зарегистрированы!");
    }

    private SendMessage createResponseMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    private SendMessage requireNewUserData(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Чтобы зарегистрироваться в Анализаторе клиентов, пришлите id вашего Google календаря:");
        return message;
    }

    @Override
    @SneakyThrows
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();

        return message != null && "/register".equals(message.getText());
    }

    @Override
    public boolean isFinished(Long userId) {
        return createUserRequests.get(userId).getMainCalendarId() != null && createUserRequests.get(userId).getCancelledCalendarId() != null &&
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }
}
