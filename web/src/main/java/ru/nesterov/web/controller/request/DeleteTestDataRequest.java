package ru.nesterov.web.controller.request;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class DeleteTestDataRequest extends CreateTestDataRequest {

}
