package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.kafka.MessageService;
import ru.nesterov.core.service.kafka.dto.KafkaMessage;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class KafkaControllerImpl implements KafkaController {

    private MessageService messageService;

    @Autowired
    public KafkaControllerImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void prodice(@RequestHeader(name = "X-username", required = false) String username,
                        @RequestParam(name = "message") String message) {
        messageService.send(new KafkaMessage("1", message));
    }
}
