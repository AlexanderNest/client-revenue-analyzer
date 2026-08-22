package ru.nesterov.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Kakfa", description = "kafka")
@RequestMapping("/kafka")
public interface KafkaController {

    @GetMapping("/prodice")
    void prodice(@RequestHeader(name = "X-username", required = false) String username,
                 @RequestParam(name = "message") String message);
}
