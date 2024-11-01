package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.controller.request.GetForYearRequest;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.google.CalendarClient;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserAnalyzerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    @MockBean
    private CalendarService calendarService;
    @MockBean
    private CalendarClient calendarClient;

    private static final String GET_YEAR_STATISTICS_URL = "/user/analyzer/getYearBusynessStatistics";

    @Test
    void getYearStatistics() throws Exception {
        User user = new User();
        user.setUsername("testUser1");
        user.setMainCalendar("someCalendar1");
        userRepository.save(user);

        Client client1 = new Client();
        client1.setUser(user);
        client1.setName("testName1");
        client1.setPricePerHour(1000);
        clientRepository.save(client1);

        EventDto event1 = EventDto.builder()
                .summary("paid1")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 12, 12, 30))
                .end(LocalDateTime.of(2024, 8, 12, 15, 0))
                .build();

        EventDto event2 = EventDto.builder()
                .summary("paid1")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 14, 12, 45))
                .end(LocalDateTime.of(2024, 8, 14, 20, 0))
                .build();

        when(calendarService.getEventsBetweenDates(eq("someCalendar1"), any(), anyBoolean(), any(), any())).thenReturn(List.of(event1, event2));

        GetForYearRequest getForYearRequest = new GetForYearRequest();
        getForYearRequest.setYear(2024);

        mockMvc.perform(
                        post(GET_YEAR_STATISTICS_URL)
                                .header("X-username", user.getUsername())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(getForYearRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months.Август").value(9.75))
                .andExpect(jsonPath("$.days.Среда").value(7.25))
                .andExpect(jsonPath("$.days.Понедельник").value(2.5));
    }
}