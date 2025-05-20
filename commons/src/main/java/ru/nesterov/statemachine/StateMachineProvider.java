package ru.nesterov.statemachine;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.statemachine.dto.NextStateFunction;
import ru.nesterov.statemachine.dto.TransitionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
@Getter
@Setter
public class StateMachineProvider<STATE, ACTION, MEMORY> {

    private final Map<Long, StateMachine<STATE, ACTION, MEMORY>> userMachines = new ConcurrentHashMap<>();
    private final STATE initialState;
    private final Class<MEMORY> memory;
    private final Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, BotApiMethod<?>, Update>> transitions = new HashMap<>();

    public StateMachineProvider(STATE initialState, Class<MEMORY> memory) {
        this.initialState = initialState;
        this.memory = memory;
    }

    public StateMachine<STATE, ACTION, MEMORY> getMachine(Long userId) {
        return userMachines.get(userId);
    }

    public StateMachine<STATE, ACTION, MEMORY> createMachine(Long userId, MEMORY memory, Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, BotApiMethod<?>, Update>> transitions) {
        StateMachine<STATE, ACTION, MEMORY> stateMachine = new StateMachine<>(initialState, memory);
        transitions.forEach(
                (transitionDescription, nextStateFunction) ->
                        stateMachine.addTransition(
                                transitionDescription.getState(),
                                transitionDescription.getAction(),
                                nextStateFunction.getState(),
                                nextStateFunction.getFunctionForTransition()
                        )
        );
        userMachines.put(userId, stateMachine);
        return stateMachine;
    }

    public void removeMachine(Long userId) {
        userMachines.remove(userId);
    }

    public StateMachineProvider<STATE, ACTION, MEMORY> addTransition(STATE state, ACTION actionForTransition, STATE nextState, Function<Update, BotApiMethod<?>> functionForTransition) {
        transitions.put(new TransitionDescription<>(state, actionForTransition), new NextStateFunction<>(nextState, functionForTransition));
        return this;
    }
}
