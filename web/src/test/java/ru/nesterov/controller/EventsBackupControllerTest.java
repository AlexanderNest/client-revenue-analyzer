package ru.nesterov.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.User;
import ru.nesterov.google.CalendarClient;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.calendar.events.backup.enable=true"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class EventsBackupControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    
    @MockBean
    private GoogleCalendarService googleCalendarService;
    @MockBean
    private CalendarClient calendarClient;
    
    @BeforeEach
    public void init() {
        User user = new User();
        user.setId(1);
        user.setUsername("testUsername");
        user.setMainCalendar("testCalendar");
        user.setEventsBackupEnabled(true);
        
        userRepository.save(user);
        
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        EventDto eventDto1 = EventDto.builder()
                .summary("event1")
                .status(EventStatus.SUCCESS)
                .start(start.minusDays(10))
                .end(end.minusDays(10))
                .build();
        
        EventDto eventDto2 = EventDto.builder()
                .summary("event2")
                .status(EventStatus.SUCCESS)
                .start(start.minusDays(5))
                .end(end.minusDays(5))
                .build();
        
        when(googleCalendarService.getEventsBetweenDates(any(), any(), anyBoolean(), any(), any()))
                .thenReturn(List.of(eventDto1, eventDto2));
    }
    
    @Test
//    @Order(1)
    public void makeSuccessfulBackup() throws Exception {
        mockMvc.perform(get("/events/backup")
                        .header("X-username", "testUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backupDone").value("true"))
                .andExpect(jsonPath("$.eventsSaved").value(2));
    }
    
    @Test
//    @Order(2)
    public void makeFailedBackup() throws Exception {
        mockMvc.perform(get("/events/backup")
                        .header("X-username", "testUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backupDone").value("true"))
                .andExpect(jsonPath("$.eventsSaved").value(2));
        
        mockMvc.perform(get("/events/backup")
                        .header("X-username", "testUsername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backupDone").value("false"))
                .andExpect(jsonPath("$.eventsSaved").value(0));
    }
}
