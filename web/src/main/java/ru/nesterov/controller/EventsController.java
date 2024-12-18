package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.user.UserService;

@RestController
@RequiredArgsConstructor
public class EventsController {
    private final GoogleCalendarService googleCalendarService;
    private final UserService userService;

    @PostMapping("/transfer")
    public void transferEvents(@RequestHeader(name = "X-username") String username) {
        UserDto userDto = userService.getUserByUsername(username);
        googleCalendarService.transferCancelledEventsToCancelledCalendar(userDto.getMainCalendar(), userDto.getCancelledCalendar());
    }
}
