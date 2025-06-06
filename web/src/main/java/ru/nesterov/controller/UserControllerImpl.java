package ru.nesterov.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.bot.dto.CreateUserRequest;
import ru.nesterov.bot.dto.CreateUserResponse;
import ru.nesterov.controller.request.GetUserRequest;
import ru.nesterov.controller.response.GetUserResponse;
import ru.nesterov.mapper.UserMapper;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.user.UserService;

@RestController
@AllArgsConstructor
public class UserControllerImpl implements UserController{
    private UserService userService;
    private final UserMapper userMapper = new UserMapper();

    public CreateUserResponse createUser(@RequestBody CreateUserRequest request) {
        UserDto userDto = userService.createUser(userMapper.mapToUserDto(request));
        return userMapper.mapToCreateUserResponse(userDto);
    }

    public ResponseEntity<GetUserResponse> getUserByUsername(@RequestBody GetUserRequest request) {
        UserDto userDto = userService.getUserByUsername(request.getUsername());
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        GetUserResponse response = userMapper.mapToGetUserResponse(userDto);
        return ResponseEntity.ok(response);
    }
}
