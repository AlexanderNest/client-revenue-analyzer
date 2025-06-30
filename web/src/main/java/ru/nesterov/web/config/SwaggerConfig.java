package ru.nesterov.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Client revenue analyzer",
                description = "Сервис для анализа мероприятий и клиентов", version = "1.0.0",
                contact = @Contact(
                        name = "Nesterov Aleksandr",
                        email = "nesterov1441@gmail.com"
                )
        )
)
public class SwaggerConfig {
}
