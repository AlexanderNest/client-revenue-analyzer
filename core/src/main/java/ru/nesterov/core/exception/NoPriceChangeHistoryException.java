package ru.nesterov.core.exception;

public class NoPriceChangeHistoryException extends CoreException {
    public NoPriceChangeHistoryException() {
        super("No price change history found");
    }
}
