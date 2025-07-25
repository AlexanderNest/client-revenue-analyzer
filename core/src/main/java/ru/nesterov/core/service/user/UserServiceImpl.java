package ru.nesterov.core.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.dto.UserDto;

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

    private UserDto convert(User user) {
        if (user == null) { return null; }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .cancelledCalendar(user.getCancelledCalendar())
                .mainCalendar(user.getMainCalendar())
                .isCancelledCalendarEnabled(user.isCancelledCalendarEnabled())
                .build();
    }

    private User convert(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setMainCalendar(userDto.getMainCalendar());
        user.setCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled());
        user.setCancelledCalendar(userDto.getCancelledCalendar());

        return user;
    }
}
