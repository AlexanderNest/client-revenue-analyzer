package ru.nesterov.exception;

public class ClientIsAlreadyCreatedException extends AppException {
    public ClientIsAlreadyCreatedException(String clientName) {
        super("Клиент с именем [" + clientName + "] уже создан");
    }
}
