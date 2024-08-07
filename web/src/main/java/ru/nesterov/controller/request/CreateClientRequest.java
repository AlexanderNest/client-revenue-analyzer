package ru.nesterov.controller.request;

import lombok.Data;

@Data
public class CreateClientRequest {
    private String name;
    private boolean generatedIdentifierRequires;
}
