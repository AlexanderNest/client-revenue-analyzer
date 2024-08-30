package ru.nesterov.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.google.CalendarClient;
import ru.nesterov.service.CalendarService;

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

    @Test
    @Transactional
    public void securityTestUnauthorized() throws Exception {
        mockMvc.perform(get("/events/analyzer/getUnpaidEvents")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(401));
    }

    @Test
    @Transactional
    public void securityTestAuthorized() throws Exception {
        mockMvc.perform(get("/events/analyzer/getUnpaidEvents").header("X-secret-token", "secret-token")
                .contentType(MediaType.APPLICATION_JSON)
            )
                    .andExpect(status().isOk());
    }
}
