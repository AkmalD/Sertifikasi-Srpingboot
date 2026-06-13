package com.sertifikasi.catalogue.product;

import com.sertifikasi.catalogue.product.dto.ProductRequest;
import com.sertifikasi.catalogue.product.dto.ProductResponse;
import com.sertifikasi.catalogue.product.dto.UpdateProductRequest;
import com.sertifikasi.catalogue.product.dto.UpdateStatusRequest;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    // Menghubungkan ProductService dengan ProductController
    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return service.getAllProducts();
    }

    @GetMapping("/{code}")
    public ProductResponse getProductByCode(@PathVariable String code) {
        return service.getProductByCode(code);
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        return service.createProduct(request);
    }

    @PutMapping("/{code}")
    public ProductResponse updateProduct(@PathVariable String code, @RequestBody ProductRequest request) {
        return service.updateProduct(code, request);
    }

    @PatchMapping("/{code}")
    public ProductResponse updateProductStock(@PathVariable String code, @RequestBody UpdateProductRequest request) {
        return service.updateProductStock(code, request);
    }

    @PatchMapping("/{code}/status")
    public ProductResponse updateProductStatus(@PathVariable String code, @RequestBody UpdateStatusRequest request) {
        return service.updateProductStatus(code, request);
    }

    @DeleteMapping("/{code}")
    public void deleteProduct(@PathVariable String code) {
        service.deleteProduct(code);
    }
}

