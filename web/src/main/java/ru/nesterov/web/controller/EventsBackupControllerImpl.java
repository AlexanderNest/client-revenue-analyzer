package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.dto.EventBackupDto;
import ru.nesterov.core.service.event.EventsBackupService;
import ru.nesterov.web.controller.response.EventBackupResponse;
import ru.nesterov.web.mapper.EventBackupMapper;

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

    public void deleteBackup(@RequestHeader(name = "X-username") String username) {
        eventsBackupService.deleteOldBackups();
    }
}
