package com.sertifikasi.order.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sertifikasi.order.client.CatalogueClient;
import com.sertifikasi.order.order.dto.OrderItemRequest;
import com.sertifikasi.order.order.dto.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private CatalogueClient catalogueClient;

    @BeforeEach
    void setUp() {
        // Bersihkan database sebelum setiap test
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrderIntegration() throws Exception {
        // 1. Setup Data Mock untuk CatalogueClient
        CatalogueClient.ProductDTO mockProduct = new CatalogueClient.ProductDTO();
        mockProduct.id = 1L;
        mockProduct.code = "BSO";
        mockProduct.name = "Baso";
        mockProduct.price = 15000.0;
        mockProduct.stock = 10;
        mockProduct.status = "ACTIVE";

        // Atur perilaku mock: Jika order service memanggil getProductByCode("BSO"), kembalikan mockProduct
        when(catalogueClient.getProductByCode("BSO")).thenReturn(mockProduct);
        // Atur perilaku mock: Jika order service memanggil reduceProductStock, jangan lakukan apapun (do nothing)
        doNothing().when(catalogueClient).reduceProductStock(eq("BSO"), anyInt());

        // 2. Siapkan Request Body untuk Create Order
        OrderItemRequest item = new OrderItemRequest("BSO", 2);
        OrderRequest request = new OrderRequest("Akmal", "akmal@example.com", List.of(item));

        // 3. Eksekusi Request & Verifikasi Hasilnya
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Berharap HTTP 200 OK
                .andExpect(jsonPath("$.customerName").value("Akmal")) // Nama sesuai
                .andExpect(jsonPath("$.status").value("PENDING")) // Status default PENDING
                .andExpect(jsonPath("$.totalAmount").value(30000.0)); // Total harga 15.000 * 2
    }
}