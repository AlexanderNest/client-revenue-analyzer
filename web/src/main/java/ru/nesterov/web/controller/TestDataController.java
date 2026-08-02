package ru.nesterov.web.controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.request.CreateTestDataRequest;
import ru.nesterov.web.controller.response.CreateTestDataResponse;


@RequestMapping("/test")
public interface TestDataController {

    @PostMapping("/createTestData")
    CreateTestDataResponse createTestData(CreateTestDataRequest createTestDataRequest);
}
