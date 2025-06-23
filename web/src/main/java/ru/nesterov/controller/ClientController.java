package ru.nesterov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.request.GetClientScheduleRequest;
import ru.nesterov.controller.response.ClientResponse;
import ru.nesterov.controller.response.EventScheduleResponse;
import ru.nesterov.controller.response.FullClientInfoResponse;

import java.util.List;

@Tag(name = "Управление клиентами", description = "API для управления клиентами")
@RequestMapping("/client")
public interface ClientController {

    @Operation(
            summary = "Получить расписание клиента",
            description = "Возвращает расписание событий для указанного клиента",
            requestBody = @RequestBody(
                    description = "Запрос для получения расписания клиента",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GetClientScheduleRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getSchedule")
    List<EventScheduleResponse> getClientSchedule(@RequestHeader(name = "X-username") String username, @RequestBody GetClientScheduleRequest request);

    @Operation(
            summary = "Создать клиента",
            description = "Создает нового клиента и возвращает информацию о нем",
            requestBody = @RequestBody(
                    description = "Запрос для создания нового клиента",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateClientRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/create")
    ResponseEntity<ClientResponse> createClient(@RequestHeader(name = "X-username") String username, @RequestBody CreateClientRequest createClientRequest);

    @Operation(
            summary = "Вывод информации об активных клиентах",
            description = "Возвращает всю информацию об активных клиентах в указанном порядке сортировки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getActiveClients")
    List<ClientResponse> getActiveClients(@RequestHeader(name = "X-username") String username);
}