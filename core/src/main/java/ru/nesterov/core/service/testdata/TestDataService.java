package ru.nesterov.core.service.testdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.google.GoogleCalendarClient;
import ru.nesterov.calendar.integration.google.GoogleCalendarService;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.user.UserService;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ConditionalOnProperty(name = "test.data.enabled", havingValue = "true")
@Service
@Slf4j
@RequiredArgsConstructor
public class TestDataService {
    private final GoogleCalendarService googleCalendarService;
    private final ClientService clientService;
    private final UserService userService;
    private final byte START_COUNT = 0;
    private final byte MAX_COUNT = 2;

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

    public boolean tryToCreateTestData(String username) {
        AtomicBoolean success = new AtomicBoolean(false);

        requestCounterMap.merge(username, START_COUNT, (oldValue, newValue) -> {
            newValue = 1;
            byte result = (byte) (oldValue + newValue);

            if (result >= MAX_COUNT) {
                try { //TODO на подумать. надо защититься и всегда (даже если будет ошибка вернуть null
                    createTestData(username);//TODO execute
                    success.set(true);
                } catch (Exception e) {
                    success.set(false);
                } finally {
                    result = 0;
//                    return null;
                }

            }

            return result;
        });

        return success.get();
    }

    private void createTestData(String username) {
        ClientDto testClientVasiliy = ClientDto.builder()
                .name("Василий")
                .pricePerHour(1000)
                .description("Описание клиента Василий")
                .active(true)
                .startDate(new Date())
                .phone("89936062512")
                .build();

        ClientDto testClientAnton = ClientDto.builder()
                .name("Антон")
                .pricePerHour(3000)
                .description("Описание клиента Антон")
                .active(true)
                .startDate(new Date())
                .phone("89166044512")
                .build();

        ClientDto testClientPavel = ClientDto.builder()
                .name("Павел")
                .pricePerHour(2000)
                .description("Описание клиента Павел")
                .active(false)
                .startDate(new Date())
                .phone("89656061512")
                .build();

        UserDto userDto = userService.getUserByUsername(username);

        googleCalendarService.createEvent(testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-02T17:00:00.000Z", "2026-08-02T18:00:00.000Z");
        googleCalendarService.createEvent(testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-02T19:00:00.000Z", "2026-08-02T20:00:00.000Z");
        googleCalendarService.createEvent(testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-02T10:00:00.000Z", "2026-08-02T11:00:00.000Z");

        clientService.createClient(userDto, testClientVasiliy, false);
        clientService.createClient(userDto, testClientAnton, false);
        clientService.createClient(userDto, testClientPavel, false);
    }
}
