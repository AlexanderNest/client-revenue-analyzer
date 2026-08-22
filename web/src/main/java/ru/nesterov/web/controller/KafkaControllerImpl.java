package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.kafka.KafkaService;
import ru.nesterov.core.service.kafka.dto.EventUserActionDto;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class KafkaControllerImpl implements KafkaController {

    private KafkaService kafkaService;

    @Autowired
    public  KafkaControllerImpl(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Override
    public void prodice(@RequestHeader(name = "X-username", required = false) String username,
                        @RequestParam(name = "message") String message) {
        kafkaService.sendToQueue(new EventUserActionDto("1", message));
    }
}
