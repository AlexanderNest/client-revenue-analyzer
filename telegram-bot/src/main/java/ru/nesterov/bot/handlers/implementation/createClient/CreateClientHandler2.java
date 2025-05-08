package ru.nesterov.bot.handlers.implementation.createClient;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.CreateClientRequest;
import ru.nesterov.dto.CreateClientResponse;
import ru.nesterov.statemachine.StateMachine;
import ru.nesterov.statemachine.dto.Action;
import ru.nesterov.statemachine.dto.NextStateFunction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class CreateClientHandler2 extends DisplayedCommandHandler {
    private final StateMachine<State, Action, BotApiMethod<?>, Update, CreateClientRequest> stateMachine = new StateMachine<>(State.STARTED, CreateClientRequest.builder().build());

    @PostConstruct
    private void initTransitions() {
        stateMachine
                .addTransition(State.STARTED, Action.COMMAND_INPUT, State.NAME_INPUT, this::handleCreateClientCommand)
                .addTransition(State.NAME_INPUT, Action.ANY_STRING, State.PRICE_INPUT, this::handleNameInput)
                .addTransition(State.PRICE_INPUT, Action.ANY_STRING, State.DESCRIPTION_INPUT, this::handlePricePerHourInput)
                .addTransition(State.DESCRIPTION_INPUT, Action.ANY_STRING, State.NUMBER_INPUT, this::handleDescriptionInput)
                .addTransition(State.NUMBER_INPUT, Action.ANY_STRING, State.CLIENT_NAME_GENERATION_INPUT, this::handlePhoneNumberInput)
                .addTransition(State.CLIENT_NAME_GENERATION_INPUT, Action.CALLBACK_INPUT, State.FINISH, this::handleIdGenerationNeededInput);
    }

    private BotApiMethod<?> handleCreateClientCommand(Update update) {
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите имя");
    }

    private BotApiMethod<?> handleNameInput(Update update) {
        stateMachine.getMemory().setName(update.getMessage().getText());
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите стоимость за час");
    }

    private BotApiMethod<?> handlePricePerHourInput(Update update) {
        stateMachine.getMemory().setPricePerHour(Integer.parseInt(update.getMessage().getText()));
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите описание");
    }

    private BotApiMethod<?> handleDescriptionInput(Update update) {
        stateMachine.getMemory().setDescription(update.getMessage().getText());
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите номер телефона");
    }

    private BotApiMethod<?> handlePhoneNumberInput(Update update) {
        stateMachine.getMemory().setPhone(update.getMessage().getText());
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
        stateMachine.getMemory().setIdGenerationNeeded(Boolean.valueOf(getButtonCallbackValue(update)));

        return createClient(update);
    }

    private BotApiMethod<?> createClient(Update update) {
        long chatId = TelegramUpdateUtils.getChatId(update);
        CreateClientResponse response = client.createClient(String.valueOf(TelegramUpdateUtils.getUserId(update)), stateMachine.getMemory());
        if (response.getResponseCode() == 409) {
            return getPlainSendMessage(chatId, "Клиент с таким именем уже создан");
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

    @Override
    public String getCommand() {
        return "/cc";
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Action action;

        if (update.getMessage() != null && update.getMessage().getText().equals(getCommand())) {
            action = Action.COMMAND_INPUT;
        } else if (update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null) {
            action = Action.CALLBACK_INPUT;
        } else {
            action = Action.ANY_STRING;
        }

        NextStateFunction<State, BotApiMethod<?>, Update> nextStateFunction = stateMachine.getNextStateFunction(action);
        BotApiMethod<?> botApiMethod = nextStateFunction.getFunctionForTransition().apply(update);
        stateMachine.applyNextState(action);

        return botApiMethod;
    }

    @Override
    public boolean isFinished(Long userId) {
        return stateMachine.getCurrentState() == State.FINISH;
    }
}
