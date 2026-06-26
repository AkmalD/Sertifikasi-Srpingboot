package com.sertifikasi.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class CatalogueClient {
    
    private final RestTemplate restTemplate;
    private final String catalogueServiceUrl;

    public CatalogueClient(
            RestTemplate restTemplate,
            @Value("${catalogue.service.url:http://localhost:8080/api/products}") String catalogueServiceUrl) {
        this.restTemplate = restTemplate;
        this.catalogueServiceUrl = catalogueServiceUrl;
    }
    
    public ProductDTO getProductByCode(String code) {
        String url = catalogueServiceUrl + "/" + code;
        try {
            return restTemplate.getForObject(url, ProductDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Produk dengan code " + code + " tidak ditemukan");
        }
    }
    
    public void reduceProductStock(String code, Integer quantity) {
        String url = catalogueServiceUrl + "/" + code + "/reduce-stock";
        Map<String, Object> request = Map.of("quantity", quantity);
        try {
            restTemplate.patchForObject(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Gagal mengurangi stok produk " + code);
        }
    }

    public void restoreProductStock(String code, Integer quantity) {
        String url = catalogueServiceUrl + "/" + code + "/restore-stock";
        Map<String, Object> request = Map.of("quantity", quantity);
        try {
            restTemplate.patchForObject(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Gagal mengembalikan stok produk " + code);
        }
    }
    
    public static class ProductDTO {
        public Long id;
        public String code;
        public String name;
        public Double price;
        public Integer stock;
        public String status;
        
        public Long getId() { return id; }
        public String getCode() { return code; }
        public String getName() { return name; }
        public Double getPrice() { return price; }
        public Integer getStock() { return stock; }
        public String getStatus() { return status; }
    }
}
