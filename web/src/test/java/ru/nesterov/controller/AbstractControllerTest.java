package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.entity.UserSettings;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.repository.UserSettingsRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.client.ClientService;
import ru.nesterov.service.dto.ClientDto;
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
    private CalendarService calendarService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClientService clientService;

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
