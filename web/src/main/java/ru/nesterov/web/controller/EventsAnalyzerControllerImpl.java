package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.IncomeAnalysisResult;
import ru.nesterov.core.service.event.EventsAnalyzerService;
import ru.nesterov.core.service.user.UserService;
import ru.nesterov.web.controller.request.GetForMonthRequest;
import ru.nesterov.web.controller.response.ClientMeetingsStatisticResponse;
import ru.nesterov.web.controller.response.EventResponse;
import ru.nesterov.web.mapper.ClientMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EventsAnalyzerControllerImpl implements EventsAnalyzerController {
    private final EventsAnalyzerService eventsAnalyzerService;
    private final UserService userService;

    public Map<String, ClientMeetingsStatistic> getClientsStatistics(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getStatisticsOfEachClientMeetingsForMonth(userService.getUserByUsername(username), request.getMonthName());
    }

    public ClientMeetingsStatisticResponse getClientStatistic(@RequestHeader(name = "X-username") String username, @RequestParam("clientName") String clientName) {
        return ClientMapper.mapToClientMeetingsStatisticResponse(eventsAnalyzerService.getStatisticsByClientMeetings(userService.getUserByUsername(username), clientName)) ;
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