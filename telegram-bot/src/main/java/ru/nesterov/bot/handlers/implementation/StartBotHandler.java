package ru.nesterov.bot.handlers.implementation;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.nesterov.properties.BotProperties;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty("bot.enabled")
@RequiredArgsConstructor
public class StartBotHandler extends InvocableCommandHandler {
    private final List<InvocableCommandHandler> sendingMessageCommandHandlers;
    private final BotProperties botProperties;

    @Value("${bot.menu-buttons-per-line}")
    protected int buttonsCount;

    private final ReplyKeyboardMarkup buttons = new ReplyKeyboardMarkup();

    @PostConstruct
    private void buildButtons() {
        sendingMessageCommandHandlers.remove(this);
        buttons.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow currentRow = new KeyboardRow();
        for (int i = 0; i < sendingMessageCommandHandlers.size(); i++) {
            currentRow.add(new KeyboardButton(sendingMessageCommandHandlers.get(i).getCommand()));

            if ((i + 1) % botProperties.getMenuButtonsPerLine() == 0) {
                keyboardRows.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboardRows.add(currentRow);
        }

        buttons.setKeyboard(keyboardRows);
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = update.getMessage().getChatId().toString();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите опцию:");
        message.setReplyMarkup(buttons);

        return message;
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        return message != null && getCommand().equals(message.getText());
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
