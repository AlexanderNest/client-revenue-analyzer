package ru.nesterov.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.nesterov.calendar.integration.dto.CreateEventDto;
import ru.nesterov.calendar.integration.dto.ResponseCreateEventDto;
import ru.nesterov.calendar.integration.google.GoogleCalendarClient;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.Role;
import ru.nesterov.core.entity.TestDataCreationStatus;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.ClientEventRepository;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.testdata.TestDataService;
import ru.nesterov.core.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.test.data.enabled=true", "app.test.data.client.limit=3"})
public class TestDataServiceTest {
    @Autowired
    private TestDataService testDataService;
    @MockitoBean
    private GoogleCalendarClient googleCalendarClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientEventRepository clientEventRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ClientService clientService;
    @Autowired
    private UserService userService;

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setMainCalendar("TestMainCalendar" + username);
        user.setCancelledCalendar(null);
        user.setCancelledCalendarEnabled(false);
        user.setRole(Role.ADMIN);
        user.setSource(null);

        return userRepository.save(user);
    }

    @BeforeEach
    public void mockGoogleCalendarCreation() {
        when(googleCalendarClient.createEvents(anyString(), any())).thenAnswer(invocation -> {
            List<CreateEventDto> requestedEvents = invocation.getArgument(1);
            return requestedEvents.stream()
                    .map(event -> ResponseCreateEventDto.builder()
                            .eventId(UUID.randomUUID().toString())
                            .summary(event.getSummary())
                            .build())
                    .collect(Collectors.toList());
        });
    }

    @Test
    public void createTestDataTest() {
        User user = createUser("createTestUser");

        TestDataCreationStatus status = tryToCreateTestData(user.getUsername());

        assertEquals(TestDataCreationStatus.CREATED, status);

        List<Client> createdClients = clientRepository.getClientsByUserId(user.getId());

        assertEquals(3, createdClients.size());

        for (Client client : createdClients) {
            List<String> eventIds = clientEventRepository.getEventIdsByClientId(client.getId());
            assertTrue(eventIds.size() >= 5 && eventIds.size() <= 10);
        }

        verify(googleCalendarClient).createEvents(eq(user.getMainCalendar()), any());
    }

    @Test
    public void deleteTestDataTest() {
        User user = createUser("deleteTestUser");

        tryToCreateTestData(user.getUsername());

        List<Client> createdClientsForDelete = clientRepository.getClientsByUserId(user.getId());

        List<String> expectedEventIds = new ArrayList<>();

        for (Client client : createdClientsForDelete) {
            List<String> eventIds = clientEventRepository.getEventIdsByClientId(client.getId());
            assertTrue(eventIds.size() >= 5 && eventIds.size() <= 10);
            expectedEventIds.addAll(eventIds);
        }

        ClientDto realClient = createRealClient(user, "Клиент созданный не для тестовых данных");

        testDataService.deleteTestData(user.getUsername());

        List<Client> remainingClients = clientRepository.getClientsByUserId(user.getId());

        assertEquals(1, remainingClients.size());
        assertEquals(realClient.getName(), remainingClients.getFirst().getName());
        assertTrue(clientEventRepository.getEventIdsByClientId(remainingClients.getFirst().getId()).isEmpty());

        verify(googleCalendarClient).deleteEvents(
                eq(user.getMainCalendar()),
                argThat((List<String> list) ->
                        list.size() == expectedEventIds.size() && list.containsAll(expectedEventIds)
                )
        );

    }

    private ClientDto createRealClient(User user, String name) {
        UserDto userDto = userService.getUserByUsername(user.getUsername());

        ClientDto clientDto = ClientDto.builder()
                .name(name)
                .description("Не тестовый клиент")
                .pricePerHour(1000)
                .active(true)
                .phone("89000000000")
                .build();

        return clientService.createClient(userDto, clientDto, false);
    }

    private TestDataCreationStatus tryToCreateTestData(String username) {
        TestDataCreationStatus status = TestDataCreationStatus.LIMIT_NOT_REACHED;

        while (status == TestDataCreationStatus.LIMIT_NOT_REACHED) {
            status = testDataService.tryToCreateTestData(username);
        }

        return status;
    }

}
