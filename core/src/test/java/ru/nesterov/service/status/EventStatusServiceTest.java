package ru.nesterov.service.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import ru.nesterov.service.dto.EventStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = EventStatusServiceImpl.class)
@TestPropertySource(properties = {
        "app.calendar.color.successful=1,2,3",
        "app.calendar.color.cancelled=4,5",
        "app.calendar.color.requires.shift=",
        "app.calendar.color.planned=6"
})
public class EventStatusServiceTest {
    @Autowired
    private EventStatusServiceImpl eventStatusService;

    @Test
    public void defaultColorAlreadyUsedTest() {
        assertThrows(IllegalArgumentException.class, () -> new EventStatusServiceImpl(new ArrayList<>(), new ArrayList<>(), List.of("1"), List.of("2")));
    }

    @Test
    public void getSuccessStatusTest() {
        EventStatus status1 = eventStatusService.getEventStatus("1");
        EventStatus status3 = eventStatusService.getEventStatus("3");

        assertEquals(EventStatus.SUCCESS, status1);
        assertEquals(EventStatus.SUCCESS, status3);
    }

    @Test
    public void getCancelledStatusTest() {
        EventStatus status4 = eventStatusService.getEventStatus("4");
        EventStatus status5 = eventStatusService.getEventStatus("5");

        assertEquals(EventStatus.CANCELLED, status4);
        assertEquals(EventStatus.CANCELLED, status5);
    }

    @Test
    public void getRequiresShiftStatusTest() {
        EventStatus statusNull = eventStatusService.getEventStatus(null);

        assertEquals(EventStatus.REQUIRES_SHIFT, statusNull);
    }

    @Test
    public void getPlannedStatusTest() {
        EventStatus status6 = eventStatusService.getEventStatus("6");

        assertEquals(EventStatus.PLANNED, status6);
    }
}
