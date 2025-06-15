package ru.nesterov.exception;

import ru.nesterov.common.exception.AppException;

public class ClientIsAlreadyCreatedException extends AppException {
    public ClientIsAlreadyCreatedException(String clientName) {
        super("Клиент с именем [" + clientName + "] уже создан");
    }
}
