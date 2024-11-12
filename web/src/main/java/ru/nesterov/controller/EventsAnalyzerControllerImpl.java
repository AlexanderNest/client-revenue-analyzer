package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.EventResponse;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.service.user.UserService;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.event.EventsAnalyzerService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EventsAnalyzerControllerImpl implements EventsAnalyzerController {
    private final EventsAnalyzerService eventsAnalyzerService;
    private final UserService userService;

    public Map<String, ClientMeetingsStatistic> getClientStatistics(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getStatisticsOfEachClientMeetings(userService.getUserByUsername(username), request.getMonthName());
    }

    public Map<EventStatus, Integer> getEventsStatusesForMonth(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getEventStatusesByMonthName(userService.getUserByUsername(username), request.getMonthName());
    }

    public ResponseEntity<IncomeAnalysisResult> getIncomeAnalysisForMonth(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request) {
        IncomeAnalysisResult result = eventsAnalyzerService.getIncomeAnalysisByMonth(userService.getUserByUsername(username), request.getMonthName());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public List<EventResponse> getUnpaidEvents(@RequestHeader(name = "X-username") String username) {
        return eventsAnalyzerService.getUnpaidEvents(userService.getUserByUsername(username)).stream()
                .map(event -> new EventResponse(event.getSummary(), event.getStart()))
                .toList();
    }
}