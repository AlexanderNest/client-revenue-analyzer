package ru.nesterov.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventExtensionDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.google.EventStatusServiceImpl;
import ru.nesterov.calendar.integration.google.GoogleCalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.IncomeAnalysisResult;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.event.EventService;
import ru.nesterov.core.service.event.EventsAnalyzerProperties;
import ru.nesterov.core.service.event.EventsAnalyzerServiceImpl;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//TODO доработать тесты, проверить, что новые поля тоже подхватилисть. на новый фукнционал написать новые тесты
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
        client.setDescription("description");
        client.setStartDate(new Date(2025, Calendar.JUNE, 1));
        client.setPhone("phone");
        when(clientRepository.findClientByNameAndUserId("testName", 1)).thenReturn(client);

        User user = new User();
        user.setId(1);
        user.setUsername("testUsername");
        user.setCancelledCalendar("cancelledCalendar");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendarEnabled(true);
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

        EventExtensionDto eventExtensionDto1 = new EventExtensionDto();
        eventExtensionDto1.setIsPlanned(true);
        EventDto eventDto7 = EventDto.builder()
                .summary("testName")
                .start(start)
                .end(end)
                .eventExtensionDto(eventExtensionDto1)
                .status(EventStatus.CANCELLED)
                .build();

        EventExtensionDto eventExtensionDto2 = new EventExtensionDto();
        eventExtensionDto2.setIsPlanned(false);
        EventDto eventDto8 = EventDto.builder()
                .summary("testName")
                .start(start)
                .end(end)
                .eventExtensionDto(eventExtensionDto2)
                .status(EventStatus.CANCELLED)
                .build();


        EventDto holidayEvent = EventDto.builder()
                .summary("testName")
                .start(start)
                .end(end)
                .build();

        when(googleCalendarService.getHolidays(any(), any())).thenReturn(List.of(holidayEvent));
        when(googleCalendarService.getEventsBetweenDates(any())).thenReturn(List.of(eventDto1, eventDto2, eventDto3, eventDto4, eventDto5, eventDto6, eventDto7, eventDto8));
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
        assertEquals(3000, incomeAnalysisResult.getLostIncome());
        assertEquals(4500, incomeAnalysisResult.getActualIncome());
        assertEquals(9500, incomeAnalysisResult.getPotentialIncome());
        assertEquals(6500, incomeAnalysisResult.getExpectedIncome());
        assertEquals(3000, incomeAnalysisResult.getLostIncomeDueToHoliday());
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
        assertEquals(3, statuses.get(EventStatus.CANCELLED));
        assertEquals(1, statuses.get(EventStatus.PLANNED));
        assertEquals(1, statuses.get(EventStatus.REQUIRES_SHIFT));
    }

    @Test
    void getStatisticsByOneClientMeetings() {
        UserDto userDto = UserDto.builder()
                .username("testUsername")
                .id(1)
                .build();

        Date date = new Date(2025, Calendar.JUNE, 1);

        Map<String, ClientMeetingsStatistic> meetingsStatistics = eventsAnalyzerService.getStatisticsByOneClientMeetings(userDto, "testName");
        assertEquals(1, meetingsStatistics.get("testName").getId());
        assertEquals("description", meetingsStatistics.get("testName").getDescription());
        assertEquals(date, meetingsStatistics.get("testName").getStartDate());
        assertEquals("phone", meetingsStatistics.get("testName").getPhone());
        assertEquals(3, meetingsStatistics.get("testName").getSuccessfulMeetingsHours());
        assertEquals(3, meetingsStatistics.get("testName").getCancelledMeetingsHours());
        assertEquals(50, meetingsStatistics.get("testName").getSuccessfulMeetingsPercentage());
        assertEquals(3000, meetingsStatistics.get("testName").getLostIncome());
        assertEquals(3000, meetingsStatistics.get("testName").getActualIncome());
        assertEquals(1000, meetingsStatistics.get("testName").getIncomePerHour());
        assertEquals(3, meetingsStatistics.get("testName").getSuccessfulEventsCount());
        assertEquals(2, meetingsStatistics.get("testName").getPlannedCancelledEventsCount());
        assertEquals(1, meetingsStatistics.get("testName").getNotPlannedCancelledEventsCount());

    }

}