package ru.nesterov.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.ai.core.exception.AiException;
import ru.nesterov.web.controller.response.ResponseWithMessage;

@RestControllerAdvice
public class GigachatRestControllerAdvice extends AbstractRestControllerAdvice {

    @ExceptionHandler(AiException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleAiException(AiException exception){
        return buildResponseWithMessage(exception);
    }
}
