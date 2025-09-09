package ru.nesterov.web.controller.request;

import lombok.Data;
import ru.nesterov.core.entity.Role;

@Data
public class GetAllUsersByRoleAndSourceRequest {
    private String source;
    private Role role;
}
