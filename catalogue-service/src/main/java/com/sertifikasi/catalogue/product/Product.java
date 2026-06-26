package com.sertifikasi.catalogue.product;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "product",
    uniqueConstraints = @UniqueConstraint(name = "uk_product_code", columnNames = "code")
)
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
    
    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ProductStatus.ACTIVE;
        }
    }
}

