package ru.nesterov.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.nesterov.calendar.integration.dto.CreateEventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.ResponseCreateEventDto;
import ru.nesterov.calendar.integration.exception.CalendarIntegrationException;
import ru.nesterov.calendar.integration.google.GoogleCalendarClient;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.Role;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.exception.TestDataCreationException;
import ru.nesterov.core.exception.TestDataDeletionException;
import ru.nesterov.core.repository.ClientEventRepository;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.testdata.TestDataCreationStatus;
import ru.nesterov.core.service.testdata.TestDataService;
import ru.nesterov.core.service.user.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {"app.test.data.enabled=true", "app.test.data.client.limit=3"})
public class TestDataServiceTest {
    private static final int EXPECTED_CLIENTS_COUNT = 3;
    private static final int MIN_EVENTS_PER_CLIENT = 5;
    private static final int MAX_EVENTS_PER_CLIENT = 9;
    private static final int EVENT_DATE_RANGE_MONTHS = 3;

    private static final Set<EventStatus> ALLOWED_PAST_STATUSES = Set.of(
            EventStatus.SUCCESS,
            EventStatus.PLANNED_CANCELLED,
            EventStatus.UNPLANNED_CANCELLED,
            EventStatus.PLANNED,
            EventStatus.REQUIRES_SHIFT
    );

    private static final Set<EventStatus> ALLOWED_FUTURE_STATUSES = Set.of(
            EventStatus.PLANNED,
            EventStatus.PLANNED_CANCELLED
    );

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

    @BeforeEach
    public void mockGoogleCalendarCreation() {
        when(googleCalendarClient.createEvents(anyString(), any())).thenAnswer(invocation -> {
            List<CreateEventDto> requestedEvents = invocation.getArgument(1);
            return requestedEvents.stream()
                    .map(event -> ResponseCreateEventDto.builder()
                            .eventId(UUID.randomUUID().toString())
                            .clientId(event.getClientId())
                            .build())
                    .collect(Collectors.toList());
        });
    }

    @Test
    public void testDataIsCreatedOnlyOnThirdRequest() {
        User user = createUser("thirdRequestUser");

        assertEquals(TestDataCreationStatus.LIMIT_NOT_REACHED, testDataService.tryToCreateTestData(user.getUsername()));
        assertTrue(clientRepository.getClientsByUserId(user.getId()).isEmpty());

        assertEquals(TestDataCreationStatus.LIMIT_NOT_REACHED, testDataService.tryToCreateTestData(user.getUsername()));
        assertTrue(clientRepository.getClientsByUserId(user.getId()).isEmpty());

        verify(googleCalendarClient, never()).createEvents(eq(user.getMainCalendar()), any());

        assertEquals(TestDataCreationStatus.CREATED, testDataService.tryToCreateTestData(user.getUsername()));

        List<Client> createdClients = clientRepository.getClientsByUserId(user.getId());
        assertEquals(EXPECTED_CLIENTS_COUNT, createdClients.size());

        for (Client client : createdClients) {
            List<String> eventIds = clientEventRepository.getEventIdsByClientId(client.getId());
            assertTrue(eventIds.size() >= MIN_EVENTS_PER_CLIENT && eventIds.size() <= MAX_EVENTS_PER_CLIENT,
                    "Для клиента создано событий: " + eventIds.size());
        }

        verify(googleCalendarClient).createEvents(eq(user.getMainCalendar()), any());
    }

    /**
     * Счетчик запросов сбрасывается после успешного создания, поэтому следующая серия снова требует трех запросов
     */
    @Test
    public void requestCounterIsResetAfterCreation() {
        User user = createUser("counterResetUser");

        createTestData(user.getUsername());

        assertEquals(TestDataCreationStatus.LIMIT_NOT_REACHED, testDataService.tryToCreateTestData(user.getUsername()));
    }

    /**
     * Незавершенная серия запросов не должна вычищаться, пока не истек ее TTL
     */
    @Test
    public void cleanupKeepsUnfinishedSeries() {
        User user = createUser("cleanupUser");

        assertEquals(TestDataCreationStatus.LIMIT_NOT_REACHED, testDataService.tryToCreateTestData(user.getUsername()));
        assertEquals(TestDataCreationStatus.LIMIT_NOT_REACHED, testDataService.tryToCreateTestData(user.getUsername()));

        testDataService.cleanup();

        assertEquals(TestDataCreationStatus.CREATED, testDataService.tryToCreateTestData(user.getUsername()));
    }

    @Test
    public void createdEventsAreWithinThreeMonthsRangeAndHaveStatusesMatchingTheirDate() {
        User user = createUser("eventRangeUser");

        LocalDateTime beforeCreation = LocalDateTime.now(ZoneOffset.UTC);

        createTestData(user.getUsername());

        LocalDateTime afterCreation = LocalDateTime.now(ZoneOffset.UTC);

        Instant rangeStart = beforeCreation.minusMonths(EVENT_DATE_RANGE_MONTHS).toInstant(ZoneOffset.UTC);
        Instant rangeEnd = afterCreation.plusMonths(EVENT_DATE_RANGE_MONTHS).toInstant(ZoneOffset.UTC);

        List<CreateEventDto> createdEvents = captureCreatedEvents(user.getMainCalendar());
        assertFalse(createdEvents.isEmpty());

        Instant now = Instant.now();

        for (CreateEventDto event : createdEvents) {
            Instant start = Instant.parse(event.getStart());
            Instant end = Instant.parse(event.getEnd());

            assertFalse(start.isBefore(rangeStart), "Встреча раньше начала интервала: " + event.getStart());
            assertFalse(end.isAfter(rangeEnd), "Встреча позже конца интервала: " + event.getEnd());
            assertTrue(end.isAfter(start));

            if (start.isBefore(now)) {
                assertTrue(ALLOWED_PAST_STATUSES.contains(event.getStatus()),
                        "Недопустимый статус прошедшей встречи: " + event.getStatus());
            } else {
                assertTrue(ALLOWED_FUTURE_STATUSES.contains(event.getStatus()),
                        "У будущей встречи недопустимый статус: " + event.getStatus());
            }
        }
    }

    /**
     * У прошедших встреч SUCCESS должен встречаться чаще любого другого статуса. Данные набираются
     * по нескольким пользователям, чтобы выборка не зависела от случайного разброса
     */
    @Test
    public void successIsTheMostFrequentStatusAmongPastEvents() {
        List<CreateEventDto> allCreatedEvents = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            User user = createUser("statusUser" + i);
            createTestData(user.getUsername());
            allCreatedEvents.addAll(captureCreatedEvents(user.getMainCalendar()));
        }

        Instant now = Instant.now();

        Map<EventStatus, Integer> pastStatusCounts = new EnumMap<>(EventStatus.class);

        for (CreateEventDto event : allCreatedEvents) {
            if (Instant.parse(event.getStart()).isBefore(now)) {
                pastStatusCounts.merge(event.getStatus(), 1, Integer::sum);
            }
        }

        int successCount = pastStatusCounts.getOrDefault(EventStatus.SUCCESS, 0);
        assertTrue(successCount > 0, "Среди прошедших встреч нет ни одной со статусом SUCCESS");

        pastStatusCounts.forEach((status, count) -> {
            if (status != EventStatus.SUCCESS) {
                assertTrue(successCount > count,
                        "Статус " + status + " встречается не реже SUCCESS: " + count + " против " + successCount);
            }
        });
    }

    @Test
    public void deleteTestDataRemovesOnlyTestClientsAndTheirEvents() {
        User user = createUser("deleteTestUser");

        createTestData(user.getUsername());

        List<String> expectedEventIds = new ArrayList<>();

        for (Client client : clientRepository.getClientsByUserId(user.getId())) {
            expectedEventIds.addAll(clientEventRepository.getEventIdsByClientId(client.getId()));
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

    /**
     * События удаляются раньше клиентов: если календарь ответил ошибкой, база остается нетронутой
     * и повторный вызов сможет доудалить данные
     */
    @Test
    public void deleteTestDataKeepsDataInDatabaseWhenCalendarFails() {
        User user = createUser("failedDeleteUser");

        createTestData(user.getUsername());

        List<Client> createdClients = clientRepository.getClientsByUserId(user.getId());
        assertEquals(EXPECTED_CLIENTS_COUNT, createdClients.size());

        doThrow(new CalendarIntegrationException("Не удалось удалить событий: 1"))
                .when(googleCalendarClient).deleteEvents(anyString(), anyList());

        assertThrows(TestDataDeletionException.class, () -> testDataService.deleteTestData(user.getUsername()));

        List<Client> remainingClients = clientRepository.getClientsByUserId(user.getId());
        assertEquals(EXPECTED_CLIENTS_COUNT, remainingClients.size());

        for (Client client : remainingClients) {
            assertFalse(clientEventRepository.getEventIdsByClientId(client.getId()).isEmpty());
        }
    }

    /**
     * Если создание событий упало, уже созданные клиенты должны быть удалены из базы
     */
    @Test
    public void createTestDataRollsBackClientsWhenEventCreationFails() {
        User user = createUser("failedCreateUser");

        doThrow(new CalendarIntegrationException("Создание событий завершилось с ошибкой"))
                .when(googleCalendarClient).createEvents(anyString(), any());

        assertThrows(TestDataCreationException.class, () -> createTestData(user.getUsername()));

        assertTrue(clientRepository.getClientsByUserId(user.getId()).isEmpty(),
                "После неудачного создания в базе не должно остаться тестовых клиентов");
    }

    @SuppressWarnings("unchecked")
    private List<CreateEventDto> captureCreatedEvents(String calendarId) {
        ArgumentCaptor<List<CreateEventDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(googleCalendarClient).createEvents(eq(calendarId), captor.capture());
        return captor.getValue();
    }

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

    private TestDataCreationStatus createTestData(String username) {
        TestDataCreationStatus status = TestDataCreationStatus.LIMIT_NOT_REACHED;

        while (status == TestDataCreationStatus.LIMIT_NOT_REACHED) {
            status = testDataService.tryToCreateTestData(username);
        }

        return status;
    }
}
