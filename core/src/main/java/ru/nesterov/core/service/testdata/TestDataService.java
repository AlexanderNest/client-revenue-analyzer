package ru.nesterov.core.service.testdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.CreateEventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.ResponseCreateEventDto;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.TestDataCreationStatus;
import ru.nesterov.core.service.ClientEventService;
import ru.nesterov.core.service.client.ClientBatchService;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.calendar.integration.dto.ClientEventDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.user.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@ConditionalOnProperty(name = "app.test.data.enabled", havingValue = "true")
@Service
@Slf4j
@RequiredArgsConstructor
public class TestDataService {
    private final CalendarService calendarService;
    private final ClientService clientService;
    private final UserService userService;
    private final ClientEventService clientEventService;
    private final static byte MAX_COUNT = 2;
    private final ClientBatchService clientBatchService;
    @Value("${app.test.data.client.limit}")
    private int clientLimit;

    private final Map<String, Byte> requestCounterMap = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 600000)
    public void cleanup() {
        boolean wasCleaned = requestCounterMap.entrySet().removeIf(entry -> {
            if (entry.getValue() >= MAX_COUNT) {
                log.trace("Удален {} из списка", entry);
                return true;
            }

            return false;
        });

        if (wasCleaned) {
            log.debug("Были найдены и удалены мусорные элементы");
        }
    }

    public TestDataCreationStatus tryToCreateTestData(String username) {
        byte START_COUNT = 0;
        int requestsCount = requestCounterMap.merge(username, START_COUNT, (oldValue, newValue) -> (byte) (oldValue + 1));

        TestDataCreationStatus status = TestDataCreationStatus.LIMIT_NOT_REACHED;

        if (requestsCount >= MAX_COUNT) {
            try {
                createTestData(username);
                status = TestDataCreationStatus.CREATED_NOW;
            } catch (Exception e) {
                status = TestDataCreationStatus.ERROR;
            }
        }

        return status;
    }

    private void createTestData(String username) {
        Random random = new Random();

        UserDto userDto = userService.getUserByUsername(username);

        List<ClientDto> clientDtoList = createRandomClient(userDto, random);

        Map<ClientDto, List<CreateEventDto>> clientEventsMap = createRandomEventForClients(random, userDto, clientDtoList);

        List<CreateEventDto> createEventDtoList = clientEventsMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        calendarService.batchCreateEvents(userDto.getMainCalendar(), createEventDtoList);

        List<ClientEventDto> clientEventDtoList = new ArrayList<>();

        clientEventService.batchCreateRelation(clientEventDtoList);
    }

    public void deleteTestData(String username) {
//        requestCounterMap.remove(username);

        UserDto userDto = userService.getUserByUsername(username);

        clientService.deleteClient(userDto, "Василий");
        clientService.deleteClient(userDto, "Антон");
        clientService.deleteClient(userDto, "Павел");
        clientService.deleteClient(userDto, "Артём");


    }

    private List<ClientDto> createRandomClient(UserDto userDto, Random random) {
        List<ClientDto> clientDtoList = new ArrayList<>();

        for (int i = 0; i < clientLimit; i++) {
            clientDtoList.add(ClientDto.builder()
                    .name("testName" + random.nextInt(1241241241))
                    .pricePerHour(random.nextInt(1000, 100000))
                    .description("testDescription" + random.nextInt(312312412))
                    .active(random.nextBoolean())
                    .startDate(getRandomDateForClient())
                    .phone(getRandomPhoneNumber(random))
                    .build());
        }

        return clientBatchService.createClient(userDto, clientDtoList);
    }

    private Map<ClientDto, List<CreateEventDto>> createRandomEventForClients(Random random, UserDto userDto, List<ClientDto> clientDtoList) {
        Map<ClientDto, List<CreateEventDto>> clientEventsMap = new HashMap<>();

        for (ClientDto client : clientDtoList) {
            List<CreateEventDto> eventsForClient = new ArrayList<>();

            for (int i = 0; i < random.nextInt(5, 10); i++) {
                String startDate = getDateForEvent(random).get("start");
                String endDate = getDateForEvent(random).get("end");

                CreateEventDto event = CreateEventDto.builder()
                        .mainCalendar(userDto.getMainCalendar())
                        .summary(client.getName())
                        .description(client.getDescription())
                        .start(startDate)
                        .end(endDate)
                        .status(EventStatus.values()[random.nextInt(EventStatus.values().length)])
                        .build();

                eventsForClient.add(event);
            }
            clientEventsMap.put(client, eventsForClient);
        }

        return clientEventsMap;
    }

    private String getRandomPhoneNumber(Random random) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }

    private Map<String, String> getDateForEvent(Random random) {
        long startSec = Instant.parse("2026-01-01T00:00:00.000Z").getEpochSecond();
        long endSec = Instant.parse("2026-12-31T23:00:00.000Z").getEpochSecond();

        long randomStart = startSec + random.nextLong(endSec - startSec + 1);
        Instant start = Instant.ofEpochSecond(randomStart);
        Instant end = start.plusSeconds(3600);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneOffset.UTC);

        Map<String, String> map = new HashMap<>();
        map.put("start", fmt.format(start));
        map.put("end", fmt.format(end));

        return map;
    }

    private Date getRandomDateForClient() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        int daysInYear = start.lengthOfYear();
        int randomOffset = ThreadLocalRandom.current().nextInt(daysInYear);
        LocalDate randomLocalDate = start.plusDays(randomOffset);
        return Date.from(randomLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
