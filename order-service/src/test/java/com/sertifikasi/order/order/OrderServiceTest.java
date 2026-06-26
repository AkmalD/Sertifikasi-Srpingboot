package com.sertifikasi.order.order;

import com.sertifikasi.order.client.CatalogueClient;
import com.sertifikasi.order.client.CatalogueClient.ProductDTO;
import com.sertifikasi.order.order.dto.OrderItemRequest;
import com.sertifikasi.order.order.dto.OrderRequest;
import com.sertifikasi.order.order.dto.OrderResponse;
import com.sertifikasi.order.orderitem.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private CatalogueClient catalogueClient;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(repository, catalogueClient);
    }

    @Test
    void createOrderReducesStockAndStoresSnapshot() {
        OrderRequest request = orderRequest("Budi", "budi@example.com", itemRequest("NSPD", 2));
        when(catalogueClient.getProductByCode("NSPD")).thenReturn(product("NSPD", "Nasi Padang", 25000.0, 10, "ACTIVE"));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.createOrder(request);

        assertEquals("Budi", response.getCustomerName());
        assertEquals("budi@example.com", response.getCustomerEmail());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(50000.0, response.getTotalAmount());
        assertEquals("Nasi Padang", response.getItems().get(0).getProductName());
        assertEquals(25000.0, response.getItems().get(0).getProductPrice());
        verify(catalogueClient).reduceProductStock("NSPD", 2);
        verify(catalogueClient, never()).restoreProductStock(anyString(), anyInt());
    }

    @Test
    void createOrderAggregatesStockReductionForDuplicateProducts() {
        OrderRequest request = orderRequest(
            "Budi",
            "budi@example.com",
            itemRequest("NSPD", 2),
            itemRequest("NSPD", 3)
        );
        when(catalogueClient.getProductByCode("NSPD")).thenReturn(product("NSPD", "Nasi Padang", 25000.0, 10, "ACTIVE"));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createOrder(request);

        verify(catalogueClient).reduceProductStock("NSPD", 5);
    }

    @Test
    void createOrderWithInvalidEmailFails() {
        OrderRequest request = orderRequest("Budi", "invalid-email", itemRequest("NSPD", 1));

        assertThrows(RuntimeException.class, () -> service.createOrder(request));
        verifyNoInteractions(catalogueClient, repository);
    }

    @Test
    void createOrderWithEmptyItemsFails() {
        OrderRequest request = orderRequest("Budi", "budi@example.com");

        assertThrows(RuntimeException.class, () -> service.createOrder(request));
        verifyNoInteractions(catalogueClient, repository);
    }

    @Test
    void createOrderWithZeroQuantityFails() {
        OrderRequest request = orderRequest("Budi", "budi@example.com", itemRequest("NSPD", 0));

        assertThrows(RuntimeException.class, () -> service.createOrder(request));
        verifyNoInteractions(catalogueClient, repository);
    }

    @Test
    void createOrderWithInactiveProductFails() {
        OrderRequest request = orderRequest("Budi", "budi@example.com", itemRequest("NSPD", 1));
        when(catalogueClient.getProductByCode("NSPD")).thenReturn(product("NSPD", "Nasi Padang", 25000.0, 10, "INACTIVE"));

        assertThrows(RuntimeException.class, () -> service.createOrder(request));
        verify(catalogueClient, never()).reduceProductStock(anyString(), anyInt());
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void createOrderWithInsufficientStockFails() {
        OrderRequest request = orderRequest("Budi", "budi@example.com", itemRequest("NSPD", 2));
        when(catalogueClient.getProductByCode("NSPD")).thenReturn(product("NSPD", "Nasi Padang", 25000.0, 1, "ACTIVE"));

        assertThrows(RuntimeException.class, () -> service.createOrder(request));
        verify(catalogueClient, never()).reduceProductStock(anyString(), anyInt());
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void createOrderRestoresReducedStockWhenSaveFails() {
        OrderRequest request = orderRequest("Budi", "budi@example.com", itemRequest("NSPD", 2));
        when(catalogueClient.getProductByCode("NSPD")).thenReturn(product("NSPD", "Nasi Padang", 25000.0, 10, "ACTIVE"));
        when(repository.save(any(Order.class))).thenThrow(new RuntimeException("database error"));

        assertThrows(RuntimeException.class, () -> service.createOrder(request));
        verify(catalogueClient).reduceProductStock("NSPD", 2);
        verify(catalogueClient).restoreProductStock("NSPD", 2);
    }

    @Test
    void payOrderOnlyChangesPendingOrderToPaid() {
        Order order = order(OrderStatus.PENDING, item("NSPD", 2));
        when(repository.findByOrderCode("ORD-123")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.payOrder("ORD-123");

        assertEquals(OrderStatus.PAID, response.getStatus());
        verify(catalogueClient, never()).reduceProductStock(anyString(), anyInt());
        verify(catalogueClient, never()).restoreProductStock(anyString(), anyInt());
    }

    @Test
    void payOrderWithNonPendingStatusFails() {
        Order order = order(OrderStatus.PAID, item("NSPD", 2));
        when(repository.findByOrderCode("ORD-123")).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> service.payOrder("ORD-123"));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderRestoresStockAndCancelsPendingOrder() {
        Order order = order(OrderStatus.PENDING, item("NSPD", 2), item("NSPD", 1));
        when(repository.findByOrderCode("ORD-123")).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.cancelOrder("ORD-123");

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(catalogueClient).restoreProductStock("NSPD", 3);
    }

    @Test
    void cancelOrderWithNonPendingStatusFails() {
        Order order = order(OrderStatus.PAID, item("NSPD", 2));
        when(repository.findByOrderCode("ORD-123")).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> service.cancelOrder("ORD-123"));
        verify(catalogueClient, never()).restoreProductStock(anyString(), anyInt());
        verify(repository, never()).save(any(Order.class));
    }

    private OrderRequest orderRequest(String customerName, String customerEmail, OrderItemRequest... items) {
        OrderRequest request = new OrderRequest();
        request.setCustomerName(customerName);
        request.setCustomerEmail(customerEmail);
        request.setItems(List.of(items));
        return request;
    }

    private OrderItemRequest itemRequest(String productCode, Integer quantity) {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductCode(productCode);
        request.setQuantity(quantity);
        return request;
    }

    private ProductDTO product(String code, String name, Double price, Integer stock, String status) {
        ProductDTO product = new ProductDTO();
        product.id = 1L;
        product.code = code;
        product.name = name;
        product.price = price;
        product.stock = stock;
        product.status = status;
        return product;
    }

    private Order order(OrderStatus status, OrderItem... items) {
        Order order = new Order();
        order.setOrderCode("ORD-123");
        order.setCustomerName("Budi");
        order.setCustomerEmail("budi@example.com");
        order.setStatus(status);
        order.setTotalAmount(50000.0);
        order.setItems(List.of(items));
        return order;
    }

    private OrderItem item(String productCode, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setProductCode(productCode);
        item.setProductName("Nasi Padang");
        item.setProductPrice(25000.0);
        item.setQuantity(quantity);
        return item;
    }
}
