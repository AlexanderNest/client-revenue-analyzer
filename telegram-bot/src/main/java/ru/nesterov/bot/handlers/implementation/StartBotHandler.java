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
        ReplyKeyboardMarkup buttons = new ReplyKeyboardMarkup();

        buttons.setResizeKeyboard(true);
        GetUserRequest getUserRequest = new GetUserRequest();
        getUserRequest.setUsername(String.valueOf(TelegramUpdateUtils.getUserId(update)));
        GetUserResponse getUserResponse = client.getUserByUsername(getUserRequest);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow currentRow = new KeyboardRow();
        for (int i = 0; i < sendingMessageCommandHandlers.size(); i++) {
            if (getUserResponse != null) {
                if (sendingMessageCommandHandlers.get(i).isDisplayedForRegistered()) {
                    currentRow.add(new KeyboardButton(sendingMessageCommandHandlers.get(i).getCommand()));
                    if ((i + 1) % botProperties.getMenuButtonsPerLine() == 0) {
                        keyboardRows.add(currentRow);
                        currentRow = new KeyboardRow();
                    }
                }
            }
//            else {
//                if (sendingMessageCommandHandlers.get(i).isDisplayedForUnregistered()) {
//                    currentRow.add(new KeyboardButton(sendingMessageCommandHandlers.get(i).getCommand()));
//                    if ((i + 1) % botProperties.getMenuButtonsPerLine() == 0) {
//                        keyboardRows.add(currentRow);
//                        currentRow = new KeyboardRow();
//                    }
//                }
//            }
        }

        if (!currentRow.isEmpty()) {
            keyboardRows.add(currentRow);
        }

        buttons.setKeyboard(keyboardRows);
        return buttons;

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
