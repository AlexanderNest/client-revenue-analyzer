package ru.nesterov.core.service.testdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.google.GoogleCalendarService;
import ru.nesterov.core.entity.TestDataCreationStatus;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.user.UserService;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@ConditionalOnProperty(name = "app.test.data.enabled", havingValue = "true")
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

    public TestDataCreationStatus tryToCreateTestData(String username) {
        AtomicReference<TestDataCreationStatus> status = new AtomicReference<>(TestDataCreationStatus.LIMIT_NOT_REACHED);

        if(requestCounterMap.getOrDefault(username, (byte) 0) == 3) {
            status.set(TestDataCreationStatus.ALREADY_CREATED);
            return status.get();
        }

        requestCounterMap.merge(username, START_COUNT, (oldValue, newValue) -> {
            newValue = 1;
            byte result = (byte) (oldValue + newValue);

            if (result >= MAX_COUNT) {
                try { //TODO на подумать. надо защититься и всегда (даже если будет ошибка вернуть null
                    createTestData(username);
                    status.set(TestDataCreationStatus.CREATED_NOW);
                } catch (Exception e) {
                    status.set(TestDataCreationStatus.ERROR);
                }
            }

            return result;
        });

        return status.get();
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

        ClientDto testClientArtem = ClientDto.builder()
                .name("Артём")
                .pricePerHour(3000)
                .description("Описание клиента Артём")
                .active(false)
                .startDate(new Date())
                .phone("89936061512")
                .build();

        UserDto userDto = userService.getUserByUsername(username);

        clientService.createClient(userDto, testClientVasiliy, false);
        clientService.createClient(userDto, testClientAnton, false);
        clientService.createClient(userDto, testClientPavel, false);
        clientService.createClient(userDto, testClientArtem, false);

        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-03T09:00:00.000Z", "2026-08-03T10:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-04T09:00:00.000Z", "2026-08-04T10:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-05T09:00:00.000Z", "2026-08-05T10:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-06T09:00:00.000Z", "2026-08-06T10:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-07T09:00:00.000Z", "2026-08-07T10:00:00.000Z", EventStatus.PLANNED_CANCELLED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-08T09:00:00.000Z", "2026-08-08T10:00:00.000Z", EventStatus.UNPLANNED_CANCELLED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientAnton.getName(), testClientAnton.getDescription(), "2026-08-09T09:00:00.000Z", "2026-08-09T10:00:00.000Z", EventStatus.REQUIRES_SHIFT);

        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-03T10:00:00.000Z", "2026-08-03T11:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-04T10:00:00.000Z", "2026-08-04T11:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-05T10:00:00.000Z", "2026-08-05T11:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-06T10:00:00.000Z", "2026-08-06T11:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-07T10:00:00.000Z", "2026-08-07T11:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-08T10:00:00.000Z", "2026-08-08T11:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientVasiliy.getName(), testClientVasiliy.getDescription(), "2026-08-09T10:00:00.000Z", "2026-08-09T11:00:00.000Z", EventStatus.PLANNED);

        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientArtem.getName(), testClientArtem.getDescription(), "2026-08-03T11:00:00.000Z", "2026-08-03T12:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientArtem.getName(), testClientArtem.getDescription(), "2026-08-04T11:00:00.000Z", "2026-08-04T12:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientArtem.getName(), testClientArtem.getDescription(), "2026-08-05T11:00:00.000Z", "2026-08-05T12:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientArtem.getName(), testClientArtem.getDescription(), "2026-08-06T11:00:00.000Z", "2026-08-06T12:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientArtem.getName(), testClientArtem.getDescription(), "2026-08-07T11:00:00.000Z", "2026-08-07T12:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientArtem.getName(), testClientArtem.getDescription(), "2026-08-08T11:00:00.000Z", "2026-08-08T12:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientArtem.getName(), testClientArtem.getDescription(), "2026-08-09T11:00:00.000Z", "2026-08-09T12:00:00.000Z", EventStatus.SUCCESS);

        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-03T12:00:00.000Z", "2026-08-03T13:00:00.000Z", EventStatus.REQUIRES_SHIFT);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-04T12:00:00.000Z", "2026-08-04T13:00:00.000Z", EventStatus.REQUIRES_SHIFT);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-05T12:00:00.000Z", "2026-08-05T13:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-06T12:00:00.000Z", "2026-08-06T13:00:00.000Z", EventStatus.PLANNED_CANCELLED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-07T12:00:00.000Z", "2026-08-07T13:00:00.000Z", EventStatus.PLANNED);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-08T12:00:00.000Z", "2026-08-08T13:00:00.000Z", EventStatus.SUCCESS);
        googleCalendarService.createEvent(userDto.getMainCalendar(), testClientPavel.getName(), testClientPavel.getDescription(), "2026-08-09T12:00:00.000Z", "2026-08-09T13:00:00.000Z", EventStatus.REQUIRES_SHIFT);

    }

    public void deleteTestData(String username) {
//        requestCounterMap.remove(username);

        UserDto userDto = userService.getUserByUsername(username);

        clientService.deleteClient(userDto, "Василий");
        clientService.deleteClient(userDto, "Антон");
        clientService.deleteClient(userDto, "Павел");
        clientService.deleteClient(userDto, "Артём");

    }
}
