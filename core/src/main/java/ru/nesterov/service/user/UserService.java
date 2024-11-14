package ru.nesterov.service.user;

import ru.nesterov.service.dto.UserDto;

public interface UserService {
    UserDto getUserByUsername(String username);
    UserDto createUser(UserDto userDto);
}
