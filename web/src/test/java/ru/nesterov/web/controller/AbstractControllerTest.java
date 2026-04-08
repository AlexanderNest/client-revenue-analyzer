package ru.nesterov.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nesterov.calendar.integration.google.GoogleCalendarClient;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.PriceChangeHistory;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.PriceChangeHistoryRepository;
import ru.nesterov.core.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

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
    @Autowired
    protected PriceChangeHistoryRepository priceChangeHistoryRepository;

    @MockitoBean
    protected GoogleCalendarClient googleCalendarClient;

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
        return saveClientWithPriceHistory(client1, 1000);
    }

    protected PriceChangeHistory createPriceHistory(int price) {
        PriceChangeHistory priceChangeHistory = new PriceChangeHistory();
        priceChangeHistory.setPrice(price);
        priceChangeHistory.setChangeDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        return priceChangeHistory;
    }

    protected Client saveClientWithPriceHistory(Client client, int price) {
        Client savedClient = clientRepository.save(client);
        PriceChangeHistory priceChangeHistory = createPriceHistory(price);
        priceChangeHistory.setClientId(savedClient.getId());
        priceChangeHistoryRepository.save(priceChangeHistory);
        return savedClient;
    }
}
