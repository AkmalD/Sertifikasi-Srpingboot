package com.sertifikasi.order.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String customerName;
    private String customerEmail;
    private List<OrderItemRequest> items;
}
