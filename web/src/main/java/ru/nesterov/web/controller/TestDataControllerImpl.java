package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.testdata.TestDataService;
import ru.nesterov.web.controller.request.CreateTestDataRequest;
import ru.nesterov.web.controller.request.DeleteTestDataRequest;
import ru.nesterov.web.controller.response.CreateTestDataResponse;
import ru.nesterov.web.controller.response.DeleteTestDataResponse;

@ConditionalOnProperty(name = "app.test.data.enabled", havingValue = "true")
@RestController
@RequiredArgsConstructor
public class TestDataControllerImpl implements TestDataController {
    private final TestDataService testDataService;

    @Override
    public CreateTestDataResponse createTestData(@RequestBody CreateTestDataRequest createTestDataRequest) {
        String message = "Вы точно хотите создать тестовых клиентов?";

        if (testDataService.tryToCreateTestData(createTestDataRequest.getUsername())) {
            message = "Тестовые клиенты были созданы";
        }

        return CreateTestDataResponse.builder()
                .message(message).build();
    }

    @Override
    public DeleteTestDataResponse deleteTestData(DeleteTestDataRequest deleteTestDataRequest) {
        testDataService.deleteTestData(deleteTestDataRequest.getUsername());

        return DeleteTestDataResponse.builder()
                .message("Тестовые данные удалены").build();
    }

}
