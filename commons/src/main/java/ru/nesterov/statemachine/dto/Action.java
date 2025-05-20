package ru.nesterov.statemachine.dto;

import org.springframework.objenesis.SpringObjenesis;

public enum Action {
    COMMAND_INPUT,
    TRUE,
    FALSE,
    ANY_BOOLEAN,
    ANY_NUMBER,
    ANY_STRING,
    ANY_CALLBACK_INPUT
}
