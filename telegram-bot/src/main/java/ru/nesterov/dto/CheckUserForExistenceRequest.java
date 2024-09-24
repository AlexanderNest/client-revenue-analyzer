package ru.nesterov.dto;

import lombok.Data;

@Data
public class CheckUserForExistenceRequest {
    private String userIdentifier;
}
