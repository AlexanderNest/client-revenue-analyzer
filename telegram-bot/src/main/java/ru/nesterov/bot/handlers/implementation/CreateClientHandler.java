package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.dto.CreateClientRequest;
import ru.nesterov.dto.CreateClientResponse;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
@Component
public class CreateClientHandler extends DisplayedCommandHandler {
    private final BotHandlersRequestsKeeper keeper;

    @Override
    public String getCommand() {
        return "Добавить клиента";
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = TelegramUpdateUtils.getChatId(update);
        long userId = TelegramUpdateUtils.getUserId(update);
        String text = null;
        if (update.getMessage() != null) {
            text = update.getMessage().getText();
        }

        CreateClientRequest createClientRequest = keeper.getRequest(userId, CreateClientHandler.class, CreateClientRequest.class);

        if (getCommand().equals(text)) {
            CreateClientRequest newRequest = CreateClientRequest.builder().build();
            keeper.putRequest(CreateClientHandler.class, userId, newRequest);
            return handleCreateClientCommand(chatId);
        } else if (createClientRequest != null && createClientRequest.getName() == null) {
            return handleNameInput(update, createClientRequest);
        } else if (createClientRequest != null && createClientRequest.getPricePerHour() == null) {
            return handlePricePerHourInput(update, createClientRequest);
        } else if (createClientRequest != null && createClientRequest.getDescription() == null) {
            return handleDescriptionInput(update, createClientRequest);
        } else if (createClientRequest != null && createClientRequest.getPhone() == null) {
            return handlePhoneNumberInput(update, createClientRequest);
        } else if (createClientRequest != null && createClientRequest.getIdGenerationNeeded() == null) {
            return handleIdGenerationNeededInput(update, createClientRequest);
        }
        return null;
    }

    private BotApiMethod<?> handleCreateClientCommand(long chatId) {
        return getPlainSendMessage(chatId, "Введите имя");
    }

    private BotApiMethod<?> handleNameInput(Update update, CreateClientRequest createClientRequest) {
        createClientRequest.setName(update.getMessage().getText());

        long chatId = TelegramUpdateUtils.getChatId(update);
        return getPlainSendMessage(chatId, "Введите стоимость за час");
    }

    private BotApiMethod<?> handlePricePerHourInput(Update update, CreateClientRequest createClientRequest) {
        createClientRequest.setPricePerHour(Integer.parseInt(update.getMessage().getText()));

        long chatId = TelegramUpdateUtils.getChatId(update);
        return getPlainSendMessage(chatId, "Введите описание");
    }

    private BotApiMethod<?> handleDescriptionInput(Update update, CreateClientRequest createClientRequest) {
        createClientRequest.setDescription(update.getMessage().getText());

        long chatId = TelegramUpdateUtils.getChatId(update);
        return getPlainSendMessage(chatId, "Введите номер телефона");
    }

    private BotApiMethod<?> handlePhoneNumberInput(Update update, CreateClientRequest createClientRequest) {
        createClientRequest.setPhone(update.getMessage().getText());
        List<InlineKeyboardButton> buttons = List.of(
                buildButton("Да", "true", getCommand()),
                buildButton("Нет", "false", getCommand())
        );
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);
        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Включить генерацию нового имени, если клиент с таким именем уже существует?", keyboardMarkup);
    }

    private BotApiMethod<?> handleIdGenerationNeededInput(Update update, CreateClientRequest createClientRequest) {
        createClientRequest.setIdGenerationNeeded(Boolean.valueOf(getButtonCallbackValue(update)));

        return createClient(update, createClientRequest);
    }

    private String formatCreateClientResponse(CreateClientResponse response) {
        return String.join(System.lineSeparator(),
                "Клиент успешно зарегистрирован!",
                        "Имя: " + response.getName(),
                        "Стоимость за час: " + response.getPricePerHour(),
                        "Описание: " + response.getDescription(),
                        "Дата начала встреч: " + response.getStartDate(),
                        "Номер телефона: " + response.getPhone());
    }

    private BotApiMethod<?> createClient(Update update, CreateClientRequest createClientRequest) {
        long chatId = TelegramUpdateUtils.getChatId(update);
        CreateClientResponse response = client.createClient(String.valueOf(TelegramUpdateUtils.getUserId(update)), createClientRequest);
        if (response.getResponseCode() == 409) {
            return getPlainSendMessage(chatId, "Клиент с таким именем уже создан");
        }
        return getPlainSendMessage(chatId, formatCreateClientResponse(response));
    }


    @Override
    public boolean isFinished(Long userId) {
        CreateClientRequest request = keeper.getRequest(userId, CreateClientHandler.class, CreateClientRequest.class);
        return request != null && request.isFilled();
    }

    private String getButtonCallbackValue(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        ButtonCallback buttonCallback = ButtonCallback.fromShortString(callbackData);

        return buttonCallback.getValue();
    }
}
