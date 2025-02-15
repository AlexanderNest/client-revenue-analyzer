package ru.nesterov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.controller.request.GetForMonthRequest;
import ru.nesterov.controller.response.GetClientAnalyticResponse;

@Tag(name = "AI Analyzer", description = "API для анализа данных о клиентах и генерации рекомендаций на основе статистики.")
@RequestMapping("/ai")
public interface AiAnalyzerController {
    @Operation(
            summary = "Генерация рекомендаций на основе статистики клиентов",
            description = "Анализирует данные клиентов за указанный период и возвращает рекомендации для оптимизации сотрудничества."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный анализ и генерация рекомендаций",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetClientAnalyticResponse.class))),
    })

    @PostMapping("/generateRecommendation")
    GetClientAnalyticResponse analyzeClients(@RequestHeader(name = "X-username") String username, @RequestBody GetForMonthRequest request);
}
