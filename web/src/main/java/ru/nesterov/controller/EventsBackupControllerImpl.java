package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.response.EventBackupResponse;
import ru.nesterov.mapper.EventBackupMapper;
import ru.nesterov.service.dto.EventBackupDto;
import ru.nesterov.service.event.EventsBackupService;

@RestController
@ConditionalOnProperty("app.calendar.events.backup.enabled")
@RequiredArgsConstructor
public class EventsBackupControllerImpl implements EventsBackupController {
    private final EventsBackupService eventsBackupService;
    
    public ResponseEntity<EventBackupResponse> makeBackup(@RequestHeader(name = "X-username") String username) {
        EventBackupDto result = eventsBackupService.backupCurrentUserEvents(username);
        EventBackupResponse response = EventBackupMapper.mapToEventBackupResponse(result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
