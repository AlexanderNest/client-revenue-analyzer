package ru.nesterov.exception;

import ru.nesterov.common.exception.AppException;

public class ClientDataIntegrityException extends AppException {
    public ClientDataIntegrityException(String message) {
        super(message);
    }
}
