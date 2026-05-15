package br.edu.faculdade.sistemapastelaria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sistemaPastelariaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Sistema Pastelaria API")
                .description("Documentacao da API do Sistema Pastelaria")
                .version("v1"));
    }
}
