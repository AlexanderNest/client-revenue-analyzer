package ru.nesterov.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.calendar.integration.exception.CalendarIntegrationException;
import ru.nesterov.web.controller.response.ResponseWithMessage;

@RestControllerAdvice
public class CalendarIntegrationRestControllerAdvice extends AbstractRestControllerAdvice {
    @ExceptionHandler(CalendarIntegrationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleCalendarIntegrationException(CalendarIntegrationException exception) {
        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(exception.getMessage());
        return responseWithMessage;
    }
}
