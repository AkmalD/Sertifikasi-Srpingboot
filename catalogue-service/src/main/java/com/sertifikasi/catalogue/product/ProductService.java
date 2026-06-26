package com.sertifikasi.catalogue.product;

import com.sertifikasi.catalogue.exception.BadRequestException;
import com.sertifikasi.catalogue.product.dto.ProductRequest;
import com.sertifikasi.catalogue.product.dto.ProductResponse;
import com.sertifikasi.catalogue.product.dto.UpdateProductRequest;
import com.sertifikasi.catalogue.product.dto.UpdateStatusRequest;

import org.springframework.dao.DataIntegrityViolationException;
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
        validateCreateRequest(request);

        String code = request.getCode().trim();
        if (repository.findByCode(code).isPresent()) {
            throw new BadRequestException("Product dengan code " + request.getCode() + " sudah ada");
        }
        
        Product product = new Product();

        product.setCode(code);
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        
        Product savedProduct = saveProduct(product);
        return mapToResponse(savedProduct);
    }
    
    public ProductResponse updateProduct(String code, ProductRequest request) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        validateUpdateRequest(request);

        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        
        Product updatedProduct = saveProduct(product);
        return mapToResponse(updatedProduct);
    }
    
    public ProductResponse updateProductStock(String code, UpdateProductRequest request) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));

        if (request == null) {
            throw new BadRequestException("Request tidak boleh kosong");
        }
        
        // Update hanya field yang dikirim (partial update)
        if (request.getName() != null) {
            validateName(request.getName());
            product.setName(request.getName().trim());
        }
        if (request.getPrice() != null) {
            validatePrice(request.getPrice());
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            validateStock(request.getStock());
            product.setStock(request.getStock());
        }
        
        Product updatedProduct = saveProduct(product);
        return mapToResponse(updatedProduct);
    }

    public ProductResponse reduceProductStock(String code, Integer quantity) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity harus lebih dari 0");
        }
        
        if (product.getStock() < quantity) {
            throw new BadRequestException("Stok tidak cukup. Stok saat ini: " + product.getStock());
        }
        
        product.setStock(product.getStock() - quantity);  // ✅ KURANGI stok
        Product updatedProduct = saveProduct(product);
        return mapToResponse(updatedProduct);
    }

    public ProductResponse restoreProductStock(String code, Integer quantity) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity harus lebih dari 0");
        }

        product.setStock(product.getStock() + quantity);
        Product updatedProduct = saveProduct(product);
        return mapToResponse(updatedProduct);
    }

    public ProductResponse updateProductStatus(String code, UpdateStatusRequest request) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        if (request == null) {
            throw new BadRequestException("Request tidak boleh kosong");
        }

        if (request.getStatus() == null) {
            throw new BadRequestException("Status tidak boleh kosong");
        }
        
        product.setStatus(request.getStatus());
        Product updatedProduct = saveProduct(product);
        return mapToResponse(updatedProduct);
    }
    
    public void deleteProduct(String code) {
        Product product = repository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Product dengan code " + code + " tidak ditemukan"));
        
        repository.delete(product);
    }

    private void validateCreateRequest(ProductRequest request) {
        if (request == null) {
            throw new BadRequestException("Request tidak boleh kosong");
        }

        validateCode(request.getCode());
        validateName(request.getName());
        validatePrice(request.getPrice());
        validateStock(request.getStock());
    }

    private void validateUpdateRequest(ProductRequest request) {
        if (request == null) {
            throw new BadRequestException("Request tidak boleh kosong");
        }

        validateName(request.getName());
        validatePrice(request.getPrice());
        validateStock(request.getStock());
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Code wajib diisi");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Name wajib diisi");
        }
    }

    private void validatePrice(Double price) {
        if (price == null || !Double.isFinite(price) || price <= 0) {
            throw new BadRequestException("Price harus lebih dari 0");
        }
    }

    private void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new BadRequestException("Stock minimal 0");
        }
    }

    private Product saveProduct(Product product) {
        try {
            return repository.save(product);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Product dengan code " + product.getCode() + " sudah ada");
        }
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
