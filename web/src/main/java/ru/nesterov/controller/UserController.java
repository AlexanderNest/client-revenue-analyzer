package ru.nesterov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.dto.CheckUserForExistenceInDbRequest;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;

@Tag(name = "Управление пользователями", description = "API для управления пользователями")
@RequestMapping("/user")
public interface UserController {
    @Operation(
            summary = "Создать нового пользователя",
            description = "Сохраняет информацию о пользователе в базе данных",
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
            summary = "Проверить, зарегистрирован ли пользователь в приложении",
            description = "Проверяет существование пользователя в базе данных",
            requestBody = @RequestBody(
                    description = "Идентификатор пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CheckUserForExistenceInDbRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            }
    )
    @PostMapping("/checkUserForExistenceInDB")
    Boolean checkUserForExistenceInDB(CheckUserForExistenceInDbRequest request);
}
