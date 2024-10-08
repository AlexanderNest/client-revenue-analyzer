package ru.nesterov.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.entity.User;
import ru.nesterov.google.CalendarClient;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = ("app.secret-token.enabled=true"))
@SpringBootTest
@AutoConfigureMockMvc
public class WebSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CalendarService calendarService;
    @MockBean
    private CalendarClient calendarClient;
    @MockBean
    private UserRepository userRepository;

    private final String TEST_URL = "/events/analyzer/getUnpaidEvents";
    private final String HEADER = "X-secret-token";

    @BeforeEach
    public void setUp() throws Exception {
        User user = new User();
        user.setMainCalendar("mainCalendarId");
        user.setCancelledCalendar("cancelCalendarId");
        user.setId(1);

        when(userRepository.findByUsername(any())).thenReturn(user);
    }

    @Test
    public void securityTestUnauthorized() throws Exception {
        mockMvc.perform(get(TEST_URL)
                        .header("X-username", "username")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(401));
    }

    @Test
    public void securityTestAuthorized() throws Exception {
        mockMvc.perform(get(TEST_URL)
                        .header(HEADER, "secret-token")
                        .header("X-username", "username")
                        .contentType(MediaType.APPLICATION_JSON)
            )
                    .andExpect(status().isOk());
    }
}
