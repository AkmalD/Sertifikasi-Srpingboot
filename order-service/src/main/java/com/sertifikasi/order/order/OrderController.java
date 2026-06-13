package com.sertifikasi.order.order;

import com.sertifikasi.order.order.dto.OrderRequest;
import com.sertifikasi.order.order.dto.OrderResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final OrderService service;
    
    public OrderController(OrderService service) {
        this.service = service;
    }
    
    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return service.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return service.getAllOrders();
    }
    
    @GetMapping("/{code}")
    public OrderResponse getOrderByCode(@PathVariable String code) {
        return service.getOrderByCode(code);
    }
    
    @PostMapping("/{code}/pay")
    public OrderResponse payOrder(@PathVariable String code) {
        return service.payOrder(code);
    }
    
    @PostMapping("/{code}/cancel")
    public OrderResponse cancelOrder(@PathVariable String code) {
        return service.cancelOrder(code);
    }
}
