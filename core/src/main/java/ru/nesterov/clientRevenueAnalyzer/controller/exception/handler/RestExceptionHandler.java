package ru.nesterov.clientRevenueAnalyzer.controller.exception.handler;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.clientRevenueAnalyzer.controller.responses.ErrorResponse;
import ru.nesterov.clientRevenueAnalyzer.exception.AppException;


@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(AppException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAppException(AppException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());
        return errorResponse;
    }
}
