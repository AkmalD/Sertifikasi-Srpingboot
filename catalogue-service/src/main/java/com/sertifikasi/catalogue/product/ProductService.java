package com.sertifikasi.catalogue.product;

import com.sertifikasi.catalogue.exception.BadRequestException;
import com.sertifikasi.catalogue.product.dto.ProductRequest;
import com.sertifikasi.catalogue.product.dto.ProductResponse;
import com.sertifikasi.catalogue.product.dto.UpdateProductRequest;
import com.sertifikasi.catalogue.product.dto.UpdateStatusRequest;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    private final ProductRepository repository;
    
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    
    public List<ProductResponse> getAllProducts() {
        return repository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public ProductResponse getProductByCode(String code) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        return mapToResponse(product);
    }
    
    public ProductResponse createProduct(ProductRequest request) {
        if (request.getCode() == null || request.getName() == null || request.getPrice() == null || request.getStock() == null) {
            throw new BadRequestException("Request tidak boleh ada yang null");
        }
        
        Product product = new Product();

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        
        Product savedProduct = repository.save(product);
        return mapToResponse(savedProduct);
    }
    
    public ProductResponse updateProduct(String code, ProductRequest request) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        if (request.getName() == null || request.getPrice() == null || request.getStock() == null) {
            throw new BadRequestException("Request tidak boleh ada yang null");
        }

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        
        Product updatedProduct = repository.save(product);
        return mapToResponse(updatedProduct);
    }
    
    public ProductResponse updateProductStock(String code, UpdateProductRequest request) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        // Update hanya field yang dikirim (partial update)
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        
        Product updatedProduct = repository.save(product);
        return mapToResponse(updatedProduct);
    }

    public ProductResponse updateProductStatus(String code, UpdateStatusRequest request) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        if (request.getStatus() == null) {
            throw new BadRequestException("Status tidak boleh kosong");
        }
        
        product.setStatus(request.getStatus());
        Product updatedProduct = repository.save(product);
        return mapToResponse(updatedProduct);
    }
    
    public void deleteProduct(String code) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        repository.delete(product);
    }
    
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getCode(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            product.getStatus()
        );
    }
}
