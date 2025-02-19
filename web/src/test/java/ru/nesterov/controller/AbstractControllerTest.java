package ru.nesterov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;

import java.time.LocalDateTime;
import java.time.ZoneId;

@AutoConfigureMockMvc
@SpringBootTest
public abstract class AbstractControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ClientRepository clientRepository;
    @Autowired
    private CalendarService calendarService;

    @MockBean
    protected GoogleCalendarClient googleCalendarClient;

    protected Event buildEvent(String color, String summary, String description, int startY, int startM, int startD, int startH, int startMin,
                             int endY, int endM, int endD, int endH, int endMin) {
        Event event = new Event();
        event.setColorId(color);
        event.setSummary(summary);
        event.setDescription(description);

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(java.util.Date.from(
                        LocalDateTime.of(startY, startM, startD, startH, startMin)
                                .atZone(ZoneId.systemDefault())
                                .toInstant())));

        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(java.util.Date.from(
                        LocalDateTime.of(endY, endM, endD, endH, endMin)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()))
                );

        event.setEnd(end);

        return event;
    }

    protected User createUser(String username) {
        User user1 = new User();
        user1.setUsername(username);
        user1.setMainCalendar("someCalendar1");

        return userRepository.save(user1);
    }

    protected Client createClient(String name, User user) {
        Client client1 = new Client();
        client1.setUser(user);
        client1.setName(name);
        client1.setPricePerHour(1000);
        return clientRepository.save(client1);
    }
}
