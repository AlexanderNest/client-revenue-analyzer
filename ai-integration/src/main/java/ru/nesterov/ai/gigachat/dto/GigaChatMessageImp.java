package ru.nesterov.ai.gigachat.dto;

import lombok.Data;
import ru.nesterov.ai.core.api.Message;

@Data
public class GigaChatMessageImp implements Message {
    private String content;
    private String role;

}
