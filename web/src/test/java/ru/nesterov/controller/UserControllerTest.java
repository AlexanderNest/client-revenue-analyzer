package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.dto.CheckUserForExistenceInDbRequest;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    private static final String CHECK_USER_URL = "/user/checkUserForExistenceInDB";


    @Test
    @Transactional
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
    @Transactional
    void createTheSameUser() throws Exception {
        User user = new User();
        user.setId(1);
        user.setUsername("user");
        user.setMainCalendar("111111");
        user.setCancelledCalendarEnabled(false);

        userRepository.save(user);

        CheckUserForExistenceInDbRequest request = new CheckUserForExistenceInDbRequest();
        request.setUserIdentifier("user");
        mockMvc.perform(
                post(CHECK_USER_URL)
                        .header("X-username", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
