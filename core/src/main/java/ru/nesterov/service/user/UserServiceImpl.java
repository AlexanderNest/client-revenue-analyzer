package ru.nesterov.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.entity.User;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.dto.UserDto;

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

    public List<Long> getAllUsersId() {
        return userRepository.findAllUserIds();
    }
}
