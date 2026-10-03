package ru.nesterov.web.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import ru.nesterov.core.entity.Role;
import ru.nesterov.core.entity.User;
import ru.nesterov.web.controller.AbstractControllerTest;
import ru.nesterov.web.controller.request.user.GetAllUsersByRoleAndSourceRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = ("app.secret-token.enabled=true"))
public class WebSecurityTest extends AbstractControllerTest {
    private final String TEST_URL = "/events/analyzer/getUnpaidEvents";
    private final String HEADER = "X-secret-token";

    private final static String USERNAME = "webSecurityUsername";

    @Test
    public void securityTestUnauthorized() throws Exception {
        createUser(1, null);
        mockMvc.perform(get(TEST_URL)
                        .header("X-username", USERNAME + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(401));
    }
    
    @Test
    public void securityTestAuthorized() throws Exception {
        createUser(2, Role.USER);
        mockMvc.perform(get(TEST_URL)
                        .header(HEADER, "secret-token")
                        .header("X-username", USERNAME + 2)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void securityTestAuthorizedForAdmin() throws Exception {
        createUser(3, Role.ADMIN);

        GetAllUsersByRoleAndSourceRequest getAllUsersByRoleAndSourceRequest = new GetAllUsersByRoleAndSourceRequest();
        getAllUsersByRoleAndSourceRequest.setRole(Role.ADMIN);
        getAllUsersByRoleAndSourceRequest.setSource("testSource");

        mockMvc.perform(post("/user/getUsersIdByRoleAndSource")
                        .header(HEADER, "secret-token")
                        .header("X-username", USERNAME + 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getAllUsersByRoleAndSourceRequest))
                )
                .andExpect(status().isOk());
    }

    private void createUser(int id, Role role) {
        User user = new User();
        user.setUsername(USERNAME + id);
        user.setMainCalendar("mainCalendarId");
        user.setCancelledCalendar("cancelCalendarId");
        user.setRole(role);

        userRepository.save(user);
    }
}
