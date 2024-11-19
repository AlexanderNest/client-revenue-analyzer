package ru.nesterov.gigachat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.gigachat.service.GigaChatIntegrationService;
import ru.nesterov.gigachat.service.GigaChatTokenService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class GigaChatIntegrationController {
    private final GigaChatIntegrationService service;
    private final GigaChatTokenService tokenService;

    @GetMapping("/token")
    public String getAccessToken(@RequestHeader(name = "X-username") String username) {
        return tokenService.getTokenForUser();
    }
}
