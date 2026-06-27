package com.sertifikasi.catalogue.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogueOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Catalogue Service API")
                        .description("REST API untuk manajemen produk — CRUD, update stok, dan update status produk.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sertifikasi Team")
                                .email("team@sertifikasi.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local (direct)"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")
                ));
    }
}
