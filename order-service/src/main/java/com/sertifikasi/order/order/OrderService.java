package com.sertifikasi.order.order;

import com.sertifikasi.order.client.CatalogueClient;
import com.sertifikasi.order.client.CatalogueClient.ProductDTO;
import com.sertifikasi.order.exception.BadRequestException;
import com.sertifikasi.order.order.dto.OrderItemRequest;
import com.sertifikasi.order.order.dto.OrderRequest;
import com.sertifikasi.order.order.dto.OrderResponse;
import com.sertifikasi.order.orderitem.OrderItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class OrderService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );

    private final OrderRepository repository;
    private final CatalogueClient catalogueClient;

    public OrderService(OrderRepository repository, CatalogueClient catalogueClient) {
        this.repository = repository;
        this.catalogueClient = catalogueClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        validateOrderRequest(request);

        Order order = new Order();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(request.getCustomerName().trim());
        order.setCustomerEmail(request.getCustomerEmail().trim());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        Map<String, ProductDTO> productsByCode = new LinkedHashMap<>();
        Map<String, Integer> quantitiesByCode = new LinkedHashMap<>();
        double totalAmount = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            validateItemRequest(itemRequest);

            String requestedProductCode = itemRequest.getProductCode().trim();
            ProductDTO product = productsByCode.computeIfAbsent(requestedProductCode, catalogueClient::getProductByCode);
            validateProduct(product, requestedProductCode);
            productsByCode.putIfAbsent(product.getCode(), product);

            Integer quantity = itemRequest.getQuantity();
            quantitiesByCode.merge(product.getCode(), quantity, Integer::sum);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductCode(product.getCode());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(quantity);

            items.add(item);
            totalAmount += product.getPrice() * quantity;
        }

        validateStockAvailability(quantitiesByCode, productsByCode);

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        List<StockAdjustment> reducedStock = new ArrayList<>();
        try {
            reduceStock(quantitiesByCode, reducedStock);
            Order savedOrder = repository.save(order);
            return mapToResponse(savedOrder);
        } catch (RuntimeException ex) {
            restoreStockSafely(reducedStock);
            throw ex;
        }
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
            throw new BadRequestException("Hanya order dengan status PENDING yang bisa dibayar");
        }

        order.setStatus(OrderStatus.PAID);
        Order updatedOrder = repository.save(order);

        return mapToResponse(updatedOrder);
    }

    public OrderResponse cancelOrder(String code) {
        Order order = repository.findByOrderCode(code)
            .orElseThrow(() -> new RuntimeException("Order dengan code " + code + " tidak ditemukan"));

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException("Hanya order dengan status PENDING yang bisa dibatalkan");
        }

        Map<String, Integer> quantitiesByCode = aggregateOrderQuantities(order.getItems());
        List<StockAdjustment> restoredStock = new ArrayList<>();

        try {
            restoreStock(quantitiesByCode, restoredStock);
            order.setStatus(OrderStatus.CANCELLED);
            Order updatedOrder = repository.save(order);
            return mapToResponse(updatedOrder);
        } catch (RuntimeException ex) {
            reduceStockSafely(restoredStock);
            throw ex;
        }
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request == null) {
            throw new BadRequestException("Request order tidak boleh kosong");
        }

        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new BadRequestException("Customer name wajib diisi");
        }

        if (request.getCustomerEmail() == null || !EMAIL_PATTERN.matcher(request.getCustomerEmail().trim()).matches()) {
            throw new BadRequestException("Email customer tidak valid");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order harus memiliki minimal 1 item");
        }
    }

    private void validateItemRequest(OrderItemRequest itemRequest) {
        if (itemRequest == null) {
            throw new BadRequestException("Item order tidak boleh kosong");
        }

        if (itemRequest.getProductCode() == null || itemRequest.getProductCode().isBlank()) {
            throw new BadRequestException("Product code wajib diisi");
        }

        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
            throw new BadRequestException("Quantity minimal 1");
        }
    }

    private void validateProduct(ProductDTO product, String requestedCode) {
        if (product == null) {
            throw new BadRequestException("Produk dengan code " + requestedCode + " tidak ditemukan");
        }

        if (product.getCode() == null || product.getName() == null || product.getPrice() == null || product.getStock() == null) {
            throw new BadRequestException("Data produk " + requestedCode + " tidak lengkap");
        }

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new BadRequestException("Product " + product.getCode() + " tidak tersedia (status: " + product.getStatus() + ")");
        }
    }

    private void validateStockAvailability(Map<String, Integer> quantitiesByCode, Map<String, ProductDTO> productsByCode) {
        for (var quantityEntry : quantitiesByCode.entrySet()) {
            ProductDTO product = productsByCode.get(quantityEntry.getKey());
            if (product.getStock() < quantityEntry.getValue()) {
                throw new BadRequestException("Stok produk " + product.getCode() + " tidak cukup. Stok saat ini: " + product.getStock());
            }
        }
    }

    private Map<String, Integer> aggregateOrderQuantities(List<OrderItem> items) {
        Map<String, Integer> quantitiesByCode = new LinkedHashMap<>();
        for (OrderItem item : items) {
            quantitiesByCode.merge(item.getProductCode(), item.getQuantity(), Integer::sum);
        }
        return quantitiesByCode;
    }

    private void reduceStock(Map<String, Integer> quantitiesByCode, List<StockAdjustment> reducedStock) {
        for (var quantityEntry : quantitiesByCode.entrySet()) {
            catalogueClient.reduceProductStock(quantityEntry.getKey(), quantityEntry.getValue());
            reducedStock.add(new StockAdjustment(quantityEntry.getKey(), quantityEntry.getValue()));
        }
    }

    private void restoreStock(Map<String, Integer> quantitiesByCode, List<StockAdjustment> restoredStock) {
        for (var quantityEntry : quantitiesByCode.entrySet()) {
            catalogueClient.restoreProductStock(quantityEntry.getKey(), quantityEntry.getValue());
            restoredStock.add(new StockAdjustment(quantityEntry.getKey(), quantityEntry.getValue()));
        }
    }

    private void restoreStockSafely(List<StockAdjustment> reducedStock) {
        for (StockAdjustment adjustment : reducedStock) {
            try {
                catalogueClient.restoreProductStock(adjustment.productCode(), adjustment.quantity());
            } catch (RuntimeException ignored) {
                // Best-effort compensation for a failed create order.
            }
        }
    }

    private void reduceStockSafely(List<StockAdjustment> restoredStock) {
        for (StockAdjustment adjustment : restoredStock) {
            try {
                catalogueClient.reduceProductStock(adjustment.productCode(), adjustment.quantity());
            } catch (RuntimeException ignored) {
                // Best-effort compensation for a failed cancel order.
            }
        }
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderCode(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getItems()
        );
    }

    private record StockAdjustment(String productCode, Integer quantity) {
    }
}