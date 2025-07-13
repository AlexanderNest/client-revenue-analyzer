package ru.nesterov.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.IncomeAnalysisResult;
import ru.nesterov.web.controller.request.GetForMonthRequest;
import ru.nesterov.web.controller.response.ClientMeetingsStatisticResponse;
import ru.nesterov.web.controller.response.EventResponse;

import java.util.List;
import java.util.Map;

@Tag(name = "Анализатор событий", description = "API для анализа событий")
@RequestMapping("/events/analyzer")
public interface EventsAnalyzerController {

    @Operation(
            summary = "Получить статистику клиентов",
            description = "Возвращает статистику встреч клиентов за указанный месяц",
            requestBody = @RequestBody(
                    description = "Запрос для получения статистики за месяц",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GetForMonthRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getClientsStatistics")
    Map<String, ClientMeetingsStatistic> getClientsStatistics(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request);

    @Operation(
            summary = "Получить статистику клиента",
            description = "Возвращает статистику встреч клиента за два года",
            requestBody = @RequestBody(
                    description = "Запрос для получения статистики за два года",
                    required = true,
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/getClientStatistic")
    ClientMeetingsStatisticResponse getClientStatistic(@RequestHeader(name = "X-username") String username, @RequestParam("clientName") String clientName);

    @Operation(
            summary = "Получить статусы событий за месяц",
            description = "Возвращает количество событий по их статусам за указанный месяц",
            requestBody = @RequestBody(
                    description = "Запрос для получения статусов событий за месяц",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GetForMonthRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getEventsStatusesForMonth")
    Map<EventStatus, Integer> getEventsStatusesForMonth(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request);

    @Operation(
            summary = "Получить анализ доходов за месяц",
            description = "Возвращает анализ доходов за указанный месяц",
            requestBody = @RequestBody(
                    description = "Запрос для получения анализа доходов за месяц",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GetForMonthRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getIncomeAnalysisForMonth")
    ResponseEntity<IncomeAnalysisResult> getIncomeAnalysisForMonth(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request);

    @Operation(
            summary = "Получить неоплаченные события",
            description = "Возвращает список неоплаченных событий",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping("/getUnpaidEvents")
    List<EventResponse> getUnpaidEvents(@RequestHeader(name = "X-username") String username);
}