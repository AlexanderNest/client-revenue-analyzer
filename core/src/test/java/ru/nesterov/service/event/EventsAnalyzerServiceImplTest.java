package ru.nesterov.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.entity.Client;
import ru.nesterov.entity.User;
import ru.nesterov.google.EventStatusServiceImpl;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.dto.IncomeAnalysisResult;
import ru.nesterov.service.dto.UserDto;

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

        EventDto eventDto1 = EventDto.builder()
                .summary("testName")
                .status(EventStatus.SUCCESS)
                .start(start)
                .end(end)
                .build();

        EventDto eventDto2 = EventDto.builder()
                .summary("testName")
                .status(EventStatus.SUCCESS)
                .start(start)
                .end(end)
                .build();

        EventDto eventDto3 = EventDto.builder()
                .summary("testName")
                .status(EventStatus.PLANNED)
                .start(start)
                .end(end)
                .build();

        EventDto eventDto4 = EventDto.builder()
                .summary("testName")
                .status(EventStatus.REQUIRES_SHIFT)
                .start(start)
                .end(end)
                .build();

        EventDto eventDto5 = EventDto.builder()
                .summary("testName")
                .status(EventStatus.CANCELLED)
                .start(start)
                .end(end)
                .build();

        EventExtensionDto eventExtensionDto = new EventExtensionDto();
        eventExtensionDto.setIncome(2500);
        EventDto eventDto6 = EventDto.builder()
                .summary("testName")
                .start(start)
                .end(end)
                .eventExtensionDto(eventExtensionDto)
                .status(EventStatus.SUCCESS)
                .build();

        when(googleCalendarService.getEventsBetweenDates(any())).thenReturn(List.of(eventDto1, eventDto2, eventDto3, eventDto4, eventDto5, eventDto6));
    }

    @Test
    void getStatisticsOfEachClientMeetings() {
    }

    @Test
    void getIncomeAnalysisByMonth() {
        UserDto userDto = UserDto.builder()
                .username("testUsername")
                .id(1)
                .build();

        IncomeAnalysisResult incomeAnalysisResult = eventsAnalyzerService.getIncomeAnalysisByMonth(userDto, "august");
        assertEquals(1000, incomeAnalysisResult.getLostIncome());
        assertEquals(4500, incomeAnalysisResult.getActualIncome());
        assertEquals(7500, incomeAnalysisResult.getPotentialIncome());
        assertEquals(6500, incomeAnalysisResult.getExpectedIncome());
    }

    @Test
    void getEventStatusesByMonthName() {
        UserDto userDto = UserDto.builder()
                .username("testUsername")
                .id(1)
                .build();

        Map<EventStatus, Integer> statuses = eventsAnalyzerService.getEventStatusesByMonthName(userDto, "august");
        assertEquals(4, statuses.size());
        assertEquals(3, statuses.get(EventStatus.SUCCESS));
        assertEquals(1, statuses.get(EventStatus.CANCELLED));
        assertEquals(1, statuses.get(EventStatus.PLANNED));
        assertEquals(1, statuses.get(EventStatus.REQUIRES_SHIFT));
    }
}