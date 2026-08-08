package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.testdata.TestDataService;
import ru.nesterov.core.entity.TestDataCreationStatus;
import ru.nesterov.web.controller.request.CreateTestDataRequest;
import ru.nesterov.web.controller.request.DeleteTestDataRequest;
import ru.nesterov.web.controller.response.ResponseWithMessage;

@ConditionalOnProperty(name = "app.test.data.enabled", havingValue = "true")
@RestController
@RequiredArgsConstructor
public class TestDataControllerImpl implements TestDataController {
    private final TestDataService testDataService;

    @Override
    public ResponseWithMessage createTestData(@RequestBody CreateTestDataRequest createTestDataRequest) {
        TestDataCreationStatus status = testDataService.tryToCreateTestData(createTestDataRequest.getUsername());

        String message = switch (status) {
            case ALREADY_CREATED -> "Тестовые данные уже были созданы ранее.";
            case CREATED_NOW -> "Тестовые данные были созданы";
            case LIMIT_NOT_REACHED -> "Вы точно хотите создать тестовые данные?";
            case ERROR -> "Ошибка при создании тестовых данных";
        };

        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(message);
        return responseWithMessage;
    }

    @Override
    public ResponseWithMessage deleteTestData(DeleteTestDataRequest deleteTestDataRequest) {
        testDataService.deleteTestData(deleteTestDataRequest.getUsername());

        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage("Тестовые данные удалены");
        return responseWithMessage;
    }

}
