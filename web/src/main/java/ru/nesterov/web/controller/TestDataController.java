package ru.nesterov.web.controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.request.CreateTestDataRequest;
import ru.nesterov.web.controller.request.DeleteTestDataRequest;
import ru.nesterov.web.controller.response.ResponseWithMessage;


@RequestMapping("/test")
public interface TestDataController {

    @PostMapping("/createTestData")
    ResponseWithMessage createTestData(CreateTestDataRequest createTestDataRequest);

    @DeleteMapping("/deleteTestData")
    ResponseWithMessage deleteTestData(DeleteTestDataRequest deleteTestDataRequest);
}
