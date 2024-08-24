package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.EventResponse;
import ru.nesterov.service.event.EventsAnalyzerService;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.service.dto.IncomeAnalysisResult;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/analyzer")
public class EventsAnalyzerController {
    private final EventsAnalyzerService eventsAnalyzerService;

    @PostMapping("/getClientsStatistics")
    public Map<String, ClientMeetingsStatistic> getClientStatistics(@RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getStatisticsOfEachClientMeetings(request.getMonthName());
    }

    @PostMapping("/getEventsStatusesForMonth")
    public Map<EventStatus, Integer> getEventsStatusesForMonth(@RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getEventStatusesByMonthName(request.getMonthName());
    }

    @PostMapping("/getIncomeAnalysisForMonth")
    public IncomeAnalysisResult getIncomeAnalysisForMonth(@RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getIncomeAnalysisByMonth(request.getMonthName());
    }

    @GetMapping("/getUnpaidEvents")
    public List<EventResponse> getUnpaidEvents() {
        return eventsAnalyzerService.getUnpaidEvents().stream()
                .map(event -> new EventResponse(event.getSummary(), event.getStart()))
                .toList();
    }
}