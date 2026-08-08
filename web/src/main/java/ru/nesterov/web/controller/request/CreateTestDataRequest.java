package ru.nesterov.web.controller.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.nesterov.web.controller.request.user.RequestWithUsername;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateTestDataRequest extends RequestWithUsername {

}
