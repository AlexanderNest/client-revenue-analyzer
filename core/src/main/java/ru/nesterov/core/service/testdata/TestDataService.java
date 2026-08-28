package ru.nesterov.core.service.testdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.ClientEventDto;
import ru.nesterov.calendar.integration.dto.CreateEventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.ResponseCreateEventDto;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.exception.TestDataCreationException;
import ru.nesterov.core.exception.TestDataDeletionException;
import ru.nesterov.core.service.ClientEventService;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.user.UserService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "app.test.data.enabled", havingValue = "true")
@Service
@Slf4j
@RequiredArgsConstructor
public class TestDataService {
    /**
     * Реальное создание данных происходит только на третий подряд идущий запрос от одного пользователя
     */
    private static final int REQUIRED_ATTEMPTS_COUNT = 3;
    /**
     * Незавершенная серия запросов протухает, если пользователь не повторил запрос в течение этого времени
     */
    private static final Duration ATTEMPTS_TTL = Duration.ofMinutes(10);
    private static final long CLEANUP_INTERVAL_MS = 600_000;

    private static final int EVENT_DATE_RANGE_MONTHS = 3;
    private static final int EVENT_DURATION_SECONDS = 3600;
    private static final int MIN_EVENTS_PER_CLIENT = 5;
    private static final int MAX_EVENTS_PER_CLIENT = 9;

    private static final DateTimeFormatter EVENT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    /**
     * У прошедших встреч статус SUCCESS должен встречаться заметно чаще остальных
     */
    private static final List<WeightedStatus> PAST_EVENT_STATUSES = List.of(
            new WeightedStatus(EventStatus.SUCCESS, 60),
            new WeightedStatus(EventStatus.PLANNED_CANCELLED, 15),
            new WeightedStatus(EventStatus.UNPLANNED_CANCELLED, 15),
            new WeightedStatus(EventStatus.PLANNED, 5),
            new WeightedStatus(EventStatus.REQUIRES_SHIFT, 5)
    );

    /**
     * Будущая встреча еще не могла состояться, поэтому SUCCESS для нее невозможен
     */
    private static final List<WeightedStatus> FUTURE_EVENT_STATUSES = List.of(
            new WeightedStatus(EventStatus.PLANNED, 80),
            new WeightedStatus(EventStatus.PLANNED_CANCELLED, 20)
    );

    private final Map<String, CreationAttempt> creationAttempts = new ConcurrentHashMap<>();

    @Value("${app.test.data.client.limit}")
    private int clientLimit;

    private final CalendarService calendarService;
    private final UserService userService;
    private final ClientEventService clientEventService;
    private final ClientService clientService;

    /**
     * Убирает незавершенные серии запросов, по которым пользователь так и не дошел до создания данных.
     * Завершенные серии удаляются сразу в {@link #tryToCreateTestData(String)}
     */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MS)
    public void cleanup() {
        Instant now = Instant.now();

        boolean wasCleaned = creationAttempts.entrySet().removeIf(entry -> {
            if (isExpired(entry.getValue(), now)) {
                log.trace("Удалена протухшая серия запросов пользователя [{}]", entry.getKey());
                return true;
            }

            return false;
        });

        if (wasCleaned) {
            log.debug("Были найдены и удалены протухшие серии запросов на создание тестовых данных");
        }
    }

    public TestDataCreationStatus tryToCreateTestData(String username) {
        Instant now = Instant.now();

        CreationAttempt attempt = creationAttempts.compute(username, (key, currentAttempt) -> {
            if (currentAttempt == null || isExpired(currentAttempt, now)) {
                return new CreationAttempt(1, now);
            }

            return new CreationAttempt(currentAttempt.count() + 1, now);
        });

        if (attempt.count() < REQUIRED_ATTEMPTS_COUNT) {
            log.debug("Запрос [{}] из [{}] на создание тестовых данных для пользователя [{}]",
                    attempt.count(), REQUIRED_ATTEMPTS_COUNT, username);
            return TestDataCreationStatus.LIMIT_NOT_REACHED;
        }

        try {
            createTestData(username);
            return TestDataCreationStatus.CREATED;
        } catch (Exception e) {
            throw new TestDataCreationException(username, e);
        } finally {
            creationAttempts.remove(username);
        }
    }

    /**
     * Удаляет тестовые данные пользователя. Клиент считается тестовым, если на него есть ссылки
     * в таблице связей с событиями.
     * <p>
     * Сначала удаляются события в календаре и только после этого клиенты из базы: если календарь ответил
     * ошибкой, база остается нетронутой и повторный вызов доудалит остатки. Повторное удаление уже
     * удаленного события в Google идемпотентно
     */
    public void deleteTestData(String username) {
        UserDto userDto = userService.getUserByUsername(username);

        List<ClientDto> testClients = new ArrayList<>();
        List<String> eventIds = new ArrayList<>();

        for (ClientDto client : clientService.getClientByUserId(userDto)) {
            List<String> clientEventIds = clientEventService.getEventIdsByClientId(client.getId());

            if (clientEventIds.isEmpty()) {
                continue;
            }

            testClients.add(client);
            eventIds.addAll(clientEventIds);
        }

        if (testClients.isEmpty()) {
            log.debug("Тестовые данные для пользователя [{}] не найдены", username);
            return;
        }

        try {
            calendarService.deleteEvents(userDto.getMainCalendar(), eventIds);
        } catch (Exception e) {
            throw new TestDataDeletionException(username, e);
        }

        for (ClientDto testClient : testClients) {
            clientService.deleteClient(userDto, testClient.getName());
        }

        log.info("Для пользователя [{}] удалено тестовых клиентов: [{}], событий: [{}]",
                username, testClients.size(), eventIds.size());
    }

    /**
     * Если создание упало на любом шаге, все уже созданное откатывается: сначала события в календаре,
     * затем клиенты в базе
     */
    private void createTestData(String username) {
        Random random = new Random();

        UserDto userDto = userService.getUserByUsername(username);

        List<ClientDto> createdClients = new ArrayList<>();
        List<ResponseCreateEventDto> createdEvents = new ArrayList<>();

        try {
            createdClients.addAll(createRandomClients(userDto, random));
            createdEvents.addAll(createRandomEventsForClients(random, userDto, createdClients));

            List<ClientEventDto> clientEventLinks = createdEvents.stream()
                    .map(event -> ClientEventDto.builder()
                            .clientId(event.getClientId())
                            .eventId(event.getEventId())
                            .build())
                    .toList();

            clientEventService.createClientEventLinks(clientEventLinks);

            log.info("Для пользователя [{}] создано клиентов: [{}], событий: [{}]",
                    username, createdClients.size(), createdEvents.size());
        } catch (Exception e) {
            rollbackCreatedTestData(userDto, createdClients, createdEvents, e);
            throw e;
        }
    }

    private void rollbackCreatedTestData(UserDto userDto, List<ClientDto> createdClients,
                                         List<ResponseCreateEventDto> createdEvents, Exception cause) {
        log.error("Создание тестовых данных для пользователя [{}] упало, откатываем уже созданное",
                userDto.getUsername(), cause);

        List<String> eventIds = createdEvents.stream()
                .map(ResponseCreateEventDto::getEventId)
                .toList();

        try {
            calendarService.deleteEvents(userDto.getMainCalendar(), eventIds);
        } catch (Exception e) {
            log.error("При откате не удалось удалить события из календаря [{}]. Их придется удалить вручную: {}",
                    userDto.getMainCalendar(), eventIds, e);
        }

        for (ClientDto createdClient : createdClients) {
            try {
                clientService.deleteClient(userDto, createdClient.getName());
            } catch (Exception e) {
                log.error("При откате не удалось удалить клиента [{}]", createdClient.getName(), e);
            }
        }
    }

    private List<ClientDto> createRandomClients(UserDto userDto, Random random) {
        List<ClientDto> clientList = new ArrayList<>();

        for (int i = 0; i < clientLimit; i++) {
            ClientDto clientDto = ClientDto.builder()
                    .name("testName" + random.nextInt(1241241241))
                    .pricePerHour(random.nextInt(1000, 100000))
                    .description("testDescription" + random.nextInt(312312412))
                    .active(random.nextBoolean())
                    .phone(getRandomPhoneNumber(random))
                    .build();

            clientList.add(clientService.createClient(userDto, clientDto, true));
        }

        return clientList;
    }

    private List<ResponseCreateEventDto> createRandomEventsForClients(Random random, UserDto userDto,
                                                                      List<ClientDto> clientDtoList) {
        List<CreateEventDto> eventsForClients = new ArrayList<>();

        for (ClientDto client : clientDtoList) {
            int eventsCount = random.nextInt(MIN_EVENTS_PER_CLIENT, MAX_EVENTS_PER_CLIENT + 1);

            for (int i = 0; i < eventsCount; i++) {
                eventsForClients.add(createRandomEvent(random, userDto, client));
            }
        }

        return calendarService.createEvents(userDto.getMainCalendar(), eventsForClients);
    }

    private CreateEventDto createRandomEvent(Random random, UserDto userDto, ClientDto client) {
        Instant start = getRandomEventStart(random);

        return CreateEventDto.builder()
                .mainCalendar(userDto.getMainCalendar())
                .clientId(client.getId())
                .summary(client.getName())
                .description(client.getDescription())
                .start(EVENT_DATE_FORMATTER.format(start))
                .end(EVENT_DATE_FORMATTER.format(start.plusSeconds(EVENT_DURATION_SECONDS)))
                .status(getRandomEventStatus(random, start))
                .build();
    }

    /**
     * Дата встречи выбирается случайно в интервале +-3 месяца от текущей даты
     */
    private Instant getRandomEventStart(Random random) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        long startSec = now.minusMonths(EVENT_DATE_RANGE_MONTHS).toEpochSecond(ZoneOffset.UTC);
        long endSec = now.plusMonths(EVENT_DATE_RANGE_MONTHS).toEpochSecond(ZoneOffset.UTC) - EVENT_DURATION_SECONDS;

        return Instant.ofEpochSecond(random.nextLong(startSec, endSec + 1));
    }

    private EventStatus getRandomEventStatus(Random random, Instant eventStart) {
        List<WeightedStatus> statuses = eventStart.isBefore(Instant.now()) ? PAST_EVENT_STATUSES : FUTURE_EVENT_STATUSES;

        int totalWeight = statuses.stream()
                .mapToInt(WeightedStatus::weight)
                .sum();

        int roll = random.nextInt(totalWeight);

        for (WeightedStatus weightedStatus : statuses) {
            roll -= weightedStatus.weight();

            if (roll < 0) {
                return weightedStatus.status();
            }
        }

        return statuses.getLast().status();
    }

    private String getRandomPhoneNumber(Random random) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(8);

        for (int i = 0; i < 10; i++) {
            stringBuilder.append(random.nextInt(9));
        }

        return stringBuilder.toString();
    }

    private boolean isExpired(CreationAttempt attempt, Instant now) {
        return attempt.lastAttemptDate().plus(ATTEMPTS_TTL).isBefore(now);
    }

    private record CreationAttempt(int count, Instant lastAttemptDate) {
    }

    private record WeightedStatus(EventStatus status, int weight) {
    }
}
