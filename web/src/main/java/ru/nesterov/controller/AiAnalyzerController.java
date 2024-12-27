package ru.nesterov.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.controller.response.GetClientAnalyticResponse;
@RequestMapping("/ai")
public interface AiAnalyzerController {
    @GetMapping("/generateText")
    GetClientAnalyticResponse analyzeClients();
}
