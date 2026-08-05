package ru.nesterov.web.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteTestDataResponse {
    private String message;
}
