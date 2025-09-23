package ru.nesterov.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.request.CreateClientRequest;
import ru.nesterov.web.controller.request.GetClientScheduleRequest;
import ru.nesterov.web.controller.request.UpdateClientRequest;
import ru.nesterov.web.controller.response.ClientResponse;
import ru.nesterov.web.controller.response.EventScheduleResponse;

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
    ClientResponse createClient(@RequestHeader(name = "X-username") String username, @RequestBody CreateClientRequest createClientRequest);

    @Operation(
            summary = "Вывод информации об активных клиентах",
            description = "Возвращает всю информацию об активных клиентах в порядке убывания стоимости",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getActiveClients")
    List<ClientResponse> getActiveClients(@RequestHeader(name = "X-username") String username);

    @Operation(
            summary = "Удалить клиента",
            description = "Удаляет клиента",
            requestBody = @RequestBody(
                    description = "Запрос для удаления клиента",
                    required = true
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @DeleteMapping("/{clientName}")
    void deleteClient(@RequestHeader(name = "X-username") String username, @PathVariable String clientName);


    @Operation(
           summary = "Обновить данные клиента",
           description = "Обновляет данные клиента и возвращает данные о нем",
            requestBody = @RequestBody(
                    description = "Запрос для обновление клиента",
                    required = true // описать pathVar
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/update")
    ClientResponse updateClient(@RequestHeader(name = "X-username") String username, @RequestBody UpdateClientRequest updateClientRequest);
}