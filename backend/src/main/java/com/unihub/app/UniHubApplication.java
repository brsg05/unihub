package com.unihub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "UniHub API",
        version = "v1",
        description = "API para o Sistema de Avaliação de Professores UniHub.",
        license = @License(name = "Apache 2.0", url = "http://springdoc.org")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor de Desenvolvimento Local")
    }
)
public class UniHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniHubApplication.class, args);
    }

} 