package ru.nesterov.service.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.PrimaryEventData;
import ru.nesterov.calendar.integration.google.EventStatusServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = EventStatusServiceImpl.class)
public class EventDtoStatusServiceTest {
    @Autowired
    private EventStatusServiceImpl eventStatusService;

    @Test
    public void defaultColorAlreadyUsedTest() {
        assertThrows(IllegalArgumentException.class, () -> new EventStatusServiceImpl(new ArrayList<>(), new ArrayList<>(), List.of("1"), List.of("2"), new ArrayList<>()));
        assertThrows(IllegalArgumentException.class, () -> new EventStatusServiceImpl(List.of("1"), new ArrayList<>(), new ArrayList<>(), List.of("2"), new ArrayList<>()));
        assertThrows(IllegalArgumentException.class, () -> new EventStatusServiceImpl(List.of("1"), List.of("2"), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    }

    @Test
    public void getSuccessStatusTest() {
        PrimaryEventData primaryEventData2 = PrimaryEventData.builder()
                .colorId("2")
                .build();
        EventStatus status2 = eventStatusService.getEventStatus(primaryEventData2);

        PrimaryEventData primaryEventData10 = PrimaryEventData.builder()
                .colorId("10")
                .build();

        EventStatus status10 = eventStatusService.getEventStatus(primaryEventData10);

        assertEquals(EventStatus.SUCCESS, status2);
        assertEquals(EventStatus.SUCCESS, status10);
    }

    @Test
    public void getCancelledStatusTest() {
        PrimaryEventData primaryEventData11 = PrimaryEventData.builder()
                .colorId("11")
                .build();

        EventStatus status11 = eventStatusService.getEventStatus(primaryEventData11);

        assertEquals(EventStatus.CANCELLED, status11);
    }

    @Test
    public void getRequiresShiftStatusTest() {
        PrimaryEventData primaryEventData5 = PrimaryEventData.builder()
                .colorId("5")
                .build();

        EventStatus status5 = eventStatusService.getEventStatus(primaryEventData5);

        assertEquals(EventStatus.REQUIRES_SHIFT, status5);
    }

    @Test
    public void getPlannedStatusTest() {
        PrimaryEventData primaryEventData = PrimaryEventData.builder().build();

        EventStatus statusNull = eventStatusService.getEventStatus(primaryEventData);

        assertEquals(EventStatus.PLANNED, statusNull);
    }
}
