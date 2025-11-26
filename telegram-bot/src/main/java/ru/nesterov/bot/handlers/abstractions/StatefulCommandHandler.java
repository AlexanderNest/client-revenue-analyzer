package ru.nesterov.bot.handlers.abstractions;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.statemachine.ActionService;
import ru.nesterov.bot.statemachine.StateMachine;
import ru.nesterov.bot.statemachine.StateMachineProvider;
import ru.nesterov.bot.statemachine.dto.Action;
import ru.nesterov.bot.statemachine.dto.NextStateFunction;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class StatefulCommandHandler<STATE extends Enum<STATE>, MEMORY> extends DisplayedCommandHandler {
    @Autowired
    protected ActionService actionService;

    protected final StateMachineProvider<STATE, MEMORY> stateMachineProvider;
    private final Class<MEMORY> memoryType;

    public StatefulCommandHandler(STATE state, Class<MEMORY> memoryType) {
        this.memoryType = memoryType;
        stateMachineProvider = new StateMachineProvider<>(state, memoryType);
        initTransitions();
    }

    /**
     * Инициализирует переходы между состояниями. Метод должен быть вызван в конструкторе.
     */
    protected abstract void initTransitions();

    public void resetState(long userId) {
        stateMachineProvider.removeMachine(userId);
    }

    /**
     * Определяет нужно ли сбрасывать обработчик для указанного пользователя.
     * Это полезно для тех случаев, когда один и тот же обработчик должен получить несколько сообщений подряд.
     * @param userId
     * @return
     *      true - если надо сбросить обработчики для пользователя.
     *      false - если надо, чтобы при следующем обновлении в чате вызвался тот же обработчик
     */
    public boolean isFinishedOrNotStarted(Long userId) {
        StateMachine<STATE, Action, MEMORY> stateMachine = stateMachineProvider.getMachine(userId);
        return stateMachine == null || "FINISH".equals(stateMachine.getCurrentState().name());
    }

    /**
     * Возвращает машину для указанного пользователя. Если машина для пользователя не существует, создает ее.
     */
    protected StateMachine<STATE, Action, MEMORY> getStateMachine(Update update) {
        long chatId = TelegramUpdateUtils.getChatId(update);
        StateMachine<STATE, Action, MEMORY> stateMachine = stateMachineProvider.getMachine(chatId);
        if (stateMachine == null){
            try {
                stateMachine = stateMachineProvider.createMachine(
                        chatId,
                        memoryType.getDeclaredConstructor().newInstance(),
                        stateMachineProvider.getTransitions()
                );
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return stateMachine;
    }

    @Override
    public List<BotApiMethod<?>> handle(Update update) {
        StateMachine<STATE, Action, MEMORY> stateMachine = getStateMachine(update);
        List<Action> expectedActions = stateMachine.getExpectedActions();
        Action action = actionService.defineTheAction(getCommand(), update, expectedActions);

        NextStateFunction<STATE> nextStateFunction = stateMachine.getNextStateFunction(action);

        if (nextStateFunction == null) {
            throw new RuntimeException(
                    "Не найдена функция для перехода из состояния '%s' через action = '%s', из данных update = %s"
                            .formatted(stateMachine.getCurrentState(), action, update)
            );
        }

        List<BotApiMethod<?>> botApiMethod = nextStateFunction.getFunctionForTransition().apply(update);
        stateMachine.applyNextState(action);

        return botApiMethod;
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        boolean isPlainText = message != null && message.getText() != null;
        if (isPlainText && !isFinishedOrNotStarted(TelegramUpdateUtils.getChatId(update))) {
            return true;
        }

        return super.isApplicable(update);
    }

    @SneakyThrows
    public List<BotApiMethod<?>> handleCommandInputAndSendClientNamesKeyboard(Update update, String text) {
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

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), text, keyboardMarkup);
    }

    public List<BotApiMethod<?>> handleApproveKeyBoard(Update update, String message) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);
        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), message, keyboardMarkup);
    }


}
