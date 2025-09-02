package ru.nesterov.bot.handlers.implementation.invocable.adminsHandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetAllUsersByRoleAndSourceResponse;
import ru.nesterov.bot.dto.SendMessageToUserRequest;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.statemachine.dto.Action;
import ru.nesterov.bot.utils.TelegramUpdateUtils;
import ru.nesterov.core.entity.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Запуск рассылки админом
 */
@Component
@Slf4j
public class SendMessageToUsersHandler extends StatefulCommandHandler<State, SendMessageToUserRequest> {

    public SendMessageToUsersHandler() {
        super(State.STARTED, SendMessageToUserRequest.class);
    }

    @Override
    public String getCommand() {
        return "Запустить рассылку";
    }

    @Override
    protected List<Role> getApplicableRoles() {
        return List.of(Role.ADMIN);
    }

    @Override
    public void initTransitions() {
        stateMachineProvider
                .addTransition(State.STARTED, Action.COMMAND_INPUT, State.TEXT_INPUT, this::handleStartMessage)

                .addTransition(State.TEXT_INPUT, Action.ANY_STRING, State.WAITING_FOR_CONFIRMATION, this::handleTextInput)
                .addTransition(State.TEXT_INPUT, Action.ANY_STRING, State.WAITING_FOR_CONFIRMATION, this::handleTextInput)

                .addTransition(State.WAITING_FOR_CONFIRMATION, Action.CALLBACK_TRUE, State.TEXT_INPUT, this::handleUpdatedMessage)
                .addTransition(State.WAITING_FOR_CONFIRMATION, Action.CALLBACK_FALSE, State.FINISH, this::sendMessageToUsers);
    }

    public List<BotApiMethod<?>> handleStartMessage(Update update) {
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите текст рассылки");
    }

    public List<BotApiMethod<?>> handleUpdatedMessage(Update update) {
        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Введите исправленный текст рассылки");
    }

    public List<BotApiMethod<?>> handleTextInput(Update update) {
        getStateMachine(update).getMemory().setMessage(update.getMessage().getText());
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Редактировать сообщение?", keyboardMarkup);
    }

    public List<BotApiMethod<?>> sendMessageToUsers(Update update) {
        List<GetAllUsersByRoleAndSourceResponse> users = client.getUsersIdByRoleAndSource(TelegramUpdateUtils.getChatId(update),
                Role.USER, "telegram");
        List<BotApiMethod<?>> messages = new ArrayList<>();
        for (GetAllUsersByRoleAndSourceResponse user : users) {
            try {
                messages.addAll(getPlainSendMessage(Long.parseLong(user.getId()), getStateMachine(update).getMemory().getMessage()));
            } catch (Exception exception) {
                log.error("Ошибка отправки сообщения");
            }
        }
        messages.addAll(getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Рассылка завершена"));
        return messages;
    }
}
