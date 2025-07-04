package ru.nesterov.ai.gigachat.response;

import lombok.Data;
import ru.nesterov.ai.gigachat.dto.GigaChatChoiceImp;
import ru.nesterov.ai.gigachat.dto.GigaChatUsageImp;

import java.util.List;

@Data
public class GigaChatTextGenerationResponse {
    private List<GigaChatChoiceImp> gigaChatChoiceImps;
    private Long created;
    private String model;
    private String object;
    private GigaChatUsageImp usage;
}
