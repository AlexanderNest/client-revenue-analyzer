package ru.nesterov.web.handler;

import ru.nesterov.web.controller.response.ResponseWithMessage;

public abstract class AbstractRestControllerAdvice {
    protected ResponseWithMessage buildResponseWithMessage(Exception exception) {
        ResponseWithMessage responseWithMessage = new ResponseWithMessage();
        responseWithMessage.setMessage(exception.getMessage());
        return responseWithMessage;
    }
}
