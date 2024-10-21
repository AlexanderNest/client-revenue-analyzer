package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.service.UserService;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.event.EventsBackupService;

@RestController
@RequiredArgsConstructor
public class EventsBackupControllerImpl implements EventsBackupController {
    private final UserService userService;
    private final EventsBackupService eventsBackupService;
    
    public int makeBackup(@RequestHeader(name = "X-username") String username) {
        UserDto userDto = userService.getUserByUsername(username);
        return eventsBackupService.manualBackup(userDto);
    }
}
