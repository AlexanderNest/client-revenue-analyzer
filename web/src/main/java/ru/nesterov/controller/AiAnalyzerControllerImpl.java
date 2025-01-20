package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.GetClientAnalyticResponse;
import ru.nesterov.service.ai.AiAnalyzerService;
import ru.nesterov.service.user.UserService;

@RestController
@RequiredArgsConstructor
public class AiAnalyzerControllerImpl implements AiAnalyzerController {
    private final AiAnalyzerService aiAnalyzerService;
    private final UserService userService;

    @Override
    public GetClientAnalyticResponse analyzeClients(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request) {
        GetClientAnalyticResponse response = new GetClientAnalyticResponse();
        response.setContent(aiAnalyzerService.analyzeClients(userService.getUserByUsername(username),request.getMonthName()));

        return response;
    }
}
