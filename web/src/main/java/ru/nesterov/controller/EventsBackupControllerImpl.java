package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.response.EventBackupResponse;
import ru.nesterov.exception.EventBackupTimeoutException;
import ru.nesterov.service.event.EventsBackupService;

@RestController
@ConditionalOnProperty("app.calendar.events.backup.enabled")
@RequiredArgsConstructor
public class EventsBackupControllerImpl implements EventsBackupController {
    private final EventsBackupService eventsBackupService;
    
    public ResponseEntity<EventBackupResponse> makeBackup(@RequestHeader(name = "X-username") String username) {
        EventBackupResponse response = new EventBackupResponse();
        
        try {
            int savedEventsCount = eventsBackupService.backupCurrentUserEvents(username);
            response.setSavedEventsCount(savedEventsCount);
            response.setIsBackupMade(true);
        } catch (EventBackupTimeoutException e) {
            response.setIsBackupMade(false);
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
