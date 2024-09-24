package ru.nesterov.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.dto.CheckUserForExistenceResponse;
import ru.nesterov.dto.CheckUserForExistenceRequest;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.mapper.UserMapper;
import ru.nesterov.service.user.UserService;

@RestController
@AllArgsConstructor
public class UserControllerImpl implements UserController{
    private UserService userService;
    private final UserMapper userMapper = new UserMapper();

    public CreateUserResponse createUser(@RequestBody CreateUserRequest request) {
        return userMapper.mapToCreateUserResponse(userService.createUser(userMapper.mapToUserDto(request)));
    }

    public CheckUserForExistenceResponse checkUserForExistence(@RequestBody CheckUserForExistenceRequest request) {
        CheckUserForExistenceResponse response = new CheckUserForExistenceResponse();
        response.setPresent(userService.getUserByUsername(request.getUserIdentifier()) != null);
        return response;
    }
}
