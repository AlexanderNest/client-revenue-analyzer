package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.dto.GetUserResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.properties.BotProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Отображает кнопки для управления ботом
 */

@Component
@ConditionalOnProperty("bot.enabled")
@RequiredArgsConstructor
public class StartBotHandler extends InvocableCommandHandler {
    private final List<DisplayedCommandHandler> sendingMessageCommandHandlers;
    private final BotProperties botProperties;
    private final ClientRevenueAnalyzerIntegrationClient client;


    public ReplyKeyboardMarkup buildButtons(Update update) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        boolean isRegistered = isUserRegistered(update);
        int buttonsPerLine = botProperties.getMenuButtonsPerLine();

        List<KeyboardRow> keyboardRows = buildKeyboardRows(isRegistered, buttonsPerLine);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private boolean isUserRegistered(Update update) {
        GetUserRequest request = new GetUserRequest();
        request.setUsername(String.valueOf(TelegramUpdateUtils.getUserId(update)));
        GetUserResponse getUserResponse = client.getUserByUsername(request);
        return getUserResponse != null;
    }

    private List<KeyboardRow> buildKeyboardRows(boolean isRegistered, int buttonsPerLine) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();
        int buttonCount = 0;

        for (DisplayedCommandHandler handler : getRelevantHandlers(isRegistered)) {
            currentRow.add(new KeyboardButton(handler.getCommand()));
            buttonCount++;

            if (buttonCount % buttonsPerLine == 0) {
                rows.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        return rows;
    }

    private List<DisplayedCommandHandler> getRelevantHandlers(boolean isRegistered) {
        return sendingMessageCommandHandlers.stream()
                .filter(handler -> isRegistered ?
                        handler.isDisplayedForRegistered() :
                        handler.isDisplayedForUnregistered())
                .collect(Collectors.toList());
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        ReplyKeyboardMarkup controlButtons = buildButtons(update);
        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Выберите опцию:", controlButtons);
    }

    @Override
    public boolean isFinished(Long userId) {
        return true;
    }

    @Override
    public String getCommand() {
        return "/start";
    }
}
