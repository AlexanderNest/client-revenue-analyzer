package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.EventResponse;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.event.EventsAnalyzerService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EventsAnalyzerControllerImpl implements EventsAnalyzerController {
    private final EventsAnalyzerService eventsAnalyzerService;

    public Map<String, ClientMeetingsStatistic> getClientStatistics(@RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getStatisticsOfEachClientMeetings(request.getMonthName());
    }

    public Map<EventStatus, Integer> getEventsStatusesForMonth(@RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getEventStatusesByMonthName(request.getMonthName());
    }

    public IncomeAnalysisResult getIncomeAnalysisForMonth(@RequestBody GetForMonthRequest request) {
        return eventsAnalyzerService.getIncomeAnalysisByMonth(request.getMonthName());
    }

    public List<EventResponse> getUnpaidEvents() {
        return eventsAnalyzerService.getUnpaidEvents().stream()
                .map(event -> new EventResponse(event.getSummary(), event.getStart()))
                .toList();
    }
}