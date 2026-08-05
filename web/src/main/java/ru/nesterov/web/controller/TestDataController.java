package ru.nesterov.web.controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.web.controller.request.CreateTestDataRequest;
import ru.nesterov.web.controller.request.DeleteTestDataRequest;
import ru.nesterov.web.controller.response.CreateTestDataResponse;
import ru.nesterov.web.controller.response.DeleteTestDataResponse;


@RequestMapping("/test")
public interface TestDataController {

    @PostMapping("/createTestData")
    CreateTestDataResponse createTestData(CreateTestDataRequest createTestDataRequest);

    @DeleteMapping("/deleteTestData")
    DeleteTestDataResponse deleteTestData(DeleteTestDataRequest deleteTestDataRequest);
}
