package ru.nesterov.core.exception;

public class ClientIsAlreadyCreatedException extends CoreException {
    public ClientIsAlreadyCreatedException(String clientName) {
        super("Клиент с именем [" + clientName + "] уже создан");
    }
}
