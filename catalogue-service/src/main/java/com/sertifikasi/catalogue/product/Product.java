package com.sertifikasi.catalogue.product;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String code;
    private String name;
    private Double price;
    private Integer stock;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    
    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ProductStatus.ACTIVE;
        }
    }
}

