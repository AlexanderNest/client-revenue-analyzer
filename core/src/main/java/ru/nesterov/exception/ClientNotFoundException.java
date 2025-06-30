package ru.nesterov.exception;

import java.time.LocalDateTime;

public class ClientNotFoundException extends CoreException {
    public ClientNotFoundException(String clientName) {
        super("Клиент с именем [" + clientName + "] не найден в базе");
    }

    public ClientNotFoundException(String clientName, LocalDateTime eventDateTime) {
        super("Клиент с именем [" + clientName + "] от даты [" + eventDateTime + "] не найден в базе");
    }
}
