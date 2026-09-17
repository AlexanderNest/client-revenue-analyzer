package ru.nesterov.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.request.GetForYearRequest;
import ru.nesterov.web.controller.response.YearBusynessStatisticsResponse;


@Tag(name = "Анализатор пользователей", description = "API для анализа пользователей")
@RequestMapping("/user/analyzer")
public interface UserAnalyzerController {
    @Operation(
            summary = "Получить расчет занятости за год",
            description = "Возвращает фактическую занятость пользователя за указанный год, отсортированную по убыванию загруженности",
            requestBody = @RequestBody(
                    description = "Запрос для получения статистики за год",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GetForYearRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getYearBusynessStatistics")
    YearBusynessStatisticsResponse getYearBusynessStatistics(@RequestHeader(name = "X-username") String username, @RequestBody GetForYearRequest getForYearRequest);
}
