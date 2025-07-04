package ru.nesterov.gigachat.service;

public interface GigaChatTokenService {
    String getToken(boolean forceUpdate);
}
