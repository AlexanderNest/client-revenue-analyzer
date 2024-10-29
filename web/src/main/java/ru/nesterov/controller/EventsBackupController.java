package ru.nesterov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.controller.response.ResponseWithMessage;

@Tag(name = "Бэкап событий", description = "API для бэкапа событий")
@RequestMapping("/events/backup")
public interface EventsBackupController {
    
    @Operation(
            summary = "Создать бэкап событий",
            description = "Возвращает количество сохранённых событий",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @GetMapping
    ResponseEntity<ResponseWithMessage> makeBackup(@RequestHeader(name = "X-username") String username);
}
