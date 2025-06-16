package ru.nesterov.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.nesterov.common.exception.AppException;
@ResponseStatus(HttpStatus.CONFLICT)
public class ClientDataIntegrityException extends AppException {
    public ClientDataIntegrityException(String message) {
        super(message);
    }
}
