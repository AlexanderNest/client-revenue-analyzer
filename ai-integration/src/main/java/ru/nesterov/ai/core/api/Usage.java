package ru.nesterov.ai.core.api;

public interface Usage {
    Long getPromptTokens();
    Long getCompletionTokens();
    Long getTotalTokens();
}
