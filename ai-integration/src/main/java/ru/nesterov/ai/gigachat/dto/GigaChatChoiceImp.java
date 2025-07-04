package ru.nesterov.ai.gigachat.dto;

import lombok.Data;
import ru.nesterov.ai.core.api.Choice;

@Data
public class GigaChatChoiceImp implements Choice<GigaChatMessageImp> {
    private GigaChatMessageImp gigaChatMessage;
    private Long index;
    private String finishReason;

    @Override
    public GigaChatMessageImp getMessage() {
        return gigaChatMessage;
    }
}
