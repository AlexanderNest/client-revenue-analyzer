package ru.nesterov.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.MediaType;
import ru.nesterov.bot.dto.GetForMonthRequest;
import ru.nesterov.calendar.integration.dto.CalendarType;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventExtensionDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventsAnalyzerControllerTest extends AbstractControllerTest {
    private final static String USERNAME = "testUser1";

    @BeforeEach
    void init() {
        User user = createUser(USERNAME);
        createClient("testName1", user);
        createClient("testName2", user);

        EventExtensionDto eventExtensionDto5 = new EventExtensionDto();
        eventExtensionDto5.setIsPlanned(true);
        EventExtensionDto eventExtensionDto6 = new EventExtensionDto();
        eventExtensionDto6.setIsPlanned(false);
        EventExtensionDto eventExtensionDto8 = new EventExtensionDto();
        eventExtensionDto6.setIsPlanned(false);

        EventDto eventDto1 = EventDto.builder()
                .summary("testName1")
                .status(EventStatus.PLANNED)
                .start(LocalDateTime.of(2024, 8, 9, 11, 30))
                .end(LocalDateTime.of(2024, 8, 9, 12, 30))
                .build();

        EventDto eventDto2 = EventDto.builder()
                .summary("testName1")
                .status(EventStatus.PLANNED)
                .start(LocalDateTime.of(2024, 8, 10, 11, 30))
                .end(LocalDateTime.of(2024, 8, 10, 12, 30))
                .build();

        EventDto eventDto3 = EventDto.builder()
                .summary("testName1")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 11, 11, 30))
                .end(LocalDateTime.of(2024, 8, 11, 12, 30))
                .build();

        EventDto eventDto4 = EventDto.builder()
                .summary("testName1")
                .status(EventStatus.REQUIRES_SHIFT)
                .start(LocalDateTime.of(2024, 8, 12, 11, 30))
                .end(LocalDateTime.of(2024, 8, 12, 12, 30))
                .build();

        EventDto eventDto5 = EventDto.builder()
                .summary("testName1")
                .status(EventStatus.CANCELLED)
                .start(LocalDateTime.of(2024, 8, 13, 11, 30))
                .end(LocalDateTime.of(2024, 8, 13, 12, 30))
                .eventExtensionDto(eventExtensionDto5)
                .build();

        EventDto eventDto6 = EventDto.builder()
                .summary("testName1")
                .status(EventStatus.CANCELLED)
                .start(LocalDateTime.of(2024, 8, 14, 11, 30))
                .end(LocalDateTime.of(2024, 8, 14, 12, 30))
                .eventExtensionDto(eventExtensionDto6)
                .build();

        EventDto eventDto7 = EventDto.builder()
                .summary("testName2")
                .status(EventStatus.CANCELLED)
                .start(LocalDateTime.of(2024, 8, 14, 11, 30))
                .end(LocalDateTime.of(2024, 8, 14, 12, 30))
                .eventExtensionDto(eventExtensionDto6)
                .build();

        EventDto eventDto8 = EventDto.builder()
                .summary("testName2")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 14, 11, 30))
                .end(LocalDateTime.of(2024, 8, 14, 12, 30))
                .eventExtensionDto(eventExtensionDto6)
                .build();

        when(googleCalendarClient.getEventsBetweenDates(eq("someCalendar1"), eq(CalendarType.MAIN), any(), any(), isNull())).thenReturn(List.of(eventDto1, eventDto2, eventDto3, eventDto4, eventDto5, eventDto6, eventDto7, eventDto8));
    }

    @AfterEach
    public void cleanup() {
        User user = userRepository.findByUsername(USERNAME);
        Client client1 = clientRepository.findClientByNameAndUserId("testName1", user.getId());
        clientRepository.delete(client1);
        Client client2 = clientRepository.findClientByNameAndUserId("testName2", user.getId());
        clientRepository.delete(client2);
        userRepository.delete(user);
    }

    @Test
    public void getUnpaidEvents() throws Exception {
        LocalDateTime expectedEventStart1 = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime expectedEventStart2 = LocalDateTime.of(2024, 8, 10, 11, 30);
        LocalDateTime expectedEventStart3 = LocalDateTime.of(2024, 8, 12, 11, 30);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        mockMvc.perform(get("/events/analyzer/getUnpaidEvents")
                        .header("X-username", "testUser1")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].summary").value("testName1"))
                .andExpect(jsonPath("$[0].eventStart").value(expectedEventStart1.format(formatter)))
                .andExpect(jsonPath("$[1].summary").value("testName1"))
                .andExpect(jsonPath("$[1].eventStart").value(expectedEventStart2.format(formatter)))
                .andExpect(jsonPath("$[2].summary").value("testName1"))
                .andExpect(jsonPath("$[2].eventStart").value(expectedEventStart3.format(formatter)));
    }

    @Test
    public void getClientsStatistics() throws Exception {
        GetForMonthRequest request = new GetForMonthRequest();
        request.setMonthName("august");
        mockMvc.perform(post("/events/analyzer/getClientsStatistics")
                        .header("X-username", "testUser1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testName1.successfulMeetingsHours").value(1))
                .andExpect(jsonPath("$.testName1.cancelledMeetingsHours").value(2))
                .andExpect(jsonPath("$.testName1.successfulEventsCount").value(1))
                .andExpect(jsonPath("$.testName1.plannedCancelledEventsCount").value(1))
                .andExpect(jsonPath("$.testName1.notPlannedCancelledEventsCount").value(1))
                .andExpect(jsonPath("$.testName1.incomePerHour").value(1000))
                .andExpect(jsonPath("$.testName1.actualIncome").value(1000))
                .andExpect(jsonPath("$.testName1.lostIncome").value(2000))
                .andExpect(jsonPath("$.testName2.successfulMeetingsHours").value(1))
                .andExpect(jsonPath("$.testName2.cancelledMeetingsHours").value(1))
                .andExpect(jsonPath("$.testName2.successfulEventsCount").value(1))
                .andExpect(jsonPath("$.testName2.plannedCancelledEventsCount").value(0))
                .andExpect(jsonPath("$.testName2.notPlannedCancelledEventsCount").value(1))
                .andExpect(jsonPath("$.testName2.incomePerHour").value(1000))
                .andExpect(jsonPath("$.testName2.actualIncome").value(1000))
                .andExpect(jsonPath("$.testName2.lostIncome").value(1000));
    }

    @Test
    public void getClientStatisticShouldReturnStatisticForOneClient() throws Exception {
        User user = createUser("myUser");

        Client client = createClient("oneStat", user);

        EventDto eventDto1 = EventDto.builder()
                .summary(client.getName())
                .status(EventStatus.PLANNED)
                .start(LocalDateTime.of(2024, 8, 9, 11, 30))
                .end(LocalDateTime.of(2024, 8, 9, 12, 30))
                .build();

        EventDto eventDto2 = EventDto.builder()
                .summary(client.getName())
                .status(EventStatus.CANCELLED)
                .start(LocalDateTime.of(2024, 8, 10, 11, 30))
                .end(LocalDateTime.of(2024, 8, 10, 12, 30))
                .build();

        EventDto eventDto3 = EventDto.builder()
                .summary(client.getName())
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 10, 11, 30))
                .end(LocalDateTime.of(2024, 8, 10, 12, 30))
                .build();

        when(googleCalendarClient.getEventsBetweenDates(eq("someCalendar1"), eq(CalendarType.MAIN), any(), any(), eq(client.getName()))).thenReturn(List.of(eventDto1, eventDto2, eventDto3));

        mockMvc.perform(get("/events/analyzer/getClientStatistic")
                        .header("X-username", "myUser")
                        .param("clientName", "oneStat")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(client.getName()))
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.description").value(client.getDescription()))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.serviceDuration").value(0))
                .andExpect(jsonPath("$.phone").isEmpty())
                .andExpect(jsonPath("$.successfulMeetingsHours").value(1.0))
                .andExpect(jsonPath("$.cancelledMeetingsHours").value(1.0))
                .andExpect(jsonPath("$.incomePerHour").value(1000))
                .andExpect(jsonPath("$.successfulEventsCount").value(1))
                .andExpect(jsonPath("$.plannedCancelledEventsCount").value(1))
                .andExpect(jsonPath("$.notPlannedCancelledEventsCount").value(0));

        clientRepository.delete(client);
        userRepository.delete(user);
    }
}
