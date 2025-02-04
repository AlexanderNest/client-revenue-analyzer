package ru.nesterov.controller;

import org.springframework.web.bind.annotation.GetMapping;
import ru.nesterov.controller.request.GetHolidaysRequest;
import ru.nesterov.dto.EventDto;
import java.util.List;

public interface HolidayController {
    @GetMapping("/holidayDay")
    List<EventDto> getHolidays(GetHolidaysRequest getHolidaysRequest);
}
