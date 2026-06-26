package com.sertifikasi.catalogue.product;

import com.sertifikasi.catalogue.exception.BadRequestException;
import com.sertifikasi.catalogue.product.dto.ProductRequest;
import com.sertifikasi.catalogue.product.dto.ProductResponse;
import com.sertifikasi.catalogue.product.dto.UpdateProductRequest;
import com.sertifikasi.catalogue.product.dto.UpdateStatusRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.dao.DataIntegrityViolationException;

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
        when(repository.findByCode("NSPD")).thenReturn(Optional.empty());
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
    void testCreateProductWithDuplicateCodeFails() {
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        
        assertThrows(BadRequestException.class, () -> {
            service.createProduct(productRequest);
        });
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void testCreateProductWithDatabaseDuplicateFails() {
        when(repository.findByCode("NSPD")).thenReturn(Optional.empty());
        when(repository.save(any(Product.class))).thenThrow(new DataIntegrityViolationException("duplicate code"));
        
        assertThrows(BadRequestException.class, () -> {
            service.createProduct(productRequest);
        });
    }

    @Test
    void testCreateProductWithBlankNameFails() {
        productRequest.setName("   ");
        
        assertThrows(BadRequestException.class, () -> {
            service.createProduct(productRequest);
        });
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void testCreateProductWithZeroPriceFails() {
        productRequest.setPrice(0.0);
        
        assertThrows(BadRequestException.class, () -> {
            service.createProduct(productRequest);
        });
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void testCreateProductWithNegativeStockFails() {
        productRequest.setStock(-1);
        
        assertThrows(BadRequestException.class, () -> {
            service.createProduct(productRequest);
        });
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProductWithBlankNameFails() {
        productRequest.setName(" ");
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        
        assertThrows(BadRequestException.class, () -> {
            service.updateProduct("NSPD", productRequest);
        });
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProductWithNegativePriceFails() {
        productRequest.setPrice(-1000.0);
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        
        assertThrows(BadRequestException.class, () -> {
            service.updateProduct("NSPD", productRequest);
        });
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void testPartialUpdateProductWithZeroPriceFails() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setPrice(0.0);
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        
        assertThrows(BadRequestException.class, () -> {
            service.updateProductStock("NSPD", request);
        });
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void testPartialUpdateProductWithNegativeStockFails() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setStock(-1);
        when(repository.findByCode("NSPD")).thenReturn(Optional.of(product));
        
        assertThrows(BadRequestException.class, () -> {
            service.updateProductStock("NSPD", request);
        });
        verify(repository, never()).save(any(Product.class));
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
