package ru.nesterov.web.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import ru.nesterov.controller.AbstractControllerTest;
import ru.nesterov.entity.User;
import ru.nesterov.entity.UserSettings;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = ("app.secret-token.enabled=true"))
public class WebSecurityTest extends AbstractControllerTest {
    private final String TEST_URL = "/events/analyzer/getUnpaidEvents";
    private final String HEADER = "X-secret-token";

    private final static String USERNAME = "webSecurityUsername";

    @Test
    public void securityTestUnauthorized() throws Exception {
        createUserWithEnabledSettings(USERNAME + 1);
        mockMvc.perform(get(TEST_URL)
                        .header("X-username", USERNAME + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(401));
    }
    
    @Test
    public void securityTestAuthorized() throws Exception {
        createUserWithEnabledSettings(USERNAME + 2);
        mockMvc.perform(get(TEST_URL)
                        .header(HEADER, "secret-token")
                        .header("X-username", USERNAME + 2)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

}
