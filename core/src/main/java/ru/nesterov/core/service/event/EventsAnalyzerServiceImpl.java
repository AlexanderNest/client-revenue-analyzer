package ru.nesterov.core.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.EventsFilter;
import ru.nesterov.calendar.integration.service.CalendarService;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.exception.ClientNotFoundException;
import ru.nesterov.core.exception.UnknownEventStatusException;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.service.date.helper.MonthDatesPair;
import ru.nesterov.core.service.date.helper.MonthHelper;
import ru.nesterov.core.service.date.helper.WeekHelper;
import ru.nesterov.core.service.dto.BusynessAnalysisResult;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.IncomeAnalysisResult;
import ru.nesterov.core.service.dto.UserDto;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventsAnalyzerServiceImpl implements EventsAnalyzerService {
    private final CalendarService calendarService;
    private final ClientRepository clientRepository;
    private final EventsAnalyzerProperties eventsAnalyzerProperties;
    private final EventService eventService;

    @Nullable
    public ClientMeetingsStatistic getStatisticsByClientMeetings(UserDto userDto, String clientName) {
        EventsFilter eventsFilter = EventsFilter.builder()
                .mainCalendar(userDto.getMainCalendar())
                .cancelledCalendar(userDto.getCancelledCalendar())
                .leftDate(LocalDateTime.now().minusYears(2))
                .rightDate(LocalDateTime.now())
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .clientName(clientName)
                .build();

        List<EventDto> eventDtos = calendarService.getEventsBetweenDates(eventsFilter);

        return getStatisticsOfClientMeetings(userDto, eventDtos).get(clientName);
    }

    public Map<String, ClientMeetingsStatistic> getStatisticsOfEachClientMeetingsForMonth(UserDto userDto, String monthName) {
        List<EventDto> eventDtos = getEventsByMonth(userDto, monthName);

        return getStatisticsOfClientMeetings(userDto, eventDtos);
    }

    private Map<String, ClientMeetingsStatistic> getStatisticsOfClientMeetings(UserDto userDto, List<EventDto> eventDtos) {
        Map<String, ClientMeetingsStatistic> meetingsStatistics = new HashMap<>();
        for (EventDto eventDto : eventDtos) {
            EventStatus eventStatus = eventDto.getStatus();
            Client client = clientRepository.findClientByNameAndUserId(eventDto.getSummary(), userDto.getId());
            ClientMeetingsStatistic clientMeetingsStatistic = meetingsStatistics.get(eventDto.getSummary());
            if (clientMeetingsStatistic == null) {
                if (client == null) {
                    throw new ClientNotFoundException(eventDto.getSummary());
                }

                clientMeetingsStatistic = new ClientMeetingsStatistic(client.getPricePerHour());
                clientMeetingsStatistic.setName(client.getName());
                clientMeetingsStatistic.setId(client.getId());
                clientMeetingsStatistic.setDescription(client.getDescription());
                clientMeetingsStatistic.setStartDate(client.getStartDate());
                clientMeetingsStatistic.setPhone(client.getPhone());
                meetingsStatistics.put(eventDto.getSummary(), clientMeetingsStatistic);
            }

            if (eventStatus == EventStatus.SUCCESS) {
                handleSuccessfulEvent(clientMeetingsStatistic, eventDto, client);
            } else if (eventStatus == EventStatus.PLANNED_CANCELLED || eventStatus == EventStatus.UNPLANNED_CANCELLED && EventExtensionService.isPlannedStatus(eventDto)) {
                handlePlannedCancelledEvent(clientMeetingsStatistic, eventDto);
            } else if (eventStatus == EventStatus.UNPLANNED_CANCELLED ) {
                handleUnplannedCancelledEvent(clientMeetingsStatistic, eventDto);
            }
        }

        return meetingsStatistics;
    }

    private void handleSuccessfulEvent(ClientMeetingsStatistic clientMeetingsStatistic, EventDto eventDto, Client client){
        double eventDuration = eventService.getEventDuration(eventDto);
        clientMeetingsStatistic.increaseSuccessfulHours(eventDuration);
        clientMeetingsStatistic.increaseSuccessfulEvents(1);
        clientMeetingsStatistic.increaseIncome(client.getPricePerHour() * eventDuration);
    }

    private void handlePlannedCancelledEvent(ClientMeetingsStatistic clientMeetingsStatistic, EventDto eventDto){
        double eventDuration = eventService.getEventDuration(eventDto);
        clientMeetingsStatistic.increaseCancelledHours(eventDuration);
        clientMeetingsStatistic.increasePlannedCancelledEvents(1);
    }

    private void handleUnplannedCancelledEvent(ClientMeetingsStatistic clientMeetingsStatistic, EventDto eventDto){
        double eventDuration = eventService.getEventDuration(eventDto);
        clientMeetingsStatistic.increaseCancelledHours(eventDuration);
        clientMeetingsStatistic.increaseNotPlannedCancelledEvents(1);
    }

    public IncomeAnalysisResult getIncomeAnalysisByMonth(UserDto userDto, String monthName) {
        List<EventDto> eventDtos = getEventsByMonth(userDto, monthName);

        double actualIncome = 0;
        double lostIncome = 0;
        double potentialIncome = 0;
        double expectedIncome = 0;
        double lostIncomeDueToHoliday = 0;

        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        List<EventDto> holidayDtos = calendarService.getHolidays(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());

        for (EventDto eventDto : eventDtos) {
            EventStatus eventStatus = eventDto.getStatus();

            Client client = clientRepository.findClientByNameAndUserId(eventDto.getSummary(), userDto.getId());
            if (client == null) {
                throw new ClientNotFoundException(eventDto.getSummary(), eventDto.getStart());
            }

            double eventPrice = eventService.getEventIncome(userDto, eventDto);
            potentialIncome += eventPrice;

            if (eventStatus == EventStatus.SUCCESS) {
                actualIncome += eventPrice;
                expectedIncome += eventPrice;
            } else if (eventStatus.isCancelledStatus()) {
                lostIncome += eventPrice;

                if(isHoliday(holidayDtos, eventDto)) {
                    lostIncomeDueToHoliday += eventPrice;
                }
            } else if (eventStatus == EventStatus.REQUIRES_SHIFT || eventStatus == EventStatus.PLANNED) {
                expectedIncome += eventPrice;
            } else {
                throw new UnknownEventStatusException(eventStatus);
            }
        }

        IncomeAnalysisResult incomeAnalysisResult = new IncomeAnalysisResult();
        incomeAnalysisResult.setLostIncome(lostIncome);
        incomeAnalysisResult.setPotentialIncome(potentialIncome);
        incomeAnalysisResult.setActualIncome(actualIncome);
        incomeAnalysisResult.setExpectedIncome(expectedIncome);
        incomeAnalysisResult.setLostIncomeDueToHoliday(lostIncomeDueToHoliday);

        return incomeAnalysisResult;
    }

    private boolean isHoliday(List<EventDto> holidayDtos, EventDto eventDto) {
        return holidayDtos.stream()
                .anyMatch(holidayDto -> eventDto.getStart().getDayOfMonth() == holidayDto.getStart().getDayOfMonth());
    }

    @Override
    public List<EventDto> getUnpaidEventsBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate) {
        EventsFilter eventsFilter = EventsFilter.builder()
                .mainCalendar(userDto.getMainCalendar())
                .cancelledCalendar(userDto.getCancelledCalendar())
                .rightDate(rightDate)
                .leftDate(leftDate)
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .build();

        return calendarService.getEventsBetweenDates(eventsFilter).stream()
                .filter(event -> {
                    EventStatus eventStatus = event.getStatus();
                    return eventStatus == EventStatus.PLANNED || eventStatus == EventStatus.REQUIRES_SHIFT;
                })
                .toList();
    }

    @Override
    public List<EventDto> getUnpaidEvents(UserDto userDto) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime requiredDateTime = currentDateTime.minusDays(eventsAnalyzerProperties.getUnpaidEventsRange());
        return getUnpaidEventsBetweenDates(userDto, requiredDateTime, LocalDateTime.now());
    }

    public Map<EventStatus, Integer> getEventStatusesByMonthName(UserDto userDto, String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        return getEventStatusesBetweenDates(userDto, monthDatesPair.getFirstDate(), monthDatesPair.getLastDate());
    }

    public Map<EventStatus, Integer> getEventStatusesBetweenDates(UserDto userDto, LocalDateTime leftDate, LocalDateTime rightDate) {
        EventsFilter eventsFilter = EventsFilter.builder()
                .mainCalendar(userDto.getMainCalendar())
                .cancelledCalendar(userDto.getCancelledCalendar())
                .rightDate(rightDate)
                .leftDate(leftDate)
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .build();

        List<EventDto> eventDtos = calendarService.getEventsBetweenDates(eventsFilter);

        Map<EventStatus, Integer> statuses = new HashMap<>();
        for (EventDto eventDto : eventDtos) {
            EventStatus eventStatus = eventDto.getStatus();

            statuses.put(eventStatus, statuses.getOrDefault(eventStatus, 0) + 1);
        }

        return statuses;
    }

    private List<EventDto> getEventsByMonth(UserDto userDto, String monthName) {
        MonthDatesPair monthDatesPair = MonthHelper.getFirstAndLastDayOfMonth(monthName);
        EventsFilter eventsFilter = EventsFilter.builder()
                .mainCalendar(userDto.getMainCalendar())
                .cancelledCalendar(userDto.getCancelledCalendar())
                .leftDate(monthDatesPair.getFirstDate())
                .rightDate(monthDatesPair.getLastDate())
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .build();
        return calendarService.getEventsBetweenDates(eventsFilter);
    }

    private List<EventDto> getEventsByYear(UserDto userDto, int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59);
        EventsFilter eventsFilter = EventsFilter.builder()
                .mainCalendar(userDto.getMainCalendar())
                .cancelledCalendar(userDto.getCancelledCalendar())
                .leftDate(startOfYear)
                .rightDate(endOfYear)
                .isCancelledCalendarEnabled(userDto.isCancelledCalendarEnabled())
                .build();
        return calendarService.getEventsBetweenDates(eventsFilter);
    }

    @Override
    public BusynessAnalysisResult getBusynessStatisticsByYear(UserDto userDto, int year) {
        List<EventDto> eventDtos = getEventsByYear(userDto, year);
        Map<String, Double> monthHours = new HashMap<>();
        Map<String, Double> weekHours = new HashMap<>();
        for (EventDto eventDto : eventDtos) {
            if (eventDto.getStatus() == EventStatus.SUCCESS) {
                double eventDuration = eventService.getEventDuration(eventDto);
                String monthName = MonthHelper.getMonthNameByNumber(eventDto.getStart().getMonthValue());
                monthHours.merge(monthName, eventDuration, Double::sum);
                String dayOfWeekName = WeekHelper.getWeekDayNameByNumber(eventDto.getStart().getDayOfWeek().getValue());
                weekHours.merge(dayOfWeekName, eventDuration, Double::sum);
            }
        }

        LinkedHashMap<String, Double> sortedMonthHours = new LinkedHashMap<>();
        monthHours.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> sortedMonthHours.put(entry.getKey(), entry.getValue()));

        LinkedHashMap<String, Double> sortedWeekDayHours = new LinkedHashMap<>();
        weekHours.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> sortedWeekDayHours.put(entry.getKey(), entry.getValue()));

        BusynessAnalysisResult result = new BusynessAnalysisResult();
        result.setMonths(sortedMonthHours);
        result.setDays(sortedWeekDayHours);

        return result;
    }
}
