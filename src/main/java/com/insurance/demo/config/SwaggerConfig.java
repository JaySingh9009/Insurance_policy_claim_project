package com.insurance.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                        .title(
                                "Insurance Policy & Claim Management API")
                        .version("1.0")
                        .description(
                                "Insurance Management System APIs")
                        .contact(
                                new Contact()
                                .name("Sherlock Holmes")
                                .email("demo@gmail.com")));
    }
}