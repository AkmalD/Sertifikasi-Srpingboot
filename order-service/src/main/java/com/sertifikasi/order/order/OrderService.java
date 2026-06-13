package com.sertifikasi.order.order;

import com.sertifikasi.order.order.dto.OrderRequest;
import com.sertifikasi.order.order.dto.OrderResponse;
import com.sertifikasi.order.orderitem.OrderItem;
import com.sertifikasi.order.client.CatalogueClient;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class OrderService {
    
    private final OrderRepository repository;
    private final CatalogueClient catalogueClient;
    
    public OrderService(OrderRepository repository, CatalogueClient catalogueClient) {
        this.repository = repository;
        this.catalogueClient = catalogueClient;
    }
    
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order harus memiliki minimal 1 item");
        }
        
        Order order = new Order();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(OrderStatus.PENDING);
        
        List<OrderItem> items = new ArrayList<>();
        Double totalAmount = 0.0;
        
        for (var itemRequest : request.getItems()) {
            // Get product from catalogue service
            var product = catalogueClient.getProductByCode(itemRequest.getProductCode());
            
            // Validasi product ACTIVE
            if (!"ACTIVE".equals(product.getStatus())) {
                throw new RuntimeException("Product " + product.getCode() + " tidak tersedia (status: " + product.getStatus() + ")");
            }
            
            OrderItem item = new OrderItem();
            item.setProductCode(product.getCode());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setProductId(product.getId());
            item.setQuantity(itemRequest.getQuantity());
            
            items.add(item);
            totalAmount += product.getPrice() * itemRequest.getQuantity();
        }
        
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = repository.save(order);
        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = repository.findAll();
        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            responses.add(mapToResponse(order));
        }
        return responses;
    }

    public OrderResponse getOrderByCode(String code) {
        Order order = repository.findByOrderCode(code)
            .orElseThrow(() -> new RuntimeException("Order dengan code " + code + " tidak ditemukan"));
        return mapToResponse(order);
    }
    
    public OrderResponse payOrder(String code) {
        Order order = repository.findByOrderCode(code)
            .orElseThrow(() -> new RuntimeException("Order dengan code " + code + " tidak ditemukan"));
        
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new RuntimeException("Hanya order dengan status PENDING yang bisa dibayar");
        }
        
        // Kurangi stok untuk setiap item
        for (OrderItem item : order.getItems()) {
            catalogueClient.updateProductStock(item.getProductCode(), item.getQuantity());
        }
        
        order.setStatus(OrderStatus.PAID);
        Order updatedOrder = repository.save(order);
        
        return mapToResponse(updatedOrder);
    }
    
    public OrderResponse cancelOrder(String code) {
        Order order = repository.findByOrderCode(code)
            .orElseThrow(() -> new RuntimeException("Order dengan code " + code + " tidak ditemukan"));
        
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new RuntimeException("Hanya order dengan status PENDING yang bisa dibatalkan");
        }
        
        // Stok tidak berubah, langsung cancel
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = repository.save(order);
        
        return mapToResponse(updatedOrder);
    }
    
    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderCode(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getItems()
        );
    }
}
