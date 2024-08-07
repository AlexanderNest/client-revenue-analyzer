package ru.nesterov.exception.handler;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.exception.AppException;
import ru.nesterov.controller.response.ResponseWithMessage;


@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(AppException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleAppException(AppException exception) {
        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(exception.getMessage());
        return responseWithMessage;
    }
}
