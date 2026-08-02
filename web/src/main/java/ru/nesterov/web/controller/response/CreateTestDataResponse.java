package ru.nesterov.web.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTestDataResponse {
    private String message;
}
