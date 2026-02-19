package ru.nesterov.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.request.CreateUserRequest;
import ru.nesterov.web.controller.request.GetAllUsersByRoleAndSourceRequest;
import ru.nesterov.web.controller.request.GetUserRequest;
import ru.nesterov.web.controller.response.CreateUserResponse;
import ru.nesterov.web.controller.response.GetUserIdsResponse;
import ru.nesterov.web.controller.response.GetUserResponse;

@Tag(name = "Управление пользователями", description = "API для управления пользователями")
@RequestMapping("/user")
public interface UserController {

    @Operation(
            summary = "Создать нового пользователя",
            description = "Сохраняет информацию о пользователе в базе данных. После добавления пользователь сможет сохранять своих клиентов",
            requestBody = @RequestBody(
                    description = "Запрос с информацией о пользователе",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/createUser")
    CreateUserResponse createUser(CreateUserRequest request);

    @Operation(
            summary = "Получить информацию о пользователе",
            description = "Выдает информацию о пользователе по идентификатору пользователя",
            requestBody = @RequestBody(
                    description = "Запрос с идентификатором пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GetUserRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getUserByUsername")
    ResponseEntity<GetUserResponse> getUserByUsername(GetUserRequest request);


    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей по роли и по источнику создания пользователя",
            requestBody = @RequestBody(
                    description = "Запрос с ролью и источником",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GetAllUsersByRoleAndSourceRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/getUsersIdByRoleAndSource")
    GetUserIdsResponse getUsersIdByRoleAndSource(GetAllUsersByRoleAndSourceRequest getAllUsersByRoleAndSourceRequest);
}
