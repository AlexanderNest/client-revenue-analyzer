package ru.nesterov.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.exception.ClientNotFoundException;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.client.ClientServiceImpl;
import ru.nesterov.core.service.dto.ClientScheduleDto;
import ru.nesterov.core.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = ClientServiceImpl.class)
public class ClientServiceImplTest {
    @Autowired
    private ClientService clientService;
    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private CalendarService calendarService;
    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    public void init() {
        User user = new User();
        user.setId(1);
        user.setCancelledCalendar("cancelledCalendar");
        user.setMainCalendar("mainCalendar");
        user.setUsername("testUser");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        Client client1 = new Client();
        client1.setId(1);
        client1.setName("testClient1");
        client1.setPricePerHour(1000);
        client1.setUser(user);
        when(clientRepository.findClientByNameAndUserId(client1.getName(), user.getId())).thenReturn(client1);

        Client client2 = new Client();
        client2.setId(1);
        client2.setName("testClient2");
        client2.setPricePerHour(1000);
        client2.setUser(user);
        when(clientRepository.findClientByNameAndUserId(client2.getName(), user.getId())).thenReturn(client2);

        EventDto eventDto1 = EventDto.builder()
                .summary("testClient1")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 9, 11, 30))
                .end(LocalDateTime.of(2024, 8, 9, 12, 30))
                .build();

        EventDto eventDto2 = EventDto.builder()
                .summary("testClient1")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 10, 11, 30))
                .end(LocalDateTime.of(2024, 8, 10, 12, 30))
                .build();

        EventDto eventDto3 = EventDto.builder()
                .summary("testClient2")
                .status(EventStatus.REQUIRES_SHIFT)
                .start(LocalDateTime.of(2024, 8, 11, 11, 30))
                .end(LocalDateTime.of(2024, 8, 11, 12, 30))
                .build();

        EventDto eventDto4 = EventDto.builder()
                .summary("testClient2")
                .status(EventStatus.PLANNED)
                .start(LocalDateTime.of(2024, 9, 12, 11, 30))
                .end(LocalDateTime.of(2024, 9, 12, 12, 30))
                .build();

        EventDto eventDto5 = EventDto.builder()
                .summary("testClient2")
                .status(EventStatus.PLANNED_CANCELLED)
                .start(LocalDateTime.of(2025, 7, 13, 11, 30))
                .end(LocalDateTime.of(2025, 7, 13, 12, 30))
                .build();

        EventDto eventDto6 = EventDto.builder()
                .summary("testClient2")
                .status(EventStatus.UNPLANNED_CANCELLED)
                .start(LocalDateTime.of(2025, 6, 13, 11, 30))
                .end(LocalDateTime.of(2025, 6, 13, 12, 30))
                .build();


        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2025, 9, 13, 12, 30);

        EventsFilter eventsFilter = EventsFilter.builder()
                .mainCalendar(user.getMainCalendar())
                .cancelledCalendar(user.getCancelledCalendar())
                .leftDate(from)
                .rightDate(to)
                .isCancelledCalendarEnabled(user.isCancelledCalendarEnabled())
                .build();

        when(calendarService.getEventsBetweenDates(any(EventsFilter.class)))
                .thenReturn(List.of(eventDto1, eventDto2, eventDto3, eventDto4, eventDto5, eventDto6));

        // Универсальный мок для пустого результата конкретного диапазона (уникальные даты)
        when(calendarService.getEventsBetweenDates(argThat(filter -> filter != null &&
                filter.getLeftDate().equals(LocalDateTime.of(2024, 12, 3, 0, 0)) &&
                filter.getRightDate().equals(LocalDateTime.of(2024, 12, 5, 23, 59))
        ))).thenReturn(List.of());
    }

    @Test
    public void shouldReturnScheduleForClient1() {
        UserDto userDto = createUserDto();

        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);

        List<ClientScheduleDto> actual = clientService.getClientSchedule(userDto, "testClient1", from, to);

        List<ClientScheduleDto> expected = List.of(
                ClientScheduleDto.builder()
                        .eventStart(LocalDateTime.of(2024, 8, 9, 11, 30))
                        .eventEnd(LocalDateTime.of(2024, 8, 9, 12, 30))
                        .requiresShift(false)
                        .build(),
                ClientScheduleDto.builder()
                        .eventStart(LocalDateTime.of(2024, 8, 10, 11, 30))
                        .eventEnd(LocalDateTime.of(2024, 8, 10, 12, 30))
                        .requiresShift(false)
                        .build()
        );

        validateSchedule(actual, expected);
    }

    @Test
    public void shouldReturnScheduleForClient2() {
        UserDto userDto = createUserDto();

        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);

        EventDto successEvent = EventDto.builder()
                .summary("testClient2")
                .status(EventStatus.SUCCESS)
                .start(LocalDateTime.of(2024, 8, 11, 11, 30))
                .end(LocalDateTime.of(2024, 8, 11, 12, 30))
                .build();

        when(calendarService.getEventsBetweenDates(any(EventsFilter.class)))
                .thenReturn(List.of(successEvent));

        List<ClientScheduleDto> actual = clientService.getClientSchedule(userDto, "testClient2", from, to);

        List<ClientScheduleDto> expected = List.of(
                ClientScheduleDto.builder()
                        .eventStart(LocalDateTime.of(2024, 8, 11, 11, 30))
                        .eventEnd(LocalDateTime.of(2024, 8, 11, 12, 30))
                        .requiresShift(false)
                        .build()
        );

        validateSchedule(actual, expected);
    }

    @Test
    public void shouldReturnEmptySchedule() {
        UserDto userDto = createUserDto();

        LocalDateTime from = LocalDateTime.of(2024, 12, 3, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 5, 23, 59);

        List<ClientScheduleDto> clientSchedule = clientService.getClientSchedule(userDto,"testClient1", from, to);
        assertTrue(clientSchedule.isEmpty());
    }

    @Test
    public void getScheduleForNotCreatedClientShouldThrowAppException() {
        UserDto userDto = UserDto.builder()
                .username("testUser")
                .id(1)
                .build();

        LocalDateTime from = LocalDateTime.of(2024, 11, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 11, 13, 12, 30);

        assertThrows(ClientNotFoundException.class, () -> clientService.getClientSchedule(userDto,"Client", from, to));
    }

    @Test
    public void shouldReturnAllNonCancelledEvents() {
        UserDto userDto = createUserDto();

        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2025, 9, 13, 12, 30);

        List<ClientScheduleDto> actualClient2 =
                clientService.getClientSchedule(userDto, "testClient2", from, to);

        assertEquals(2, actualClient2.size());

        List<Integer> count = actualClient2.stream()
                .map(csd -> csd.getEventStart().getYear())
                .toList();

        assertTrue(count.contains(2024));
        assertFalse(count.contains(2025));

    }

    private void validateSchedule(List<ClientScheduleDto> actual, List<ClientScheduleDto> expected) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
    }

    private UserDto createUserDto() {
        return UserDto.builder()
                .username("testUser")
                .cancelledCalendar("cancelledCalendar")
                .mainCalendar("mainCalendar")
                .id(1)
                .build();
    }
}