package com.sertifikasi.catalogue.product;

import com.sertifikasi.catalogue.product.dto.ProductRequest;
import com.sertifikasi.catalogue.product.dto.ProductResponse;
import com.sertifikasi.catalogue.product.dto.UpdateProductRequest;
import com.sertifikasi.catalogue.product.dto.UpdateStatusRequest;
import com.sertifikasi.catalogue.product.dto.ReduceStockRequest;
import com.sertifikasi.catalogue.product.dto.RestoreStockRequest;

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
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Manajemen produk: CRUD, stok, dan status")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(
            summary = "Ambil semua produk",
            description = "Mengembalikan seluruh produk yang tersimpan di database."
    )
    @ApiResponse(responseCode = "200", description = "Daftar produk berhasil diambil")
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return service.getAllProducts();
    }

    @Operation(
            summary = "Ambil produk berdasarkan kode",
            description = "Mencari satu produk menggunakan kode unik produk."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produk ditemukan",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Product dengan code XXX tidak ditemukan\"}")))
    })
    @GetMapping("/{code}")
    public ProductResponse getProductByCode(
            @Parameter(description = "Kode unik produk, contoh: NSPD", example = "NSPD")
            @PathVariable String code) {
        return service.getProductByCode(code);
    }

    @Operation(
            summary = "Buat produk baru",
            description = "Membuat produk baru. Kode produk harus unik. Status default ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produk berhasil dibuat",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request tidak valid (kode duplikat, harga <= 0, dsb.)",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Product dengan code NSPD sudah ada\"}")))
    })
    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        return service.createProduct(request);
    }

    @Operation(
            summary = "Update penuh produk",
            description = "Mengganti name, price, dan stock sekaligus. Kode produk tidak bisa diubah."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produk berhasil diupdate"),
            @ApiResponse(responseCode = "400", description = "Request tidak valid"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PutMapping("/{code}")
    public ProductResponse updateProduct(
            @Parameter(description = "Kode produk yang akan diupdate", example = "NSPD")
            @PathVariable String code,
            @RequestBody ProductRequest request) {
        return service.updateProduct(code, request);
    }

    @Operation(
            summary = "Update sebagian field produk",
            description = "Update partial: hanya field yang dikirim yang akan diubah (name, price, atau stock)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produk berhasil diupdate"),
            @ApiResponse(responseCode = "400", description = "Nilai field tidak valid"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PatchMapping("/{code}")
    public ProductResponse updateProductStock(
            @Parameter(description = "Kode produk", example = "NSPD")
            @PathVariable String code,
            @RequestBody UpdateProductRequest request) {
        return service.updateProductStock(code, request);
    }

    @Operation(
            summary = "Kurangi stok produk",
            description = "Mengurangi stok produk sebesar quantity yang diberikan. Digunakan oleh order-service saat order dibuat."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stok berhasil dikurangi"),
            @ApiResponse(responseCode = "400", description = "Stok tidak cukup atau quantity tidak valid"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PatchMapping("/{code}/reduce-stock")
    public ProductResponse reduceProductStock(
            @Parameter(description = "Kode produk", example = "NSPD")
            @PathVariable String code,
            @RequestBody ReduceStockRequest request) {
        return service.reduceProductStock(code, request == null ? null : request.getQuantity());
    }

    @Operation(
            summary = "Kembalikan stok produk",
            description = "Menambah kembali stok produk sebesar quantity. Digunakan saat order dibatalkan."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stok berhasil dikembalikan"),
            @ApiResponse(responseCode = "400", description = "Quantity tidak valid"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PatchMapping("/{code}/restore-stock")
    public ProductResponse restoreProductStock(
            @Parameter(description = "Kode produk", example = "NSPD")
            @PathVariable String code,
            @RequestBody RestoreStockRequest request) {
        return service.restoreProductStock(code, request == null ? null : request.getQuantity());
    }

    @Operation(
            summary = "Update status produk",
            description = "Mengubah status produk menjadi ACTIVE atau INACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status berhasil diupdate"),
            @ApiResponse(responseCode = "400", description = "Status tidak valid"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @PatchMapping("/{code}/status")
    public ProductResponse updateProductStatus(
            @Parameter(description = "Kode produk", example = "NSPD")
            @PathVariable String code,
            @RequestBody UpdateStatusRequest request) {
        return service.updateProductStatus(code, request);
    }

    @Operation(
            summary = "Hapus produk",
            description = "Menghapus produk secara permanen berdasarkan kode."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produk berhasil dihapus"),
            @ApiResponse(responseCode = "404", description = "Produk tidak ditemukan")
    })
    @DeleteMapping("/{code}")
    public void deleteProduct(
            @Parameter(description = "Kode produk yang akan dihapus", example = "NSPD")
            @PathVariable String code) {
        service.deleteProduct(code);
    }
}
