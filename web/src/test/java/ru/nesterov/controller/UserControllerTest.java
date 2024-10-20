package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @MockBean
    private GoogleCalendarClient googleCalendarService;

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
        User user = new User();
        user.setId(1);
        user.setUsername("user");
        user.setMainCalendar("111111");
        user.setCancelledCalendarEnabled(false);

        userRepository.save(user);

        GetUserRequest request = new GetUserRequest();
        request.setUsername("user");
        mockMvc.perform(
                post(GET_USER_URL)
                        .header("X-username", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.isCancelledCalendarEnabled").value(false))
                .andExpect(jsonPath("$.mainCalendarId").value("111111"));
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
