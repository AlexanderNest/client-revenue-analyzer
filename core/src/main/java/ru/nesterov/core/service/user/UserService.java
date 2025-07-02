package ru.nesterov.core.service.user;

import ru.nesterov.core.service.dto.UserDto;

public interface UserService {
    UserDto getUserByUsername(String username);
    UserDto createUser(UserDto userDto);
}
