package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.response.ResponseWithMessage;
import ru.nesterov.service.event.EventsBackupService;

@RestController
@RequiredArgsConstructor
public class EventsBackupControllerImpl implements EventsBackupController {
    private final EventsBackupService eventsBackupService;
    
    public ResponseEntity<ResponseWithMessage> makeBackup(@RequestHeader(name = "X-username") String username) {
        int savedEvents = eventsBackupService.backupCurrentUserEvents(username);
        
        ResponseWithMessage response = new ResponseWithMessage();
        response.setMessage("Встреч сохранено: " + savedEvents);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
