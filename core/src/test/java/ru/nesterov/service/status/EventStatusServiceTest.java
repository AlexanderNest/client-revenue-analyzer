package ru.nesterov.service.status;

import com.google.api.services.calendar.model.Event;
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
        Event event2 = new Event();
        event2.setColorId("2");
        EventStatus status2 = eventStatusService.getEventStatus(event2);

        Event event10 = new Event();
        event10.setColorId("10");

        EventStatus status10 = eventStatusService.getEventStatus(event10);

        assertEquals(EventStatus.SUCCESS, status2);
        assertEquals(EventStatus.SUCCESS, status10);
    }

    @Test
    public void getCancelledStatusTest() {
        Event event11 = new Event();
        event11.setColorId("11");

        EventStatus status11 = eventStatusService.getEventStatus(event11);

        assertEquals(EventStatus.CANCELLED, status11);
    }

    @Test
    public void getRequiresShiftStatusTest() {
        Event event5 = new Event();
        event5.setColorId("5");

        EventStatus status5 = eventStatusService.getEventStatus(event5);

        assertEquals(EventStatus.REQUIRES_SHIFT, status5);
    }

    @Test
    public void getPlannedStatusTest() {
        Event event = new Event();

        EventStatus statusNull = eventStatusService.getEventStatus(event);

        assertEquals(EventStatus.PLANNED, statusNull);
    }
}
