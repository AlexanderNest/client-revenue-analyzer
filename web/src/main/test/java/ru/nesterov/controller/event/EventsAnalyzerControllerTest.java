package ru.nesterov.controller.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.dto.Event;
import ru.nesterov.entity.Client;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.CalendarService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventsAnalyzerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private CalendarService calendarService;


    @BeforeEach
    void init() {
        Client client1 = new Client();
        client1.setId(1);
        client1.setName("testName1");
        client1.setPricePerHour(1000);
        when(clientRepository.findClientByName("testName1")).thenReturn(client1);

        Client client2 = new Client();
        client2.setId(1);
        client2.setName("testName2");
        client2.setPricePerHour(1000);
        when(clientRepository.findClientByName("testName2")).thenReturn(client2);

        Event event1 = Event.builder()
                .summary("testName1")
                .colorId("1")
                .start(LocalDateTime.of(2024, 8, 9, 11, 30))
                .end(LocalDateTime.of(2024, 8, 9, 12, 30))
                .build();

        Event event2 = Event.builder()
                .summary("testName1")
                .colorId("1")
                .start(LocalDateTime.of(2024, 8, 10, 11, 30))
                .end(LocalDateTime.of(2024, 8, 10, 12, 30))
                .build();

        Event event3 = Event.builder()
                .summary("testName2")
                .colorId("6")
                .start(LocalDateTime.of(2024, 8, 11, 11, 30))
                .end(LocalDateTime.of(2024, 8, 11, 12, 30))
                .build();

        Event event4 = Event.builder()
                .summary("testName1")
                .colorId("6")
                .start(LocalDateTime.of(2024, 8, 12, 11, 30))
                .end(LocalDateTime.of(2024, 8, 12, 12, 30))
                .build();

        Event event5 = Event.builder()
                .summary("testClient2")
                .colorId("4")
                .start(LocalDateTime.of(2024, 8, 13, 11, 30))
                .end(LocalDateTime.of(2024, 8, 13, 12, 30))
                .build();

        when(calendarService.getEventsBetweenDates(any(), any())).thenReturn(List.of(event1, event2, event3, event4, event5));
    }

    @Test
    void getUnpaidEvents() throws Exception {
        LocalDateTime expectedEventStart1 = LocalDateTime.of(2024, 8, 11, 11, 30, 00);
        LocalDateTime expectedEventStart2 = LocalDateTime.of(2024, 8, 12, 11, 30, 00);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        mockMvc.perform(get("/events/analyzer/getUnpaidEvents")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].summary").value("testName2"))
                .andExpect(jsonPath("$[0].eventStart").value(expectedEventStart1.format(formatter)))
                .andExpect(jsonPath("$[1].summary").value("testName1"))
                .andExpect(jsonPath("$[1].eventStart").value(expectedEventStart2.format(formatter)));
    }
}