package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.GetClientAnalyticResponse;
import ru.nesterov.formatter.ClientAnalyticsFormatter;
import ru.nesterov.service.ai.AiAnalyzerService;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.event.EventsAnalyzerService;
import ru.nesterov.service.user.UserService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiAnalyzerControllerImpl implements AiAnalyzerController {
    private final AiAnalyzerService aiAnalyzerService;
    private final EventsAnalyzerService eventsAnalyzerService;
    private final UserService userService;

    @Override
    public GetClientAnalyticResponse analyzeClients(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request) {
        Map<String, ClientMeetingsStatistic> meetingsStatistic = eventsAnalyzerService.getStatisticsOfEachClientMeetings(
                userService.getUserByUsername(username), request.getMonthName());
        GetClientAnalyticResponse response = new GetClientAnalyticResponse();
        response.setContent(aiAnalyzerService.analyzeClients(ClientAnalyticsFormatter.format(meetingsStatistic)));

        return response;
    }
}
