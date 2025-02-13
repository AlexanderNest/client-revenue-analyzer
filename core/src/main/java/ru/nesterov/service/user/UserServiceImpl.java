package ru.nesterov.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.entity.User;
import ru.nesterov.entity.UserSettings;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.repository.UserSettingsRepository;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.dto.UserSettingsDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return convert(user);
    }

    public UserDto createUser(UserDto userDto) {
        User user = userRepository.save(convert(userDto));
        UserSettings userSettings = new UserSettings();
        userSettings.setUser(user);
        UserSettings savedUserSettings = userSettingsRepository.save(userSettings);
        user.setUserSettings(savedUserSettings);
        return convert(user);
    }

    private UserSettingsDto convertToUserSettingsDto(UserSettings userSettings){
        return UserSettingsDto.builder()
                .id(userSettings.getId())
                .isCancelledCalendarEnabled(userSettings.isCancelledCalendarEnabled())
                .isEventsBackupEnabled(userSettings.isEventsBackupEnabled())
                .build();
    }

    private UserSettings convertToUserSettings(UserSettingsDto userSettingsDto){
        UserSettings userSettings = new UserSettings();
        userSettings.setId(userSettingsDto.getId());
        userSettings.setCancelledCalendarEnabled(userSettingsDto.isCancelledCalendarEnabled());
        userSettings.setEventsBackupEnabled(userSettingsDto.isEventsBackupEnabled());
        return userSettings;
    }

    private UserDto convert(User user) {
        if (user == null) { return null; }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .cancelledCalendar(user.getCancelledCalendar())
                .mainCalendar(user.getMainCalendar())
                .userSettings(convertToUserSettingsDto(user.getUserSettings()))
                .build();
    }

    private User convert(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setMainCalendar(userDto.getMainCalendar());
        user.setUserSettings(convertToUserSettings(userDto.getUserSettings()));
        user.setCancelledCalendar(userDto.getCancelledCalendar());

        return user;
    }
}
