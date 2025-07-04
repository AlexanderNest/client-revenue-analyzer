package ru.nesterov.ai.core.api;

public interface Choice <T extends Message> {
    T getMessage();
    Long getIndex();
    String getFinishReason();
}
