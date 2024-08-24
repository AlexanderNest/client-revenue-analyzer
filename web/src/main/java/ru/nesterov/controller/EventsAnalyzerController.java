package ru.nesterov.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.EventResponse;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.service.dto.IncomeAnalysisResult;

import java.util.List;
import java.util.Map;

@Api(tags = "Анализ событий")
@RequestMapping("/events/analyzer")
public interface EventsAnalyzerController {

    @ApiOperation(value = "Получить статистику встреч клиентов", notes = "Возвращает статистику встреч клиентов за указанный месяц")
    @PostMapping("/getClientsStatistics")
    Map<String, ClientMeetingsStatistic> getClientStatistics(@RequestBody GetForMonthRequest request);

    @ApiOperation(value = "Получить статусы событий за месяц", notes = "Возвращает количество событий по их статусам за указанный месяц")
    @PostMapping("/getEventsStatusesForMonth")
    Map<EventStatus, Integer> getEventsStatusesForMonth(@RequestBody GetForMonthRequest request);

    @ApiOperation(value = "Получить анализ доходов за месяц", notes = "Возвращает анализ доходов за указанный месяц")
    @PostMapping("/getIncomeAnalysisForMonth")
    IncomeAnalysisResult getIncomeAnalysisForMonth(@RequestBody GetForMonthRequest request);

    @ApiOperation(value = "Получить неоплаченные события", notes = "Возвращает список неоплаченных событий")
    @GetMapping("/getUnpaidEvents")
    List<EventResponse> getUnpaidEvents();
}
