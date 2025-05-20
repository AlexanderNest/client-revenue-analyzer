package ru.nesterov.bot.handlers.abstractions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.implementation.stateful.createClient.State;
import ru.nesterov.bot.handlers.service.ActionService;
import ru.nesterov.statemachine.StateMachine;
import ru.nesterov.statemachine.StateMachineProvider;
import ru.nesterov.statemachine.dto.Action;
import ru.nesterov.statemachine.dto.NextStateFunction;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class StatefulCommandHandler <STATE, MEMORY> extends DisplayedCommandHandler {
    private final ActionService actionService = new ActionService(getCommand());

    protected final StateMachineProvider<STATE, Action, MEMORY> stateMachineProvider;
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
        Action action = actionService.defineTheAction(update, expectedActions);

        NextStateFunction<STATE, BotApiMethod<?>, Update> nextStateFunction = stateMachine.getNextStateFunction(action);
        BotApiMethod<?> botApiMethod = nextStateFunction.getFunctionForTransition().apply(update);
        stateMachine.applyNextState(action);

        return botApiMethod;
    }

    public abstract void initTransitions();

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
    @Override
    public boolean isFinished(Long userId) {
        StateMachine<STATE, Action, MEMORY> stateMachine = stateMachineProvider.getMachine(userId);
        return stateMachine == null || stateMachine.getCurrentState() == State.FINISH;
    }
}
