package ru.nesterov.statemachine;

import ru.nesterov.statemachine.dto.NextStateFunction;
import ru.nesterov.statemachine.dto.TransitionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StateMachineProvider<STATE, ACTION, R, T, MEMORY extends Cloneable> {

    private final Map<Long, StateMachine<STATE, ACTION, R, T, MEMORY>> userMachines = new HashMap<>();
    private final STATE initialState;
    private final Class<MEMORY> memory;

    public StateMachineProvider(STATE initialState, Class<MEMORY> memory) {
        this.initialState = initialState;
        this.memory = memory;
    }

    public StateMachine<STATE, ACTION, R, T, MEMORY> getOrCreateMachine(String userId) {
        return userMachines.computeIfAbsent(userId, ignored -> createMachine(initialState, memory, transitions));
    }

    public void removeMachine(String userId) {
        userMachines.remove(userId);
    }

    public StateMachine<STATE, ACTION, R, T, MEMORY> addTransition(STATE state, ACTION actionForTransition, STATE nextState, Function<T, R> functionForTransition) {
        transitions.put(new TransitionDescription<>(state, actionForTransition), new NextStateFunction<>(nextState, functionForTransition));
        return this;
    }

    public StateMachine<STATE, ACTION, R, T, MEMORY> createMachine(MEMORY memory,
            java.util.Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, R, T>> transitions) {
        new StateMachine<>(initialState, memory.getClass().getConstructor());
    }
}
