package ru.nesterov.statemachine;

import ru.nesterov.statemachine.dto.NextStateFunction;
import ru.nesterov.statemachine.dto.TransitionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class StateMachineProvider<STATE, ACTION, R, T, MEMORY> {

    private final Map<Long, StateMachine<STATE, ACTION, R, T, MEMORY>> userMachines = new ConcurrentHashMap<>();
    private final STATE initialState;
    private final Class<MEMORY> memory;
    private final Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, R, T>> transitions = new HashMap<>();

    public StateMachineProvider(STATE initialState, Class<MEMORY> memory) {
        this.initialState = initialState;
        this.memory = memory;
    }

    public StateMachine<STATE, ACTION, R, T, MEMORY> getOrCreateMachine(Long userId) {
        return userMachines.computeIfAbsent(userId, ignored -> {
            try{
                MEMORY memoryInstance = memory.getDeclaredConstructor().newInstance();
                return createMachine(initialState, memoryInstance, transitions);
            } catch (ReflectiveOperationException exception) {
                throw new RuntimeException("", exception);
        }
        });
    }

    private StateMachine<STATE, ACTION, R, T, MEMORY> createMachine (STATE initialState, MEMORY memory, Map<TransitionDescription<STATE, ACTION>, NextStateFunction<STATE, R, T>> transitions) {
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
