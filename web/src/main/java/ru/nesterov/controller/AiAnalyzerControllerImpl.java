package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.response.GetClientAnalyticResponse;
import ru.nesterov.service.ai.AiAnalyzerService;

@RestController
@RequiredArgsConstructor
public class AiAnalyzerControllerImpl implements AiAnalyzerController {
    private final AiAnalyzerService aiAnalyzerService;
    @Override
    public GetClientAnalyticResponse analyzeClients() {
        GetClientAnalyticResponse response = new GetClientAnalyticResponse();
        response.setContent(aiAnalyzerService.analyzeClients());

        return response;
    }
}
