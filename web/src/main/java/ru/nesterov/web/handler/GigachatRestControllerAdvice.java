package ru.nesterov.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.gigachat.exception.GigachatException;
import ru.nesterov.web.controller.response.ResponseWithMessage;

@RestControllerAdvice
public class GigachatRestControllerAdvice extends AbstractRestControllerAdvice {

    @ExceptionHandler(GigachatException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleGigachatException(GigachatException exception){
        return buildResponseWithMessage(exception);
    }
}
