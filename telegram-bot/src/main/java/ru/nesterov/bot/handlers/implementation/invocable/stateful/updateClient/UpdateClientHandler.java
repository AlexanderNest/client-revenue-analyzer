package ru.nesterov.bot.handlers.implementation.invocable.stateful.updateClient;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.statemachine.dto.Action;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.List;

import static ru.nesterov.bot.handlers.implementation.invocable.stateful.updateClient.State.STARTED;

@Component
@Slf4j
public class UpdateClientHandler extends StatefulCommandHandler<State, UpdateClientRequest> {
    public UpdateClientHandler() {
        super(STARTED, UpdateClientRequest.class);
    }

    @Override
    public String getCommand() {
        return "Обновить клиента";
    }

    @Override
    protected void initTransitions() {
        stateMachineProvider
                .addTransition(STARTED, Action.COMMAND_INPUT, State.SELECT_CLIENT, this::handleCommandInputAndSendClientNamesKeyboard)
                .addTransition( State.SELECT_CLIENT, Action.ANY_CALLBACK_INPUT, State.APPROVE_NAME_CHANGE, this::handleClientNameAndSendNameChangeApprove)

                .addTransition(State.APPROVE_NAME_CHANGE, Action.CALLBACK_FALSE, State.APPROVE_PRICE_CHANGE, this::handleFalseAndAskForPriceChange)
                .addTransition(State.APPROVE_NAME_CHANGE, Action.CALLBACK_TRUE, State.CHANGE_NAME, this::handleTrueAndAskForName)
                .addTransition(State.CHANGE_NAME, Action.ANY_STRING, State.APPROVE_PRICE_CHANGE, this::handleClientNameInputAndAskForPriceChange)

                .addTransition(State.APPROVE_PRICE_CHANGE, Action.CALLBACK_FALSE, State.APPROVE_DESCRIPTION_CHANGE, this::handleChangeDescriptionApprove)
                .addTransition(State.APPROVE_PRICE_CHANGE, Action.CALLBACK_TRUE, State.CHANGE_PRICE, this::handlePricePerHourChange)
                .addTransition(State.CHANGE_PRICE, Action.ANY_STRING, State.APPROVE_DESCRIPTION_CHANGE, this::handleChangeDescriptionApprove)

                .addTransition(State.APPROVE_DESCRIPTION_CHANGE, Action.CALLBACK_FALSE, State.APPROVE_PHONE_CHANGE, this::handleChangePhoneApprove)
                .addTransition(State.APPROVE_DESCRIPTION_CHANGE, Action.CALLBACK_TRUE, State.CHANGE_DESCRIPTION, this::handleDescriptionChange)
                .addTransition(State.CHANGE_DESCRIPTION, Action.ANY_STRING, State.APPROVE_PHONE_CHANGE, this::handleChangePhoneApprove)

                .addTransition(State.APPROVE_PHONE_CHANGE, Action.CALLBACK_FALSE, State.FINISH, this::handleUpdateClient)
                .addTransition(State.APPROVE_PHONE_CHANGE, Action.CALLBACK_TRUE, State.CHANGE_PHONE, this::handlePhoneChange)
                .addTransition(State.CHANGE_PHONE, Action.ANY_STRING, State.FINISH, this::handleUpdateClient);
    }

    public List<BotApiMethod<?>> handleCommandInputAndSendClientNamesKeyboard(Update update) {
        return getClientNamesKeyboard(update, "Выберите клиента для обновления данных:");
    }

    private List<BotApiMethod<?>> handleTrueAndAskForName(Update update) {
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите новое имя:");
    }

    private List<BotApiMethod<?>> handlePricePerHourChange(Update update) {
        if (update.hasMessage()) {
            getStateMachine(update).getMemory().setNewName(update.getMessage().getText());
        }

        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите новую стоимость в час");
    }

    private List<BotApiMethod<?>> handleDescriptionChange(Update update) {
        if(update.hasMessage()) {
            getStateMachine(update).getMemory().setPricePerHour(Integer.parseInt(update.getMessage().getText()));
        }
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите новое описание: ");
    }

    private List<BotApiMethod<?>> handlePhoneChange(Update update) {
        if(update.hasMessage()) {
            getStateMachine(update).getMemory().setDescription(update.getMessage().getText());
        }
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите новый номер телефона: ");
    }

    private List<BotApiMethod<?>> handleUpdateClient(Update update){
        if (update.hasMessage()) {
            getStateMachine(update).getMemory().setPhone(update.getMessage().getText());
        }
        UpdateClientResponse updateClientResponse = client.updateClient(TelegramUpdateUtils.getChatId(update), getStateMachine(update).getMemory());
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), formatUpdateClients(updateClientResponse));
    }

    private String formatUpdateClients (UpdateClientResponse response){

        return String.join(System.lineSeparator(),
                "Клиент успешно обновлен!",
                "Имя: " + response.getName(),
                "Стоимость за час: " + response.getPricePerHour(),
                "Описание: " + response.getDescription(),
                "Номер телефона: " + response.getPhone());
    }


    private List<BotApiMethod<?>> handleClientNameAndSendNameChangeApprove(Update update) {
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
        getStateMachine(update).getMemory().setClientName(buttonCallback.getValue());
        return getApproveKeyBoard(update, "Обновить имя пользователя? ");
    }

    private List<BotApiMethod<?>> handleFalseAndAskForPriceChange(Update update) {
        return getApproveKeyBoard(update, "Обновить стоимость за час пользователя? ");
    }

    private List<BotApiMethod<?>> handleClientNameInputAndAskForPriceChange(Update update) {
        getStateMachine(update).getMemory().setClientName(update.getMessage().getText());
        return getApproveKeyBoard(update, "Обновить стоимость за час пользователя? ");
    }

    private List<BotApiMethod<?>> handleChangeDescriptionApprove(Update update) {
        if(update.hasMessage()) {
            getStateMachine(update).getMemory().setPricePerHour(Integer.parseInt(update.getMessage().getText()));
        }
        return getApproveKeyBoard(update, "Обновить описание пользователя? ");
    }

    private List<BotApiMethod<?>> handleChangePhoneApprove(Update update) {
        if (update.hasMessage()) {
            getStateMachine(update).getMemory().setDescription(update.getMessage().getText());
        }
        return getApproveKeyBoard(update, "Обновить номер телефона пользователя? ");
    }
}