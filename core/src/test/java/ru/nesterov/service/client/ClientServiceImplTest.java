package ru.nesterov.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.Event;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.AppException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.CalendarService;
import ru.nesterov.service.monthHelper.MonthDatesPair;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = ClientServiceImpl.class)
class ClientServiceImplTest {
    @Autowired
    private ClientService clientService;
    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private CalendarService calendarService;

    @BeforeEach
    public void init() {
        Client client1 = new Client();
        client1.setId(1);
        client1.setName("testClient1");
        client1.setPricePerHour(1000);
        when(clientRepository.findClientByName("testClient1")).thenReturn(client1);

        Client client2 = new Client();
        client2.setId(1);
        client2.setName("testClient2");
        client2.setPricePerHour(1000);
        when(clientRepository.findClientByName("testClient2")).thenReturn(client2);

        Event event1 = Event.builder()
                .summary("testClient1")
                .colorId("1")
                .start(LocalDateTime.of(2024, 8, 9, 11, 30))
                .end(LocalDateTime.of(2024, 8, 9, 12, 30))
                .build();

        Event event2 = Event.builder()
                .summary("testClient1")
                .colorId("1")
                .start(LocalDateTime.of(2024, 8, 10, 11, 30))
                .end(LocalDateTime.of(2024, 8, 10, 12, 30))
                .build();

        Event event3 = Event.builder()
                .summary("testClient2")
                .colorId("4")
                .start(LocalDateTime.of(2024, 8, 11, 11, 30))
                .end(LocalDateTime.of(2024, 8, 11, 12, 30))
                .build();

        Event event4 = Event.builder()
                .summary("testClient2")
                .colorId(null)
                .start(LocalDateTime.of(2024, 8, 12, 11, 30))
                .end(LocalDateTime.of(2024, 8, 12, 12, 30))
                .build();

        Event event5 = Event.builder()
                .summary("testClient2")
                .colorId("6")
                .start(LocalDateTime.of(2024, 8, 13, 11, 30))
                .end(LocalDateTime.of(2024, 8, 13, 12, 30))
                .build();

        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);

        when(calendarService.getEventsBetweenDates(from, to)).thenReturn(List.of(event1, event2, event3, event4, event5));
    }

    @Test
    void shouldReturnScheduleForClient1() {
        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);
        List<MonthDatesPair> clientSchedule = clientService.getClientSchedule("testClient1", from, to);

        validateSchedule(
                2,
                clientSchedule,
                List.of(
                        LocalDateTime.of(2024, 8, 9, 11, 30),
                        LocalDateTime.of(2024, 8, 10, 11, 30)
                ),
                List.of(
                        LocalDateTime.of(2024, 8, 9, 12, 30),
                        LocalDateTime.of(2024, 8, 10, 12, 30)
                )
        );
    }

    @Test
    void shouldReturnScheduleForClient2() {
        LocalDateTime from = LocalDateTime.of(2024, 8, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 8, 13, 12, 30);
        List<MonthDatesPair> clientSchedule = clientService.getClientSchedule("testClient2", from, to);

        validateSchedule(3,
                clientSchedule,
                List.of(
                        LocalDateTime.of(2024, 8, 11, 11, 30),
                        LocalDateTime.of(2024, 8, 12, 11, 30),
                        LocalDateTime.of(2024, 8, 13, 11, 30)
                ),
                List.of(
                        LocalDateTime.of(2024, 8, 11, 12, 30),
                        LocalDateTime.of(2024, 8, 12, 12, 30),
                        LocalDateTime.of(2024, 8, 13, 12, 30)
                )
        );
    }

    @Test
    void shouldReturnEmptySchedule() {
        LocalDateTime from = LocalDateTime.of(2024, 11, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 11, 13, 12, 30);
        List<MonthDatesPair> clientSchedule = clientService.getClientSchedule("testClient1", from, to);
        assertTrue(clientSchedule.isEmpty());
    }

    @Test
    void shouldThrowAppException() {
        LocalDateTime from = LocalDateTime.of(2024, 11, 9, 11, 30);
        LocalDateTime to = LocalDateTime.of(2024, 11, 13, 12, 30);

        assertThrows(AppException.class, () -> {
            clientService.getClientSchedule("Client", from, to);
        });
    }

    private void validateSchedule(long listSize, List<MonthDatesPair> clientSchedule, List<LocalDateTime> firstDates,
                                  List<LocalDateTime> lastDates) {
        assertEquals(listSize, clientSchedule.size());
        List<LocalDateTime> retrievedFirstDates = clientSchedule.stream()
                .map(MonthDatesPair::getFirstDate)
                .toList();
        firstDates.forEach(date -> assertTrue(retrievedFirstDates.contains(date)));

        List<LocalDateTime> retrievedLastDates = clientSchedule.stream()
                .map(MonthDatesPair::getLastDate)
                .toList();
        lastDates.forEach(date -> assertTrue(retrievedLastDates.contains(date)));
    }
}