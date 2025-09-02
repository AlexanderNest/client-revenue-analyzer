package ru.nesterov.web.controller.response;

import lombok.Data;

import java.util.List;

@Data
public class GetUserIdsResponse {
    private List<String> userIds;
}
