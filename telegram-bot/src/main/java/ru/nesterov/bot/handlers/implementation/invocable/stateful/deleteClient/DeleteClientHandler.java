package ru.nesterov.bot.handlers.implementation.invocable.stateful.deleteClient;

import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.statemachine.dto.Action;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DeleteClientHandler extends StatefulCommandHandler<State, DeleteClientDto> {

    public DeleteClientHandler() {
        super(State.STARTED, DeleteClientDto.class);
    }

    @Override
    public String getCommand() {
        return "Удалить клиента";
    }

    @Override
    protected void initTransitions() {
        stateMachineProvider
                .addTransition(State.STARTED, Action.COMMAND_INPUT, State.SELECT_CLIENT, this::handleCommandInputAndSendClientNamesKeyboard)

                .addTransition(State.SELECT_CLIENT, Action.ANY_CALLBACK_INPUT, State.APPROVE_DELETING, this::handleClientNameAndRequestApprove)

                .addTransition(State.APPROVE_DELETING, Action.CALLBACK_TRUE, State.FINISH, this::handleDeleteClient)
                .addTransition(State.APPROVE_DELETING, Action.CALLBACK_FALSE, State.FINISH, this::handleDeleteCanceling);
    }

    @SneakyThrows
    private List<BotApiMethod<?>> handleCommandInputAndSendClientNamesKeyboard(Update update) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<GetActiveClientResponse> clients = client.getActiveClients(TelegramUpdateUtils.getUserId(update));

        if (clients.isEmpty()) {
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Нет доступных клиентов");
        }

        clients.sort(Comparator.comparing(GetActiveClientResponse::getName, String.CASE_INSENSITIVE_ORDER));

        for (GetActiveClientResponse response : clients) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(response.getName());
            ButtonCallback callback = new ButtonCallback();
            callback.setCommand(getCommand());
            callback.setValue(response.getName());
            button.setCallbackData(buttonCallbackService.getTelegramButtonCallbackString(callback));

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            keyboard.add(rowInline);
        }
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Выберите клиента для удаления:", keyboardMarkup);
    }

    private List<BotApiMethod<?>> handleClientNameAndRequestApprove(Update update) {
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
        getStateMachine(update).getMemory().setClientName(buttonCallback.getValue());

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);
        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Подтвердите удаление", keyboardMarkup);
    }

    private List<BotApiMethod<?>> handleDeleteClient(Update update) {
        client.deleteClient(TelegramUpdateUtils.getUserId(update), getStateMachine(update).getMemory().getClientName());
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), formatDeleteUserResponse(getStateMachine(update).getMemory().getClientName()));
    }

    private String formatDeleteUserResponse(String clientName) {
        return String.join(System.lineSeparator(),
                "Пользователь " + clientName + " успешно удален.");
    }

    private List<BotApiMethod<?>> handleDeleteCanceling(Update update) {
        return editMessage(TelegramUpdateUtils.getChatId(update), TelegramUpdateUtils.getMessageId(update), "Пользователь не удален.", null);
    }
}
