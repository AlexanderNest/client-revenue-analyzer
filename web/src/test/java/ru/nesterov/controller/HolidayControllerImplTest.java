package ru.nesterov.controller;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nesterov.controller.request.GetHolidaysRequest;

import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.google.GoogleCalendarService;

@ExtendWith(MockitoExtension.class)
public class HolidayControllerImplTest extends AbstractControllerTest {
    @Mock
    private GoogleCalendarService googleCalendarService;

    @InjectMocks
    private HolidayControllerImpl holidayController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(holidayController).build();
    }

    @Test
    public void testGetHolidays() throws Exception {
        // Arrange
        GetHolidaysRequest request = new GetHolidaysRequest();
        request.setLeftDateStr("2023-01-01 00:00");
        request.setRightDateStr("2023-01-31 23:59");

        LocalDateTime leftDate = LocalDateTime.parse(request.getLeftDateStr(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime rightDate = LocalDateTime.parse(request.getRightDateStr(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        EventExtensionDto eventExtensionDto = new EventExtensionDto();

        EventDto event1 = EventDto.builder()
                .status(EventStatus.SUCCESS)
                .summary("3000")
                .start(LocalDateTime.parse("2024-12-31 22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .end(LocalDateTime.parse("2025-01-21 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .eventExtensionDto(eventExtensionDto)
                .build();

        EventDto event2 = EventDto.builder()
                .status(EventStatus.PLANNED)
                .summary("2000")
                .start(LocalDateTime.parse("2024-11-31 22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .end(LocalDateTime.parse("2025-02-21 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .eventExtensionDto(eventExtensionDto)
                .build();

        List<EventDto> expectedEvents = Arrays.asList(event1, event2);

        when(googleCalendarService.getHolidays(leftDate, rightDate)).thenReturn(expectedEvents);

        // Act & Assert
        mockMvc.perform(get("/revenue-analyzer/holidayDay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(EventStatus.SUCCESS))
                .andExpect(jsonPath("$[0].summery").value("3000"))
                .andExpect(jsonPath("$[0].start").value("2024-12-31 22:00"))
                .andExpect(jsonPath("$[0].end").value("2025-01-21 12:00"))
                .andExpect(jsonPath("$[0].eventExtensionDto").value(""))
                .andExpect(jsonPath("$[1].status").value(EventStatus.PLANNED))
                .andExpect(jsonPath("$[1].summery").value("2000"))
                .andExpect(jsonPath("$[1].start").value("2024-11-31 22:00"))
                .andExpect(jsonPath("$[1].end").value("2025-02-21 12:00"))
                .andExpect(jsonPath("$[1].eventExtensionDto").value(""));

        verify(googleCalendarService, times(1)).getHolidays(leftDate, rightDate);
    }
}
