package ru.nesterov.gigachat.service;

import java.io.IOException;

public interface ResourceReader {
    String read(String resourcePath) throws IOException;
}
