package ru.nesterov.gigachat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.gigachat.service.GigaChatApiService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class GigaChatIntegrationController {
    private final GigaChatApiService gigaChatApiService;

    @GetMapping("/generate")
    public List<String> generateText(@RequestHeader(name = "X-username") String username) {
        return gigaChatApiService.generateText().getChoices()
                .stream()
                .map(choice -> choice.getMessage().getContent())
                .toList();
    }
}
