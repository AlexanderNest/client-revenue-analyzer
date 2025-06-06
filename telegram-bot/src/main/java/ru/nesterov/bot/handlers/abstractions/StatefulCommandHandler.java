package ru.nesterov.bot.handlers.abstractions;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.service.ActionService;
import ru.nesterov.statemachine.StateMachine;
import ru.nesterov.statemachine.StateMachineProvider;
import ru.nesterov.statemachine.dto.Action;
import ru.nesterov.statemachine.dto.NextStateFunction;

import java.lang.reflect.InvocationTargetException;
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

    public StateMachine<STATE, Action, MEMORY> getStateMachine(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        StateMachine<STATE, Action, MEMORY> stateMachine = stateMachineProvider.getMachine(userId);
        if (stateMachine == null){
            try {
                stateMachine = stateMachineProvider.createMachine(
                        userId,
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
    public BotApiMethod<?> handle(Update update) {
        StateMachine<STATE, Action, MEMORY> stateMachine = getStateMachine(update);
        List<Action> expectedActions = stateMachine.getExpectedActions();
        Action action = actionService.defineTheAction(getCommand(), update, expectedActions);

        NextStateFunction<STATE> nextStateFunction = stateMachine.getNextStateFunction(action);
        BotApiMethod<?> botApiMethod = nextStateFunction.getFunctionForTransition().apply(update);
        stateMachine.applyNextState(action);

        return botApiMethod;
    }

    public abstract void initTransitions();

    public void resetState(long userId) {
        stateMachineProvider.removeMachine(userId);
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        boolean isPlainText = message != null && message.getText() != null;
        if (isPlainText && !isFinishedOrNotStarted(TelegramUpdateUtils.getUserId(update))) {
            return true;
        }

        return super.isApplicable(update);
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
}
