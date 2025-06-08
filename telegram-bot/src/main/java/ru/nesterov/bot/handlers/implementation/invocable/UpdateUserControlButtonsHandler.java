package ru.nesterov.bot.handlers.implementation.invocable;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.nesterov.bot.config.BotProperties;
import ru.nesterov.bot.dto.GetUserRequest;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.createUser.CreateUserHandler;
import ru.nesterov.bot.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Отображает кнопки для управления ботом
 */

@Component
@RequiredArgsConstructor
public class UpdateUserControlButtonsHandler extends InvocableCommandHandler {
    private final List<DisplayedCommandHandler> sendingMessageCommandHandlers;
    private final BotProperties botProperties;
    private final ClientRevenueAnalyzerIntegrationClient client;
    private final CreateUserHandler createUserHandler;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        boolean isRegistered = isRegisteredUser(TelegramUpdateUtils.getUserId(update));
        int buttonsPerLine = botProperties.getMenuButtonsPerLine();

        List<KeyboardRow> keyboardRows = buildKeyboardRows(isRegistered, buttonsPerLine);

        keyboardMarkup.setKeyboard(keyboardRows);
        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Выберите опцию:", keyboardMarkup);
    }

    @Override
    public boolean isApplicable(Update update) {
        return isUnregistered(update) || super.isApplicable(update);
    }

    private boolean isUnregistered(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        String text = null;
        if (update.getMessage() != null) {
            text = update.getMessage().getText();
        }

        return !(StringUtils.isNotBlank(text) && createUserHandler.getCommand().equals(text))
                && !isRegisteredUser(userId);
    }

    private List<KeyboardRow> buildKeyboardRows(boolean isRegistered, int buttonsPerLine) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();

        List<DisplayedCommandHandler> relevantHandlers = getRelevantHandlers(isRegistered);

        for (int buttonNumber = 0; buttonNumber < relevantHandlers.size(); buttonNumber++) {
            DisplayedCommandHandler handler = relevantHandlers.get(buttonNumber);
            currentRow.add(new KeyboardButton(handler.getCommand()));

            if (buttonNumber % buttonsPerLine == 0) {
                keyboardRows.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboardRows.add(currentRow);
        }

        return keyboardRows;
    }

    private boolean isRegisteredUser(long userId) {
        GetUserRequest request = new GetUserRequest();
        request.setUsername(String.valueOf(userId));
        return client.getUserByUsername(request) != null;
    }

    private List<DisplayedCommandHandler> getRelevantHandlers(boolean isRegistered) {
        return sendingMessageCommandHandlers.stream()
                .filter(handler -> isRegistered ? handler.isDisplayedForRegistered() : handler.isDisplayedForUnregistered())
                .collect(Collectors.toList());
    }
}
