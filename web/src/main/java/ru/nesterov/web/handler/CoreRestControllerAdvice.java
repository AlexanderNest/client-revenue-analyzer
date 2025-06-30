package ru.nesterov.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nesterov.core.exception.ClientDataIntegrityException;
import ru.nesterov.core.exception.CoreException;
import ru.nesterov.web.controller.response.ResponseWithMessage;

@RestControllerAdvice
public class CoreRestControllerAdvice extends AbstractRestControllerAdvice {

    @ExceptionHandler(CoreException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseWithMessage handleCoreException(CoreException exception) {
        return buildResponseWithMessage(exception);
    }

    @ExceptionHandler(ClientDataIntegrityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseWithMessage handleClientDataIntegrityException(ClientDataIntegrityException exception){
        return buildResponseWithMessage(exception);
    }
}
