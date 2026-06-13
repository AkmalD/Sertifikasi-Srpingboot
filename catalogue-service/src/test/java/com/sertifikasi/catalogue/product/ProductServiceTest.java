package com.sertifikasi.catalogue.product;

import com.sertifikasi.catalogue.exception.BadRequestException;
import com.sertifikasi.catalogue.product.dto.ProductRequest;
import com.sertifikasi.catalogue.product.dto.ProductResponse;
import com.sertifikasi.catalogue.product.dto.UpdateStatusRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    
    @Mock
    private ProductRepository repository;
    
    @InjectMocks
    private ProductService service;
    
    private Product product;
    private ProductRequest productRequest;
    
    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setCode("NSPD");
        product.setName("Nasi Padang");
        product.setPrice(25000.0);
        product.setStock(100);
        product.setStatus(ProductStatus.ACTIVE);
        
        productRequest = new ProductRequest();
        productRequest.setCode("NSPD");
        productRequest.setName("Nasi Padang");
        productRequest.setPrice(25000.0);
        productRequest.setStock(100);
    }
    
    @Test
    void testCreateProductSuccess() {
        when(repository.save(any(Product.class))).thenReturn(product);
        
        ProductResponse response = service.createProduct(productRequest);
        
        assertNotNull(response);
        assertEquals("NSPD", response.getCode());
        assertEquals("Nasi Padang", response.getName());
        assertEquals(25000.0, response.getPrice());
        verify(repository, times(1)).save(any(Product.class));
    }
    
    @Test
    void testCreateProductWithNullCodeFails() {
        productRequest.setCode(null);
        
        assertThrows(BadRequestException.class, () -> {
            service.createProduct(productRequest);
        });
    }
    
    @Test
    void testGetProductByCodeSuccess() {
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        
        ProductResponse response = service.getProductByCode("NSPD");
        
        assertNotNull(response);
        assertEquals("NSPD", response.getCode());
        assertEquals("Nasi Padang", response.getName());
    }
    
    @Test
    void testGetProductByCodeNotFound() {
        when(repository.findByCode("INVALID")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            service.getProductByCode("INVALID");
        });
    }
    
    @Test
    void testUpdateProductStatusSuccess() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(ProductStatus.INACTIVE);
        
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);
        
        ProductResponse response = service.updateProductStatus("NSPD", request);
        
        assertNotNull(response);
        assertEquals(ProductStatus.INACTIVE, response.getStatus());
    }
    
    @Test
    void testDeleteProductSuccess() {
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        
        assertDoesNotThrow(() -> {
            service.deleteProduct("NSPD");
        });
        
        verify(repository, times(1)).delete(any(Product.class));
    }
}