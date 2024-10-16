package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.service.UserService;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.event.EventService;

@RestController
@RequiredArgsConstructor
public class EventsBackupControllerImpl implements EventsBackupController {
    private final UserService userService;
    private final EventService eventService;
    
    public void makeBackup(@RequestHeader(name = "X-username") String username) {
        UserDto userDto = userService.getUserByUsername(username);
        eventService.backup(userDto);
    }
}
