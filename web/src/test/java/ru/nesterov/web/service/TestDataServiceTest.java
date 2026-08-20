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
import ru.nesterov.core.service.testdata.TestDataService;

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

        testDataService.deleteTestData(user.getUsername());

        assertTrue(clientRepository.getClientsByUserId(user.getId()).isEmpty());

        verify(googleCalendarClient).deleteEvents(
                eq(user.getMainCalendar()),
                argThat((List<String> list) ->
                        list.size() == expectedEventIds.size() && list.containsAll(expectedEventIds)
                )
        );

    }

    private TestDataCreationStatus tryToCreateTestData(String username) {
        TestDataCreationStatus status = TestDataCreationStatus.LIMIT_NOT_REACHED;

        while (status == TestDataCreationStatus.LIMIT_NOT_REACHED) {
            status = testDataService.tryToCreateTestData(username);
        }

        return status;
    }

}
