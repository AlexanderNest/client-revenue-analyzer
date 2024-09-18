package ru.nesterov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.entity.User;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return convert(user);
    }

    private UserDto convert(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .cancelledCalendar(user.getCancelledCalendar())
                .mainCalendar(user.getMainCalendar())
                .isCancelledCalendarEnabled(user.isCancelledCalendarEnabled())
                .build();
    }
}
