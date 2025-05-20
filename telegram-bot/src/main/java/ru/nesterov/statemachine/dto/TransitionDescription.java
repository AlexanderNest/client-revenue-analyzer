package ru.nesterov.statemachine.dto;

import lombok.Value;

@Value
public class TransitionDescription<STATE> {
    private final STATE state;
    private final Action action;
}
