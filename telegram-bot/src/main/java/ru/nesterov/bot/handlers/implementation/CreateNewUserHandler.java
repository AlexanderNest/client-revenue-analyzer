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
import ru.nesterov.bot.handlers.CommandHandler;
import ru.nesterov.bot.handlers.callback.CreateNewUserCallback;
import ru.nesterov.bot.handlers.callback.GetMonthStatisticsKeyboardCallback;
import ru.nesterov.entity.User;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.repository.UserRepository;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class CreateNewUserHandler implements CommandHandler {
    private final ObjectMapper objectMapper;
    private final ClientRevenueAnalyzerIntegrationClient client;
    private final UserRepository userRepository;
    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String userData = update.getMessage().getText();
        if (userData != null && !userData.isEmpty()) {
            return registerUser(chatId, userData);
        } else {
            return requireNewUserData(chatId);
        }
    }

    private SendMessage registerUser(long chatId, String googleCalendarId) {
        String userId = String.valueOf(chatId);
        User existingUser = userRepository.findUserByTelegramId(userId);
        if (existingUser != null) {
            return createResponseMessage(chatId, "Вы уже зарегистрированы.");
        }
        User user = User.builder()
                    .active(true)
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
        boolean isCommand = message != null && "/createuser".equals(message.getText());

        CallbackQuery callbackQuery = update.getCallbackQuery();
        boolean isCallback = callbackQuery != null && "/createuser".equals(objectMapper.readValue(callbackQuery.getData(), CreateNewUserCallback.class).getCommand());

        return isCommand || isCallback;
    }
}
