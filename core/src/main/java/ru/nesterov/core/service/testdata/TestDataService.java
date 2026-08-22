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
import ru.nesterov.core.entity.TestDataCreationStatus;
import ru.nesterov.core.service.ClientEventService;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.user.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@ConditionalOnProperty(name = "app.test.data.enabled", havingValue = "true")
@Service
@Slf4j
@RequiredArgsConstructor
public class TestDataService {
    private final Map<String, Byte> requestCounterMap = new ConcurrentHashMap<>();

    @Value("${app.test.data.client.limit}")
    private int clientLimit;
    private final static byte MAX_COUNT = 2;

    private final CalendarService calendarService;
    private final UserService userService;
    private final ClientEventService clientEventService;
    private final ClientService clientService;

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

        if (requestsCount < MAX_COUNT) {
            return TestDataCreationStatus.LIMIT_NOT_REACHED;
        }

        try {
            createTestData(username);
            return TestDataCreationStatus.CREATED;
        } catch (Exception e) {
            return TestDataCreationStatus.ERROR;
        } finally {
            requestCounterMap.remove(username);
        }
    }

    private void createTestData(String username) {
        Random random = new Random();

        UserDto userDto = userService.getUserByUsername(username);

        List<ClientDto> clientDtoList = createRandomClients(userDto, random);

        List<ResponseCreateEventDto> responseCreateEventDtoList = createRandomEventForClients(random, userDto, clientDtoList);

        for (ClientDto client : clientDtoList) { //TODO здесь явно что-то не то. связь делается точно проще, чем тут и как будто сопоставление по имени тоже не должно быть
            for (ResponseCreateEventDto eventDto : responseCreateEventDtoList) {
                if (Objects.equals(client.getName(), eventDto.getSummary())) {
                    ClientEventDto clientEventDto = ClientEventDto.builder()
                            .clientId(client.getId())
                            .eventId(eventDto.getEventId())
                            .build();
                    clientEventService.createClientEventLink(clientEventDto);
                }
            }
        }
    }

    public void deleteTestData(String username) {
        UserDto userDto = userService.getUserByUsername(username);

        List<ClientDto> clientDtoList = clientService.getClientByUserId(userDto);

        List<String> eventIdList = new ArrayList<>();

        for (ClientDto client : clientDtoList) {
            List<String> clientEventIds = clientEventService.getEventIdsByClientId(client.getId());

            if (clientEventIds.isEmpty()) {
                continue;
            }

            eventIdList.addAll(clientEventIds);
            clientService.deleteClient(userDto, client.getName());
        }

        calendarService.deleteEvents(userDto.getMainCalendar(), eventIdList);
    }

    private List<ClientDto> createRandomClients(UserDto userDto, Random random) {
        List<ClientDto> clientList = new ArrayList<>();

        for (int i = 0; i < clientLimit; i++) {
            ClientDto clientDto = ClientDto.builder()
                    .name("testName" + random.nextInt(1241241241))
                    .pricePerHour(random.nextInt(1000, 100000))
                    .description("testDescription" + random.nextInt(312312412))
                    .active(random.nextBoolean())
                    .startDate(getRandomDateForClient())
                    .phone(getRandomPhoneNumber(random))
                    .build();

            clientList.add(clientService.createClient(userDto, clientDto, true));
        }

        return clientList;
    }

    private List<ResponseCreateEventDto> createRandomEventForClients(Random random, UserDto userDto, List<ClientDto> clientDtoList) {
        List<CreateEventDto> eventsForClient = new ArrayList<>();

        for (ClientDto client : clientDtoList) {
            for (int i = 0; i < random.nextInt(5, 10); i++) {
                DatesPair datesPairForEvent = getDateForEvent(random);
                String startDate = datesPairForEvent.getStartDate();
                String endDate = datesPairForEvent.getEndDate();

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
        }

        List<ResponseCreateEventDto> createdEvents = calendarService.createEvents(userDto.getMainCalendar(), eventsForClient);
        return createdEvents;
    }

    private String getRandomPhoneNumber(Random random) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(8);

        for (int i = 0; i < 10; i++) {
            stringBuilder.append(random.nextInt(9));
        }
        return stringBuilder.toString();
    }

    private DatesPair getDateForEvent(Random random) {
        int currentYear = LocalDate.now().getYear();

        long startSec = LocalDate.of(currentYear, 1, 1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant().getEpochSecond();

        long endSec = LocalDate.of(currentYear, 12, 31)
                .atTime(23, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toInstant().getEpochSecond();

        long randomStart = startSec + random.nextLong(endSec - startSec + 1);
        Instant start = Instant.ofEpochSecond(randomStart);
        Instant end = start.plusSeconds(3600);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneOffset.UTC);

        return DatesPair.builder()
                .startDate(fmt.format(start))
                .endDate(fmt.format(end))
                .build();
    }

    private Date getRandomDateForClient() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        int daysInYear = start.lengthOfYear();
        int randomOffset = ThreadLocalRandom.current().nextInt(daysInYear);
        LocalDate randomLocalDate = start.plusDays(randomOffset);
        return Date.from(randomLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
