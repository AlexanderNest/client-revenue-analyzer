package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.response.BackupResponse;
import ru.nesterov.service.event.EventsBackupService;

@RestController
@ConditionalOnProperty("app.calendar.events.backup.enable")
@RequiredArgsConstructor
public class EventsBackupControllerImpl implements EventsBackupController {
    private final EventsBackupService eventsBackupService;
    
    public ResponseEntity<BackupResponse> makeBackup(@RequestHeader(name = "X-username") String username) {
        int savedEvents = eventsBackupService.backupCurrentUserEvents(username);
        
        BackupResponse response = new BackupResponse();
        if (savedEvents > 0) {
            response.setBackupDone(true);
            response.setEventsSaved(savedEvents);
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
