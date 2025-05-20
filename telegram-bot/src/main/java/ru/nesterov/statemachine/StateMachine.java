package ru.nesterov.statemachine;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.statemachine.dto.Action;
import ru.nesterov.statemachine.dto.NextStateFunction;
import ru.nesterov.statemachine.dto.TransitionDescription;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class StateMachine<STATE, ACTION, MEMORY> {
    private final Map<TransitionDescription<STATE>, NextStateFunction<STATE>> transitions = new ConcurrentHashMap<>();
    @Getter
    private STATE currentState;
    @Getter
    @Setter
    private MEMORY memory;

    public StateMachine(STATE initialState, MEMORY memory) {
        this.currentState = initialState;
        this.memory = memory;
    }

    public StateMachine<STATE, ACTION, MEMORY> addTransition(STATE state, Action actionForTransition, STATE nextState, Function<Update, BotApiMethod<?> > functionForTransition) {
        transitions.put(new TransitionDescription<>(state, actionForTransition), new NextStateFunction<>(nextState, functionForTransition));
        return this;
    }

    public List<Action> getExpectedActions() {
        return transitions.keySet().stream()
                .map(TransitionDescription::getAction)
                .distinct()
                .toList();
    }

    public NextStateFunction<STATE> getNextStateFunction(Action action) {
        return transitions.get(new TransitionDescription<>(currentState, action));
    }

    public void applyNextState(Action action) {
        currentState = transitions.get(new TransitionDescription<>(currentState, action)).getState();
    }
}
