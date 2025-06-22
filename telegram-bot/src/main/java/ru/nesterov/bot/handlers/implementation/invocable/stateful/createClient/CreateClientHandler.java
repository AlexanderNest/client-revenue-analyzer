package ru.nesterov.bot.handlers.implementation.invocable.stateful.createClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.CreateClientRequest;
import ru.nesterov.bot.dto.CreateClientResponse;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.statemachine.dto.Action;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Процесс регистрации нового пользователя
 */
@Slf4j
@ConditionalOnProperty("bot.enabled")
@Component
public class CreateClientHandler extends StatefulCommandHandler<State, CreateClientRequest> {

    @Override
    public String getCommand() {
        return "Добавить клиента";
    }

    public CreateClientHandler() {
        super(State.STARTED, CreateClientRequest.class);
    }

    @Override
    public void initTransitions() {
        stateMachineProvider
                .addTransition(State.STARTED, Action.COMMAND_INPUT, State.NAME_INPUT, this::handleCreateClientCommand)
                .addTransition(State.NAME_INPUT, Action.ANY_STRING, State.PRICE_INPUT, this::handleNameInput)
                .addTransition(State.PRICE_INPUT, Action.ANY_STRING, State.DESCRIPTION_INPUT, this::handlePricePerHourInput)
                .addTransition(State.DESCRIPTION_INPUT, Action.ANY_STRING, State.NUMBER_INPUT, this::handleDescriptionInput)
                .addTransition(State.NUMBER_INPUT, Action.ANY_STRING, State.CLIENT_NAME_GENERATION_INPUT, this::handlePhoneNumberInput)
                .addTransition(State.CLIENT_NAME_GENERATION_INPUT, Action.CALLBACK_TRUE, State.FINISH, this::handleIdGenerationNeededInput)
                .addTransition(State.CLIENT_NAME_GENERATION_INPUT, Action.CALLBACK_FALSE, State.FINISH, this::createClient);
    }

    private BotApiMethod<?> handleCreateClientCommand(Update update) {
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите имя");
    }

    private BotApiMethod<?> handleNameInput(Update update) {
        getStateMachine(update).getMemory().setName(update.getMessage().getText());
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите стоимость за час");
    }

    private BotApiMethod<?> handlePricePerHourInput(Update update) {
        getStateMachine(update).getMemory().setPricePerHour(Integer.parseInt(update.getMessage().getText()));
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите описание");
    }

    private BotApiMethod<?> handleDescriptionInput(Update update) {
        getStateMachine(update).getMemory().setDescription(update.getMessage().getText());
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите номер телефона");
    }

    private BotApiMethod<?> handlePhoneNumberInput(Update update) {
        getStateMachine(update).getMemory().setPhone(update.getMessage().getText());
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);
        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Включить генерацию нового имени, если клиент с таким именем уже существует?", keyboardMarkup);
    }

    private BotApiMethod<?> handleIdGenerationNeededInput(Update update) {
        getStateMachine(update).getMemory().setIdGenerationNeeded(Boolean.valueOf(getButtonCallbackValue(update)));
        return createClient(update);
    }


    private BotApiMethod<?> createClient(Update update) {
        long chatId = TelegramUpdateUtils.getChatId(update);
        CreateClientResponse response = client.createClient(
                String.valueOf(TelegramUpdateUtils.getUserId(update)),
                getStateMachine(update).getMemory()
        );

        if (response.getResponseCode() == HttpStatus.CONFLICT.value()) {
            return getPlainSendMessage(chatId, response.getErrorMessage());
        }

        return getPlainSendMessage(chatId, formatCreateClientResponse(response));
    }


    private String formatCreateClientResponse(CreateClientResponse response) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return String.join(System.lineSeparator(),
                "Клиент успешно зарегистрирован!",
                "Имя: " + response.getName(),
                "Стоимость за час: " + response.getPricePerHour(),
                "Описание: " + response.getDescription(),
                "Дата начала встреч: " + formatter.format(response.getStartDate()),
                "Номер телефона: " + response.getPhone());
    }

    private String getButtonCallbackValue(Update update) {
        String telegramCallbackString = update.getCallbackQuery().getData();
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(telegramCallbackString);

        return buttonCallback.getValue();
    }

}

