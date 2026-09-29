package com.Rychlewski.DesafioItauFinancia.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Desafio Itau Financia",
        version = "1.0",
        description = "API REST para o desafio do Itau Financia"
    )
)
public class OpenApiConfig {

}
