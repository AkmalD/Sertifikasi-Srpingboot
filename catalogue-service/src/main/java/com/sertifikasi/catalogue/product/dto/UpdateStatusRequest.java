package com.sertifikasi.catalogue.product.dto;

import com.sertifikasi.catalogue.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    private ProductStatus status;
}