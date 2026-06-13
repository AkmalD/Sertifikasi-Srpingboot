package com.sertifikasi.order.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class CatalogueClient {
    
    private final RestTemplate restTemplate;
    private static final String CATALOGUE_SERVICE_URL = "http://localhost:8080/api/products";  // ← UBAH KE 8080
    
    public CatalogueClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public ProductDTO getProductByCode(String code) {
        String url = CATALOGUE_SERVICE_URL + "/" + code;
        try {
            return restTemplate.getForObject(url, ProductDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Produk dengan code " + code + " tidak ditemukan");
        }
    }
    
    public void updateProductStock(String code, Integer quantity) {
        String url = CATALOGUE_SERVICE_URL + "/" + code;
        Map<String, Object> request = Map.of("stock", quantity);
        try {
            restTemplate.patchForObject(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Gagal mengupdate stok produk " + code);
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