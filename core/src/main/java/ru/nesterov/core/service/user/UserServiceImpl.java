package ru.nesterov.core.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.core.entity.Role;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.dto.UserIdsDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return convert(user);
    }

    public UserDto createUser(UserDto userDto) {
        User user = userRepository.save(convert(userDto));
        return convert(user);
    }

    public List<UserIdsDto> getUsersIdByRoleAndSource(Role role, String source) {
        List<String> userIds = userRepository.findUsersIdByRoleAndSource(role, source);
        return userIds.stream()
                .map(this::mapToUserIdsDto)
                .toList();
    }

    private UserIdsDto mapToUserIdsDto(String userId) {
        UserIdsDto userIdsDto = new UserIdsDto();
        userIdsDto.setId(userId);
        return userIdsDto;
    }

    private UserDto convert(User user) {
        if (user == null) { return null; }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .cancelledCalendar(user.getCancelledCalendar())
                .mainCalendar(user.getMainCalendar())
                .isCancelledCalendarEnabled(user.isCancelledCalendarEnabled())
                .role(user.getRole())
                .source(user.getSource())
                .build();
    }

    private User convert(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setMainCalendar(userDto.getMainCalendar());
        user.setCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled());
        user.setCancelledCalendar(userDto.getCancelledCalendar());
        user.setRole(Role.USER);
        user.setSource(userDto.getSource());

        return user;
    }
}
