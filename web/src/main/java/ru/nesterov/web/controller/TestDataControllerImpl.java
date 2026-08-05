package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.testdata.TestDataService;
import ru.nesterov.core.entity.TestDataCreationStatus;
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
        TestDataCreationStatus status = testDataService.tryToCreateTestData(createTestDataRequest.getUsername());

        String message = switch (status) {
            case ALREADY_CREATED -> "Тестовые данные уже были созданы ранее.";
            case CREATED_NOW -> "Тестовые данные были созданы";
            case LIMIT_NOT_REACHED -> "Вы точно хотите создать тестовые данные?";
            case ERROR -> "Ошибка при создании тестовых данных";
        };

        return CreateTestDataResponse.builder()
                .message(message)
                .build();
    }

    @Override
    public DeleteTestDataResponse deleteTestData(DeleteTestDataRequest deleteTestDataRequest) {
        testDataService.deleteTestData(deleteTestDataRequest.getUsername());

        return DeleteTestDataResponse.builder()
                .message("Тестовые данные удалены").build();
    }

}
