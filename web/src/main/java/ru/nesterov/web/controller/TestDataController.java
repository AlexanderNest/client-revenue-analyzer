package ru.nesterov.web.controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.response.ResponseWithMessage;


@RequestMapping("/test")
public interface TestDataController {

    @PostMapping("/createTestData")
    ResponseWithMessage createTestData(@RequestHeader(name = "X-username") String username);

    @DeleteMapping("/deleteTestData")
    ResponseWithMessage deleteTestData(@RequestHeader(name = "X-username") String username);
}
