package com.library.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud-Based Library Management System")
                        .version("1.0")
                        .description("Cloud-Based Library Management System featuring DevOps AWS Integration. " +
                                "Author: Abraham Grace F, Course: ISWE406L.")
                        .contact(new Contact()
                                .name("Abraham Grace F")
                                .email("abraham.grace2024@vitstudent.ac.in"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
