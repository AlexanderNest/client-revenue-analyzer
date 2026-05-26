package ru.nesterov.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventExtensionDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.google.EventStatusServiceImpl;
import ru.nesterov.calendar.integration.google.GoogleCalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.PriceChangeHistory;
import ru.nesterov.core.entity.User;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.repository.PriceChangeHistoryRepository;
import ru.nesterov.core.repository.UserRepository;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.GetStatisticsByClientMeetingsDto;
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
import static org.mockito.ArgumentMatchers.eq;
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

    @MockitoBean
    private ClientRepository clientRepository;
    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private GoogleCalendarService googleCalendarService;
    @MockitoBean
    private PriceChangeHistoryRepository priceChangeHistoryRepository;

    @BeforeEach
    public void init() {
        Client client = new Client();
        client.setId(1);
        client.setName("testName");
        PriceChangeHistory pch = new PriceChangeHistory();
        pch.setPrice(1000);
        pch.setChangeDate(LocalDateTime.of(2024, 8, 9, 0, 0));
        client.setDescription("description");
        client.setStartDate(new Date(2025, Calendar.JUNE, 1));
        client.setPhone("phone");
        client.setPriceChangeHistory(List.of(pch));
        when(clientRepository.findClientByNameAndUserId("testName", 1)).thenReturn(client);
        when(clientService.getPricePerHourForDate(eq(client), any(LocalDateTime.class))).thenReturn(1000.0);

        User user = new User();
        user.setId(1);
        user.setUsername("testUsername");
        user.setCancelledCalendar("cancelledCalendar");
        user.setMainCalendar("mainCalendar");
        user.setCancelledCalendarEnabled(true);
        when(userRepository.findByUsername("testUsername")).thenReturn(user);

        LocalDateTime start = LocalDateTime.of(2024, 8, 9, 20, 30);
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
                .status(EventStatus.PLANNED_CANCELLED)
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
                .status(EventStatus.PLANNED_CANCELLED)
                .build();

        EventExtensionDto eventExtensionDto2 = new EventExtensionDto();
        eventExtensionDto2.setIsPlanned(false);
        EventDto eventDto8 = EventDto.builder()
                .summary("testName")
                .start(start)
                .end(end)
                .eventExtensionDto(eventExtensionDto2)
                .status(EventStatus.UNPLANNED_CANCELLED)
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
        assertEquals(9000, incomeAnalysisResult.getLostIncome());
        assertEquals(8500, incomeAnalysisResult.getActualIncome());
        assertEquals(23500, incomeAnalysisResult.getPotentialIncome());
        assertEquals(14500, incomeAnalysisResult.getExpectedIncome());
        assertEquals(9000, incomeAnalysisResult.getLostIncomeDueToHoliday());
    }

    @Test
    void getEventStatusesByMonthName() {
        UserDto userDto = UserDto.builder()
                .username("testUsername")
                .id(1)
                .build();

        Map<EventStatus, Integer> statuses = eventsAnalyzerService.getEventStatusesByMonthName(userDto, "august");
        assertEquals(5, statuses.size());
        assertEquals(3, statuses.get(EventStatus.SUCCESS));
        assertEquals(2, statuses.get(EventStatus.PLANNED_CANCELLED));
        assertEquals(1, statuses.get(EventStatus.UNPLANNED_CANCELLED));
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

        GetStatisticsByClientMeetingsDto dto = GetStatisticsByClientMeetingsDto.builder()
                .userDto(userDto)
                .clientName("testName")
                .leftDate(LocalDateTime.now().minusYears(2))
                .rightDate(LocalDateTime.now())
                .build();

        ClientMeetingsStatistic meetingsStatistics = eventsAnalyzerService.getStatisticsByClientMeetings(dto);
        assertEquals("testName", meetingsStatistics.getName());
        assertEquals(1, meetingsStatistics.getId());
        assertEquals("description", meetingsStatistics.getDescription());
        assertEquals(date, meetingsStatistics.getStartDate());
        assertEquals("phone", meetingsStatistics.getPhone());
        assertEquals(9, meetingsStatistics.getSuccessfulMeetingsHours());
        assertEquals(9, meetingsStatistics.getCancelledMeetingsHours());
        assertEquals(50, meetingsStatistics.getSuccessfulMeetingsPercentage());
        assertEquals(9000, meetingsStatistics.getLostIncome());
        assertEquals(9000, meetingsStatistics.getActualIncome());
        assertEquals(1000, meetingsStatistics.getIncomePerHour());
        assertEquals(3, meetingsStatistics.getSuccessfulEventsCount());
        assertEquals(2, meetingsStatistics.getPlannedCancelledEventsCount());
        assertEquals(1, meetingsStatistics.getNotPlannedCancelledEventsCount());
    }
}