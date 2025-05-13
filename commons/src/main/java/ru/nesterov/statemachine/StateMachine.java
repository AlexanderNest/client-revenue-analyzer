package ru.nesterov.statemachine;

import lombok.Getter;
import ru.nesterov.statemachine.dto.NextStateFunction;
import ru.nesterov.statemachine.dto.TransitionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class StateMachine<STATE, ACTION, R, T, MEMORY> {
    private final Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, R, T>> transitions = new ConcurrentHashMap<>();
    @Getter
    private STATE currentState;
    @Getter
    private final MEMORY memory;

    public StateMachine(STATE initialState, MEMORY memory) {
        this.currentState = initialState;
        this.memory = memory;
    }

    public StateMachine<STATE, ACTION, R, T, MEMORY> addTransition(STATE state, ACTION actionForTransition, STATE nextState, Function<T, R> functionForTransition) {
        transitions.put(new TransitionDescription<>(state, actionForTransition), new NextStateFunction<>(nextState, functionForTransition));
        return this;
    }

    public NextStateFunction<STATE, R, T> getNextStateFunction(ACTION action) {
        return transitions.get(new TransitionDescription<>(currentState, action));
    }

    public void applyNextState(ACTION action) {
        currentState = transitions.get(new TransitionDescription<>(currentState, action)).getState();
    }
}
