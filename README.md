# Sertifikasi Microservices

Sistem e-commerce berbasis microservices dengan 3 service: API Gateway, Catalogue Service, dan Order Service.

## Prasyarat

Pastikan sudah terinstall:
- Java JDK 17+
- PostgreSQL 12+
- Maven 3.8+
- Postman (untuk testing)

---

## Setup Database

Buka terminal dan masuk ke PostgreSQL, lalu buat dua database:

```sql
psql -U postgres

CREATE DATABASE catalogue_db;
CREATE DATABASE order_db;

\q
```

---

## Cara Menjalankan

> Butuh **3 terminal terpisah** — satu untuk setiap service.

### Terminal 1 — Catalogue Service (port 8081)

```bash
cd catalogue-service
mvn spring-boot:run
```

Tunggu sampai muncul:
```
Tomcat started on port 8081
```

### Terminal 2 — Order Service (port 8082)

```bash
cd order-service
mvn spring-boot:run
```

Tunggu sampai muncul:
```
Tomcat started on port 8082
```

### Terminal 3 — API Gateway (port 8080)

```bash
cd api-gateway
mvn spring-boot:run
```

Tunggu sampai muncul:
```
Tomcat started on port 8080
```

> **Urutan penting:** jalankan catalogue-service dan order-service dulu sebelum api-gateway.

---

## Alternatif: Jalankan dengan Docker Compose

Kalau sudah ada Docker, cukup satu perintah:

```bash
docker-compose up --build
```

Semua service dan database akan berjalan otomatis.

---

## Authentication

Semua endpoint dilindungi JWT. Langkah pertama selalu login dulu:

```http
POST http://localhost:8080/api/auth/login?username=admin&password=password
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Gunakan token tersebut di header setiap request berikutnya:
```
Authorization: Bearer <token>
```

---

## API Endpoints

Semua request dikirim ke API Gateway di `http://localhost:8080`.

### Produk

| Method | Endpoint | Keterangan |
|--------|----------|------------|
| GET | `/api/products` | Ambil semua produk |
| GET | `/api/products/{code}` | Ambil produk by code |
| POST | `/api/products` | Buat produk baru |
| PUT | `/api/products/{code}` | Update penuh produk |
| PATCH | `/api/products/{code}` | Partial update (name/price/stock) |
| PATCH | `/api/products/{code}/status` | Update status (ACTIVE/INACTIVE) |
| PATCH | `/api/products/{code}/reduce-stock` | Kurangi stok |
| DELETE | `/api/products/{code}` | Hapus produk |

Contoh membuat produk:
```json
POST /api/products
{
  "code": "NSPD",
  "name": "Nasi Padang",
  "price": 25000,
  "stock": 100
}
```

### Order

| Method | Endpoint | Keterangan |
|--------|----------|------------|
| GET | `/api/orders` | Ambil semua order |
| GET | `/api/orders/{code}` | Ambil order by code |
| POST | `/api/orders` | Buat order baru (status: PENDING, stok berkurang) |
| POST | `/api/orders/{code}/pay` | Bayar order (PENDING → PAID) |
| POST | `/api/orders/{code}/cancel` | Batalkan order (PENDING → CANCELLED, stok kembali) |

Contoh membuat order:
```json
POST /api/orders
{
  "customerName": "Budi",
  "customerEmail": "budi@example.com",
  "items": [
    { "productCode": "NSPD", "quantity": 2 }
  ]
}
```

---

## Dokumentasi API (Swagger)

Setelah service berjalan, buka:
- Catalogue Service: http://localhost:8081/swagger-ui.html
- Order Service: http://localhost:8082/swagger-ui.html

---

## Menjalankan Test

```bash
# Unit test catalogue service
cd catalogue-service
mvn test -Dtest=ProductServiceTest

# Integration test catalogue service
mvn test -Dtest=ProductControllerIT

# Unit test order service
cd order-service
mvn test -Dtest=OrderServiceTest
```

---

## Troubleshooting

**Port sudah dipakai**
```bash
# Windows — cek siapa yang pakai port 8080
netstat -ano | findstr :8080
```

**Database tidak konek**  
Pastikan PostgreSQL sedang berjalan dan kedua database sudah dibuat. Cek dengan:
```bash
psql -U postgres -d catalogue_db -c "SELECT 1"
```

**Product not found saat create order**  
Pastikan product sudah ada dan statusnya `ACTIVE`. Product dengan status `INACTIVE` tidak bisa dipesan.

**Stok tidak berkurang setelah pay**  
Stok berkurang saat **create order**, bukan saat pay. Saat cancel, stok dikembalikan otomatis.
