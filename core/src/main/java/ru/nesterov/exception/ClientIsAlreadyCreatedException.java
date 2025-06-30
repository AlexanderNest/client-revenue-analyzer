package ru.nesterov.exception;

public class ClientIsAlreadyCreatedException extends CoreException {
    public ClientIsAlreadyCreatedException(String clientName) {
        super("Клиент с именем [" + clientName + "] уже создан");
    }
}
