package ru.nesterov.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.nesterov.core.entity.Role;
import ru.nesterov.core.entity.User;
import ru.nesterov.web.controller.request.user.CreateUserRequest;
import ru.nesterov.web.controller.request.user.GetAllUsersByRoleAndSourceRequest;
import ru.nesterov.web.controller.request.user.GetUserRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends AbstractControllerTest {
    private static final String CREATE_USER_URL = "/user/createUser";
    private static final String GET_USER_URL = "/user/getUserByUsername";
    private static final String GET_ALL_USERS_BY_USERNAME_URL = "/user/getUsersIdByRoleAndSource";

    @Test
    void createNewUserRequest() throws Exception {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .userIdentifier("testUser")
                .mainCalendarId("mainCalendar")
                .isCancelledCalendarEnabled(false)
                .build();

        mockMvc.perform(
                post(CREATE_USER_URL)
                        .header("X-username", "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userIdentifier").value("testUser"))
                .andExpect(jsonPath("$.mainCalendarId").value("mainCalendar"))
                .andExpect(jsonPath("$.cancelledCalendarEnabled").value(false));
    }

    @Test
    void getUser() throws Exception {
        User user = createUser("user");

        GetUserRequest request = new GetUserRequest();
        request.setUsername("user");
        mockMvc.perform(
                post(GET_USER_URL)
                        .header("X-username", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.isCancelledCalendarEnabled").value(user.isCancelledCalendarEnabled()))
                .andExpect(jsonPath("$.mainCalendarId").value(user.getMainCalendar()));
    }

    @Test
    void getNonExistentUser() throws Exception {
        User user = createUser("existingUser");

        GetUserRequest request = new GetUserRequest();
        request.setUsername("111");
        mockMvc.perform(
                post(GET_USER_URL)
                        .header("X-username", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsernameForbiddenForRoleUser() throws Exception {
        User user = createUser("UserWithRoleUser");

        GetAllUsersByRoleAndSourceRequest getAllUsersByRoleAndSourceRequest = new GetAllUsersByRoleAndSourceRequest();
        getAllUsersByRoleAndSourceRequest.setRole(Role.USER);
        getAllUsersByRoleAndSourceRequest.setSource("testSource");

        mockMvc.perform(
                post(GET_ALL_USERS_BY_USERNAME_URL)
                        .header("X-username", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getAllUsersByRoleAndSourceRequest))
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserByUsernameAllowedForRoleAdmin() throws Exception{
        User user = new User();
        user.setUsername("UserWithRoleAdmin");
        user.setSource("telegram");
        user.setMainCalendar("someCalendar1");
        user.setRole(Role.ADMIN);

        userRepository.save(user);

        GetAllUsersByRoleAndSourceRequest getAllUsersByRoleAndSourceRequest = new GetAllUsersByRoleAndSourceRequest();
        getAllUsersByRoleAndSourceRequest.setRole(Role.USER);
        getAllUsersByRoleAndSourceRequest.setSource("testSource");

        mockMvc.perform(
                post(GET_ALL_USERS_BY_USERNAME_URL)
                        .header("X-username", user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getAllUsersByRoleAndSourceRequest))
        )
                .andExpect(status().isOk());
    }
}
