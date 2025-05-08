package ru.nesterov.statemachine.dto;

import lombok.Value;

@Value
public class TransitionDescription<STATE, ACTION> {
    private final STATE state;
    private final ACTION action;
}
