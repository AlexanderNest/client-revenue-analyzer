package ru.nesterov.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.response.ResponseWithMessage;

@Tag(name = "Тестовые данные", description = "API для создания тестовых данных")
@RequestMapping("/test")
public interface TestDataController {

    @Operation(
            summary = "Создать тестовые данные",
            description = "Создание тестовых данных для получения готового тестового окружения",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/createTestData")
    ResponseWithMessage createTestData(@RequestHeader(name = "X-username") String username);

    @Operation(
            summary = "Удалить тестовые данные",
            description = "Удаление тестовых данных которые ранее были созданы для получения готового тестового окружения",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @DeleteMapping("/deleteTestData")
    ResponseWithMessage deleteTestData(@RequestHeader(name = "X-username") String username);
}
