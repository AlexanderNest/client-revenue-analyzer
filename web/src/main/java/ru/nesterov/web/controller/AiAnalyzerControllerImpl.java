package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.ai.AiAnalyzerService;
import ru.nesterov.core.service.user.UserService;
import ru.nesterov.web.controller.request.GetForMonthRequest;
import ru.nesterov.web.controller.response.GetClientAnalyticResponse;

@RestController
@RequiredArgsConstructor
public class AiAnalyzerControllerImpl implements AiAnalyzerController {
    private final AiAnalyzerService aiAnalyzerService;
    private final UserService userService;

    @Override
    public GetClientAnalyticResponse analyzeClients(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request) {
        GetClientAnalyticResponse response = new GetClientAnalyticResponse();
        response.setContent(aiAnalyzerService.analyzeClients(userService.getUserByUsername(username), request.getMonthName()));

        return response;
    }
}
