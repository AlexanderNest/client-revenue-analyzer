package ru.nesterov.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.nesterov.common.dto.EventDto;
import ru.nesterov.common.dto.EventStatus;
import ru.nesterov.controller.request.GetForYearRequest;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAnalyzerControllerTest extends AbstractControllerTest {
    private static final String GET_YEAR_STATISTICS_URL = "/user/analyzer/getYearBusynessStatistics";

    @Test
    void getYearStatistics() throws Exception {
        User user = new User();
        user.setUsername("UACT_testUser1");
        user.setMainCalendar("someCalendar1");
        userRepository.save(user);

        Client client1 = new Client();
        client1.setUser(user);
        client1.setName("testName2");
        client1.setPricePerHour(1000);
        clientRepository.save(client1);

        EventDto eventDto1 = EventDto.builder()
                .summary("paid1")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 12, 12, 30))
                .end(LocalDateTime.of(2024, 8, 12, 15, 0))
                .build();

        EventDto eventDto2 = EventDto.builder()
                .summary("paid1")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 14, 12, 45))
                .end(LocalDateTime.of(2024, 8, 14, 20, 0))
                .build();

        when(googleCalendarClient.getEventsBetweenDates(eq("someCalendar1"), any(), any(), any())).thenReturn(List.of(eventDto1, eventDto2));

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