package ru.nesterov.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;

@RequestMapping("/user")
public interface UserController {

    @PostMapping("/createUser")
    CreateUserResponse createUser(CreateUserRequest request);
}
