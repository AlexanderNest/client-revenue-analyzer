package ru.nesterov.statemachine;

import lombok.Getter;
import ru.nesterov.statemachine.dto.NextStateFunction;
import ru.nesterov.statemachine.dto.TransitionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
@Getter
public class StateMachineProvider<STATE, ACTION, R, T, MEMORY> {

    private final Map<Long, StateMachine<STATE, ACTION, R, T, MEMORY>> userMachines = new ConcurrentHashMap<>();
    private final STATE initialState;
    private final Class<MEMORY> memory;
    private final Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, R, T>> transitions = new HashMap<>();

    public StateMachineProvider(STATE initialState, Class<MEMORY> memory) {
        this.initialState = initialState;
        this.memory = memory;
    }

    public StateMachine<STATE, ACTION, R, T, MEMORY> getMachine(Long userId) {
        return userMachines.get(userId);
    }

    public StateMachine<STATE, ACTION, R, T, MEMORY> createMachine (Long userId, STATE initialState, MEMORY memory, Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, R, T>> transitions) {
        StateMachine<STATE, ACTION, R, T, MEMORY> stateMachine = new StateMachine<>(initialState, memory);
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

    public StateMachineProvider<STATE, ACTION, R, T, MEMORY> addTransition(STATE state, ACTION actionForTransition, STATE nextState, Function<T, R> functionForTransition) {
        transitions.put(new TransitionDescription<>(state, actionForTransition), new NextStateFunction<>(nextState, functionForTransition));
        return this;
    }
}
