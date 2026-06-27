package com.sertifikasi.order.config;

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
    public OpenAPI orderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("REST API untuk manajemen order — buat order, bayar, dan batalkan. " +
                                "Berkomunikasi dengan Catalogue Service untuk validasi produk dan manajemen stok.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sertifikasi Team")
                                .email("team@sertifikasi.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local (direct)"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")
                ));
    }
}
