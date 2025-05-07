package ru.nesterov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Event Manager", description = "API для управления событиями.")
@RequestMapping("/events")
public interface EventsController {

    @Operation(
            summary = "Перенос событий.",
            description = "Переносит отмененные события из основного календаря в календарь с отменными событиями."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный перенос событий"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/canceled/transfer")
    void transferEvents(@RequestHeader(name = "X-username") String username);
}
