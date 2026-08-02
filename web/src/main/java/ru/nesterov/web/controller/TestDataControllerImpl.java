package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.testdata.TestDataService;
import ru.nesterov.web.controller.request.CreateTestDataRequest;
import ru.nesterov.web.controller.response.CreateTestDataResponse;

@ConditionalOnProperty(name = "test.data.enabled", havingValue = "true")
@RestController
@RequiredArgsConstructor
public class TestDataControllerImpl implements TestDataController {
    private final TestDataService testDataService;

    @Override
    public CreateTestDataResponse createTestData(@RequestBody CreateTestDataRequest createTestDataRequest) {
        if (testDataService.tryToCreateTestData(createTestDataRequest.getUsername())) {
            return CreateTestDataResponse.builder()
                    .message("Тестовые клиенты были созданы").build();
        }
        return CreateTestDataResponse.builder()
                .message("Вы точно хотите создать тестовых клиентов ?").build();
    }
}
