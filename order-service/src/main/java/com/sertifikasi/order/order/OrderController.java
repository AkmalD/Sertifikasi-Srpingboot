package com.sertifikasi.order.order;

import com.sertifikasi.order.order.dto.OrderRequest;
import com.sertifikasi.order.order.dto.OrderResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Manajemen order: buat, lihat, bayar, dan batalkan order")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(
            summary = "Buat order baru",
            description = """
                    Membuat order baru dengan satu atau lebih item produk.
                    
                    Flow yang terjadi di background:
                    1. Validasi data request (email, items, quantity)
                    2. Ambil data produk dari Catalogue Service
                    3. Validasi stok dan status produk (harus ACTIVE)
                    4. Kurangi stok di Catalogue Service
                    5. Simpan order ke database
                    
                    Jika penyimpanan ke database gagal, stok yang sudah dikurangi akan dikembalikan secara otomatis (rollback).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order berhasil dibuat dengan status PENDING",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request tidak valid: email salah, items kosong, stok tidak cukup, atau produk INACTIVE",
                    content = @Content(schema = @Schema(example = """
                            {"status":400,"error":"Bad Request","message":"Stok produk NSPD tidak cukup. Stok saat ini: 3"}
                            """)))
    })
    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return service.createOrder(request);
    }

    @Operation(
            summary = "Ambil semua order",
            description = "Mengembalikan seluruh order yang tersimpan di database."
    )
    @ApiResponse(responseCode = "200", description = "Daftar order berhasil diambil")
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return service.getAllOrders();
    }

    @Operation(
            summary = "Ambil order berdasarkan kode",
            description = "Mencari satu order menggunakan order code (format: ORD-XXXXXXXX)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order ditemukan",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order tidak ditemukan",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Order dengan code ORD-XXXXXXXX tidak ditemukan\"}")))
    })
    @GetMapping("/{code}")
    public OrderResponse getOrderByCode(
            @Parameter(description = "Kode unik order, format: ORD-XXXXXXXX", example = "ORD-A1B2C3D4")
            @PathVariable String code) {
        return service.getOrderByCode(code);
    }

    @Operation(
            summary = "Bayar order",
            description = "Mengubah status order dari PENDING menjadi PAID. Hanya order berstatus PENDING yang bisa dibayar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order berhasil dibayar, status berubah menjadi PAID"),
            @ApiResponse(responseCode = "400", description = "Order tidak dalam status PENDING",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Hanya order dengan status PENDING yang bisa dibayar\"}"))),
            @ApiResponse(responseCode = "404", description = "Order tidak ditemukan")
    })
    @PostMapping("/{code}/pay")
    public OrderResponse payOrder(
            @Parameter(description = "Kode order yang akan dibayar", example = "ORD-A1B2C3D4")
            @PathVariable String code) {
        return service.payOrder(code);
    }

    @Operation(
            summary = "Batalkan order",
            description = """
                    Membatalkan order dan mengembalikan stok produk ke Catalogue Service.
                    
                    Flow yang terjadi:
                    1. Validasi order harus berstatus PENDING
                    2. Kembalikan stok semua item ke Catalogue Service
                    3. Ubah status order menjadi CANCELLED
                    
                    Jika ada kegagalan saat restore stok, order tetap dalam status PENDING (rollback cancel).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order berhasil dibatalkan, status berubah menjadi CANCELLED, stok dikembalikan"),
            @ApiResponse(responseCode = "400", description = "Order tidak dalam status PENDING",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Hanya order dengan status PENDING yang bisa dibatalkan\"}"))),
            @ApiResponse(responseCode = "404", description = "Order tidak ditemukan")
    })
    @PostMapping("/{code}/cancel")
    public OrderResponse cancelOrder(
            @Parameter(description = "Kode order yang akan dibatalkan", example = "ORD-A1B2C3D4")
            @PathVariable String code) {
        return service.cancelOrder(code);
    }
}
