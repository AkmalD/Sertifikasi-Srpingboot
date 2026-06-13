package com.sertifikasi.catalogue.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Membuat method supaya bisa mencari product berdasarkan code, bukan id
    Optional<Product> findByCode(String code);
}