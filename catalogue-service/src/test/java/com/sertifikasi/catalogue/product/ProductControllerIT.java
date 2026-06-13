package com.sertifikasi.catalogue.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sertifikasi.catalogue.product.dto.ProductRequest;
import com.sertifikasi.catalogue.product.dto.ReduceStockRequest;
import com.sertifikasi.catalogue.product.dto.UpdateStatusRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIT {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductRepository repository;
    
    private ProductRequest productRequest;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        productRequest = new ProductRequest();
        productRequest.setCode("TEST001");
        productRequest.setName("Test Product");
        productRequest.setPrice(50000.0);
        productRequest.setStock(100);
    }
    
    @Test
    void testCreateProductIntegration() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST001"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @Test
    void testGetProductByCodeIntegration() throws Exception {
        // Create product first
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)));
        
        // Get product
        mockMvc.perform(get("/api/products/TEST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }
    
    @Test
    void testGetProductNotFound() throws Exception {
        mockMvc.perform(get("/api/products/NOTFOUND"))
        .andExpect(status().isNotFound()) 
        .andExpect(jsonPath("$.error").value("Not Found")); 
}
    
    @Test
    void testUpdateProductStatusIntegration() throws Exception {
        // Create product
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)));
        
        // Update status
        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setStatus(ProductStatus.INACTIVE);
        
        mockMvc.perform(patch("/api/products/TEST001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
    
    @Test
    void testDeleteProductIntegration() throws Exception {
        // Create product
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)));
        
        // Delete
        mockMvc.perform(delete("/api/products/TEST001"))
                .andExpect(status().isOk());
        
        // Verify deleted

        mockMvc.perform(get("/api/products/TEST001"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testReduceStockIntegration() throws Exception {
        // Create product dengan stock 100
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)));
        
        // Reduce stock 10
        ReduceStockRequest reduceRequest = new ReduceStockRequest();
        reduceRequest.setQuantity(10);
        
        mockMvc.perform(patch("/api/products/TEST001/reduce-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reduceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(90));
    }
} 
