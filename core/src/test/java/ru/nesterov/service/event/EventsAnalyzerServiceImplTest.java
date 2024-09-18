package ru.nesterov.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventExtension;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.google.EventStatusServiceImpl;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.dto.IncomeAnalysisResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        EventsAnalyzerServiceImpl.class,
        EventStatusServiceImpl.class,
        EventsAnalyzerProperties.class,
        EventService.class
})
class EventsAnalyzerServiceImplTest {
    @Autowired
    private EventsAnalyzerServiceImpl eventsAnalyzerService;

    @MockBean
    private ClientRepository clientRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GoogleCalendarService googleCalendarService;

    @BeforeEach
    public void init() {
        Client client = new Client();
        client.setId(1);
        client.setName("testName");
        client.setPricePerHour(1000);
        when(clientRepository.findClientByNameAndUserId("testName", 1)).thenReturn(client);

        User user = new User();
        user.setId(1);
        user.setUsername("testUsername");
        user.setCancelledCalendar("cancelledCalendar");
        user.setMainCalendar("mainCalendar");
        when(userRepository.findByUsername("testUsername")).thenReturn(user);

        LocalDateTime start = LocalDateTime.of(2024, 8, 9, 22, 30);
        LocalDateTime end = LocalDateTime.of(2024, 8, 9, 23, 30);

        Event event1 = Event.builder()
                .summary("testName")
                .status(EventStatus.SUCCESS)
                .start(start)
                .end(end)
                .build();

        Event event2 = Event.builder()
                .summary("testName")
                .status(EventStatus.SUCCESS)
                .start(start)
                .end(end)
                .build();

        Event event3 = Event.builder()
                .summary("testName")
                .status(EventStatus.PLANNED)
                .start(start)
                .end(end)
                .build();

        Event event4 = Event.builder()
                .summary("testName")
                .status(EventStatus.REQUIRES_SHIFT)
                .start(start)
                .end(end)
                .build();

        Event event5 = Event.builder()
                .summary("testName")
                .status(EventStatus.CANCELLED)
                .start(start)
                .end(end)
                .build();

        EventExtension eventExtension = new EventExtension();
        eventExtension.setIncome(2500);
        Event event6 = Event.builder()
                .summary("testName")
                .start(start)
                .end(end)
                .eventExtension(eventExtension)
                .status(EventStatus.SUCCESS)
                .build();

        when(googleCalendarService.getEventsBetweenDates(any(), any(), any(), any())).thenReturn(List.of(event1, event2, event3, event4, event5, event6));
    }

    @Test
    void getStatisticsOfEachClientMeetings() {
    }

    @Test
    void getIncomeAnalysisByMonth() {
        IncomeAnalysisResult incomeAnalysisResult = eventsAnalyzerService.getIncomeAnalysisByMonth("testUsername", "august");
        assertEquals(1000, incomeAnalysisResult.getLostIncome());
        assertEquals(4500, incomeAnalysisResult.getActualIncome());
        assertEquals(7500, incomeAnalysisResult.getExpectedIncoming());
    }

    @Test
    void getEventStatusesByMonthName() {
        Map<EventStatus, Integer> statuses = eventsAnalyzerService.getEventStatusesByMonthName("testUsername", "august");
        assertEquals(4, statuses.size());
        assertEquals(3, statuses.get(EventStatus.SUCCESS));
        assertEquals(1, statuses.get(EventStatus.CANCELLED));
        assertEquals(1, statuses.get(EventStatus.PLANNED));
        assertEquals(1, statuses.get(EventStatus.REQUIRES_SHIFT));
    }
}