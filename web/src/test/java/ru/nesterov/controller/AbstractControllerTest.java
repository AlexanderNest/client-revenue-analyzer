package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.repository.UserSettingsRepository;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.dto.UserSettingsDto;
import ru.nesterov.service.user.UserService;

@AutoConfigureMockMvc
@SpringBootTest
public abstract class AbstractControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ClientRepository clientRepository;
    @Autowired
    protected UserSettingsRepository userSettingsRepository;
    @Autowired
    private UserService userService;

    @MockBean
    protected GoogleCalendarClient googleCalendarClient;

    protected UserDto createUserWithEnabledSettings(String username) {
        UserDto userDto = UserDto.builder()
                .username(username)
                .mainCalendar("someCalendar1")
                .userSettings(
                        UserSettingsDto.builder()
                        .isCancelledCalendarEnabled(false)
                        .isEventsBackupEnabled(false)
                        .build()
                )
                .build();
        return userService.createUser(userDto);
    }

}
