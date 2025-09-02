package ru.nesterov.core.service.user;

import ru.nesterov.core.entity.Role;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.dto.UserIdsDto;

import java.util.List;

public interface UserService {
    UserDto getUserByUsername(String username);
    UserDto createUser(UserDto userDto);

    List<UserIdsDto> getUsersIdByRoleAndSource(Role role, String source);
}
