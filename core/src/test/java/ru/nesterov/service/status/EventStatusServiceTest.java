package ru.nesterov.service.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.EventStatus;
import ru.nesterov.google.EventStatusServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = EventStatusServiceImpl.class)
public class EventStatusServiceTest {
    @Autowired
    private EventStatusServiceImpl eventStatusService;

    @Test
    public void defaultColorAlreadyUsedTest() {
        assertThrows(IllegalArgumentException.class, () -> new EventStatusServiceImpl(new ArrayList<>(), new ArrayList<>(), List.of("1"), List.of("2")));
    }

    @Test
    public void getSuccessStatusTest() {
        EventStatus status2 = eventStatusService.getEventStatus("2");
        EventStatus status10 = eventStatusService.getEventStatus("10");

        assertEquals(EventStatus.SUCCESS, status2);
        assertEquals(EventStatus.SUCCESS, status10);
    }

    @Test
    public void getCancelledStatusTest() {
        EventStatus status11 = eventStatusService.getEventStatus("11");

        assertEquals(EventStatus.CANCELLED, status11);
    }

    @Test
    public void getRequiresShiftStatusTest() {
        EventStatus status5 = eventStatusService.getEventStatus("5");

        assertEquals(EventStatus.REQUIRES_SHIFT, status5);
    }

    @Test
    public void getPlannedStatusTest() {
        EventStatus statusNull = eventStatusService.getEventStatus(null);

        assertEquals(EventStatus.PLANNED, statusNull);
    }
}
