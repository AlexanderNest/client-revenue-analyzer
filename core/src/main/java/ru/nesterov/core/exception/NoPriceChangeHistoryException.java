package ru.nesterov.core.exception;

public class NoPriceChangeHistoryException extends CoreException {
    public NoPriceChangeHistoryException(String clientName) {
        super("Не найдена цена для клиента " + clientName);
    }
}
