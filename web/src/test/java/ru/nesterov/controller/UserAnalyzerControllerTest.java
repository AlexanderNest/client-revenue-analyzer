package ru.nesterov.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.nesterov.controller.request.GetForYearRequest;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.service.client.ClientService;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAnalyzerControllerTest extends AbstractControllerTest {
    @Autowired
    private ClientService clientService;
    private static final String GET_YEAR_STATISTICS_URL = "/user/analyzer/getYearBusynessStatistics";

    @Test
    void getYearStatistics() throws Exception {
        UserDto user = createUserWithEnabledSettings("UACT_testUser1");
        ClientDto clientDto1 = ClientDto.builder()
                .active(true)
                .name("testName2")
                .description("aa")
                .pricePerHour(1000)
                .build();
        ClientDto client1 = clientService.createClient(user, clientDto1, false);

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

        when(googleCalendarClient.getEventsBetweenDates(eq("someCalendar1"), anyBoolean(), any(), any())).thenReturn(List.of(eventDto1, eventDto2));

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