package ru.nesterov.web.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.calendar.integration.exception.CalendarIntegrationException;
import ru.nesterov.core.exception.ClientDataIntegrityException;
import ru.nesterov.core.exception.CoreException;
import ru.nesterov.web.controller.response.ResponseWithMessage;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {
    @ExceptionHandler(CoreException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleAppException(CoreException exception) {
        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(exception.getMessage());
        log.error("Core exception", exception);
        return responseWithMessage;
    }

    @ExceptionHandler(CalendarIntegrationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleAppException(CalendarIntegrationException exception) {
        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(exception.getMessage());
        log.error("Google calendar exception", exception);
        return responseWithMessage;
    }

    @ExceptionHandler(ClientDataIntegrityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseWithMessage handleClientDataIntegrityException(ClientDataIntegrityException exception){
        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(exception.getMessage());
        log.error("AppException", exception);
        return responseWithMessage;
    }
}
