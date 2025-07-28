package ru.nesterov.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.calendar.integration.google.GoogleCalendarClient;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.UserRepository;

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

    protected User createUser(String username) {
        User user1 = new User();
        user1.setUsername(username);
        user1.setMainCalendar("someCalendar1");

        User saved = userRepository.save(user1);
        System.out.println("➡️ СОЗДАНИЕ user: " + saved.getUsername() + ", mainCalendar: " + saved.getMainCalendar());
        return saved;

    //    return userRepository.save(user1);
    }

    protected Client createClient(String name, User user) {
        Client client1 = new Client();
        client1.setUser(user);
        client1.setName(name);
        client1.setPricePerHour(1000);

        Client saved = clientRepository.save(client1);
        System.out.println("➡️ СОЗДАНИЕ client: " + saved.getName() + " for user: " + user.getUsername());
        return saved;
      //  return clientRepository.save(client1);
    }
}
