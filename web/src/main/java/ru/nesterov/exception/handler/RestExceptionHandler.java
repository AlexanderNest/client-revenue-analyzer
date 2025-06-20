package ru.nesterov.exception.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.common.exception.AppException;
import ru.nesterov.controller.response.ResponseWithMessage;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {
    @ExceptionHandler(AppException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleAppException(AppException exception) {
        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(exception.getMessage());
        log.error("AppException", exception);
        return responseWithMessage;
    }
}
