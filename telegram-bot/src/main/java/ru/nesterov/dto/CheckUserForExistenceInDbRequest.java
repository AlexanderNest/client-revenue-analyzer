package ru.nesterov.dto;

import lombok.Data;

@Data
public class CheckUserForExistenceInDbRequest {
    private String userIdentifier;
}
