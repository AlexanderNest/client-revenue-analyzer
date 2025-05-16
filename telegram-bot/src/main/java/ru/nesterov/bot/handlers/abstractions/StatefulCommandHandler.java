package ru.nesterov.bot.handlers.abstractions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.implementation.stateful.createClient.State;
import ru.nesterov.statemachine.StateMachineProvider;
import ru.nesterov.statemachine.dto.Action;

public abstract class StatefulCommandHandler <STATE, MEMORY> extends DisplayedCommandHandler {
    protected final StateMachineProvider<STATE, Action, BotApiMethod<?>, Update, MEMORY> stateMachineProvider;

    public StatefulCommandHandler(STATE state, Class<MEMORY> memoryType) {
        stateMachineProvider = new StateMachineProvider<>(state, memoryType);
        initTransitions();
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
        return stateMachineProvider.getMachine(userId).getCurrentState() == State.FINISH;
        // TODO скорее всего тут будут плодиться машины. заменить на метод get. Создавать тут машину не надо
    }
}
