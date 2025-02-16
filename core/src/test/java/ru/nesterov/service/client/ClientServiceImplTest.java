package ru.nesterov.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.CalendarServiceDto;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.date.helper.MonthDatesPair;
import ru.nesterov.service.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
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
                .start(LocalDateTime.of(2024, 8, 12, 11, 30))
                .end(LocalDateTime.of(2024, 8, 12, 12, 30))
                .build();

        EventDto eventDto5 = EventDto.builder()
                .summary("testClient2")
                .status(EventStatus.CANCELLED)
                .start(LocalDateTime.of(2024, 8, 13, 11, 30))
                .end(LocalDateTime.of(2024, 8, 13, 12, 30))
                .build();

        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);

        CalendarServiceDto calendarServiceDto = CalendarServiceDto.builder()
                .mainCalendar(user.getMainCalendar())
                .cancelledCalendar(user.getCancelledCalendar())
                .leftDate(from)
                .rightDate(to)
                .build();

        when(calendarService.getEventsBetweenDates(calendarServiceDto))
                .thenReturn(List.of(eventDto1, eventDto2, eventDto3, eventDto4, eventDto5));
    }

    @Test
    public void shouldReturnScheduleForClient1() {
        UserDto userDto = createUserDto();

        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);

        List<MonthDatesPair> actual = clientService.getClientSchedule(userDto, "testClient1", from, to);

        List<MonthDatesPair> expected = List.of(
                new MonthDatesPair(
                        LocalDateTime.of(2024, 8, 9, 11, 30),
                        LocalDateTime.of(2024, 8, 9, 12, 30)
                ),
                new MonthDatesPair(
                        LocalDateTime.of(2024, 8, 10, 11, 30),
                        LocalDateTime.of(2024, 8, 10, 12, 30)
                )
        );

        validateSchedule(actual, expected);
    }

    @Test
    public void shouldReturnScheduleForClient2() {
        UserDto userDto = createUserDto();

        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);
        List<MonthDatesPair> actual = clientService.getClientSchedule(userDto,"testClient2", from, to);
        List<MonthDatesPair> expected = List.of(
                new MonthDatesPair(
                        LocalDateTime.of(2024, 8, 11, 11, 30),
                        LocalDateTime.of(2024, 8, 11, 12, 30)
                ),
                new MonthDatesPair(
                        LocalDateTime.of(2024, 8, 12, 11, 30),
                        LocalDateTime.of(2024, 8, 12, 12, 30)
                ),
                new MonthDatesPair(
                        LocalDateTime.of(2024, 8, 13, 11, 30),
                        LocalDateTime.of(2024, 8, 13, 12, 30)
                )
        );

        validateSchedule(actual, expected);
    }

    @Test
    public void shouldReturnEmptySchedule() {
        UserDto userDto = createUserDto();

        LocalDateTime from = LocalDateTime.of(2024, 11, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 11, 13, 12, 30);
        List<MonthDatesPair> clientSchedule = clientService.getClientSchedule(userDto,"testClient1", from, to);
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

        assertThrows(ClientNotFoundException.class, () -> {
            clientService.getClientSchedule(userDto,"Client", from, to);
        });
    }

    private void validateSchedule(List<MonthDatesPair> actual, List<MonthDatesPair> expected) {
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