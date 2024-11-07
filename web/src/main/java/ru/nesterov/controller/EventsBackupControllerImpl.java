package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.response.ResponseWithMessage;
import ru.nesterov.exception.EventBackupTimeoutException;
import ru.nesterov.service.event.EventsBackupService;

@RestController
@ConditionalOnProperty("app.calendar.events.backup.enabled")
@RequiredArgsConstructor
public class EventsBackupControllerImpl implements EventsBackupController {
    private final EventsBackupService eventsBackupService;
    
    public ResponseEntity<ResponseWithMessage> makeBackup(@RequestHeader(name = "X-username") String username) {
        ResponseWithMessage response = new ResponseWithMessage();
        int savedEvents;
        
        try {
            savedEvents = eventsBackupService.backupCurrentUserEvents(username);
        } catch (EventBackupTimeoutException e) {
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        
        response.setMessage("Встреч сохранено: " + savedEvents);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
