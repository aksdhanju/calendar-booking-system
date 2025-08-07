package com.company.calendar.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Calendar Booking System API",
                version = "1.0",
                description = "APIs for managing appointments, users, and availability.",
                contact = @Contact(
                        name = "Akashdeep Singh",
                        email = "aksdhanju@gmail.com"
                )
        )
)
public class OpenApiConfig {
}