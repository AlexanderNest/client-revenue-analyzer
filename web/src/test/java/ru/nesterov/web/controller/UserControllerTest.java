package ru.nesterov.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.nesterov.core.entity.User;
import ru.nesterov.web.controller.request.CreateUserRequest;
import ru.nesterov.web.controller.request.GetUserRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class UserControllerTest extends AbstractControllerTest {
    private static final String CREATE_USER_URL = "/user/createUser";
    private static final String GET_USER_URL = "/user/getUserByUsername";

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
        GetUserRequest request = new GetUserRequest();
        request.setUsername("111");
        mockMvc.perform(
                post(GET_USER_URL)
                        .header("X-username", "111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound());
    }
}
