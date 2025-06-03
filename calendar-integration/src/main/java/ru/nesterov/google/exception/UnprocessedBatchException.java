package ru.nesterov.google.exception;

import ru.nesterov.exception.AppException;

public class UnprocessedBatchException extends AppException {
    public UnprocessedBatchException(String message) {
        super(message);
    }
}
