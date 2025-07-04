package ru.nesterov.ai.gigachat.dto;

import lombok.Data;
import ru.nesterov.ai.core.api.Usage;

@Data
public class GigaChatUsageImp implements Usage {
    private Long promptTokens;
    private Long completionTokens;
    private Long totalTokens;
}
