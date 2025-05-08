package ru.nesterov.statemachine.dto;

import lombok.Value;

import java.util.function.Function;

@Value
public class NextStateFunction<STATE, R, T> {
    private final STATE state;
    private final Function<T, R> functionForTransition;
}
