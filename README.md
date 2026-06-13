# Sertifikasi Microservices Project

Project microservices untuk sistem e-commerce dengan arsitektur API Gateway, Catalogue Service, dan Order Service.

## 📋 Prerequisite

- Java JDK 17+
- PostgreSQL 12+
- Maven 3.8+
- Git
- Postman atau REST Client (untuk testing)

## 🗄️ Database Setup

### 1. Buat Database PostgreSQL

```sql
-- Koneksi ke PostgreSQL
psql -U postgres

-- Buat database untuk catalogue service
CREATE DATABASE catalogue_db;

-- Buat database untuk order service
CREATE DATABASE order_db;

-- Exit
\q
```

### 2. Verify Database

```bash
psql -U postgres -d catalogue_db -c "SELECT 1"
psql -U postgres -d order_db -c "SELECT 1"
```

## 🚀 Cara Menjalankan Project

### 1. Clone Repository

```bash
cd D:\Tugas Akhir\Sertifikasi
git clone <repository-url>
cd Sertifikasi
```

### 2. Build All Services

```bash
# Build catalogue-service
cd catalogue-service
mvn clean install
cd ..

# Build order-service
cd order-service
mvn clean install
cd ..

# Build api-gateway
cd api-gateway
mvn clean install
cd ..
```

### 3. Jalankan Services (di terminal terpisah)

#### Terminal 1: API Gateway (Port 8080)
```bash
cd api-gateway
mvn spring-boot:run
```

**Expected Output:**
```
Tomcat started on port 8080 (http) with context path '/'
```

#### Terminal 2: Catalogue Service (Port 8001)
```bash
cd catalogue-service
mvn spring-boot:run
```

**Expected Output:**
```
Tomcat started on port 8001 (http) with context path '/'
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection
```

#### Terminal 3: Order Service (Port 8002)
```bash
cd order-service
mvn spring-boot:run
```

**Expected Output:**
```
Tomcat started on port 8002 (http) with context path '/'
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection
```

## 🌐 Service URLs

| Service | Port | URL |
|---------|------|-----|
| API Gateway | 8080 | http://localhost:8080 |
| Catalogue Service | 8001 | http://localhost:8001 |
| Order Service | 8002 | http://localhost:8002 |

## 📝 API Endpoints

Semua request dikirim ke **API Gateway (port 8080)** yang kemudian di-routing ke service yang sesuai.

### Catalogue Service Endpoints

#### 1. Create Product
```http
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "code": "NSPD",
  "name": "Nasi Padang",
  "price": 25000,
  "stock": 100
}
```

**Response:**
```json
{
  "id": 1,
  "code": "NSPD",
  "name": "Nasi Padang",
  "price": 25000,
  "stock": 100,
  "status": "ACTIVE"
}
```

#### 2. Get All Products
```http
GET http://localhost:8080/api/products
```

#### 3. Get Product by Code
```http
GET http://localhost:8080/api/products/NSPD
```

#### 4. Update Product (Full Update)
```http
PUT http://localhost:8080/api/products/NSPD
Content-Type: application/json

{
  "name": "Nasi Padang Spesial",
  "price": 28000,
  "stock": 150
}
```

#### 5. Partial Update Product
```http
PATCH http://localhost:8080/api/products/NSPD
Content-Type: application/json

{
  "stock": 80,
  "price": 26000
}
```

#### 6. Update Product Status
```http
PATCH http://localhost:8080/api/products/NSPD/status
Content-Type: application/json

{
  "status": "INACTIVE"
}
```

#### 7. Reduce Product Stock
```http
PATCH http://localhost:8080/api/products/NSPD/reduce-stock
Content-Type: application/json

{
  "quantity": 5
}
```

#### 8. Delete Product
```http
DELETE http://localhost:8080/api/products/NSPD
```

---

### Order Service Endpoints

#### 1. Create Order (Status: PENDING)
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "items": [
    {
      "productCode": "NSPD",
      "quantity": 2
    },
    {
      "productCode": "BTGR",
      "quantity": 1
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "orderCode": "ORD-A1B2C3D4",
  "status": "PENDING",
  "totalAmount": 60000,
  "createdAt": "2026-06-13T15:00:00",
  "updatedAt": "2026-06-13T15:00:00",
  "items": [
    {
      "id": 1,
      "productCode": "NSPD",
      "productName": "Nasi Padang",
      "productPrice": 25000,
      "quantity": 2
    }
  ]
}
```

#### 2. Get Order Detail
```http
GET http://localhost:8080/api/orders/1
```

#### 3. Pay Order (Status: PENDING → PAID, Stok berkurang)
```http
POST http://localhost:8080/api/orders/1/pay
```

**Response:**
```json
{
  "id": 1,
  "orderCode": "ORD-A1B2C3D4",
  "status": "PAID",
  "totalAmount": 60000,
  "createdAt": "2026-06-13T15:00:00",
  "updatedAt": "2026-06-13T15:05:00",
  "items": [...]
}
```

#### 4. Cancel Order (Status: PENDING → CANCELLED, Stok tetap)
```http
POST http://localhost:8080/api/orders/1/cancel
```

**Response:**
```json
{
  "id": 1,
  "orderCode": "ORD-A1B2C3D4",
  "status": "CANCELLED",
  "totalAmount": 60000,
  "createdAt": "2026-06-13T15:00:00",
  "updatedAt": "2026-06-13T15:05:00",
  "items": [...]
}
```

## 🧪 Testing

### Unit Test
```bash
cd catalogue-service
mvn test -Dtest=ProductServiceTest
```

### Integration Test
```bash
cd catalogue-service
mvn test -Dtest=ProductControllerIT
```

## 📊 Database Schema

### Catalogue Service (catalogue_db)

**Table: product**
```sql
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(255),
    name VARCHAR(255),
    price DOUBLE,
    stock INTEGER,
    status VARCHAR(50) DEFAULT 'ACTIVE'
);
```

### Order Service (order_db)

**Table: orders**
```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_code VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING',
    total_amount DOUBLE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Table: order_items**
```sql
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT,
    product_code VARCHAR(255),
    product_name VARCHAR(255),
    product_price DOUBLE,
    quantity INTEGER,
    order_id BIGINT REFERENCES orders(id)
);
```

## 🔐 Authentication

Semua service dilengkapi Spring Security dengan:
- **Username:** `user`
- **Password:** Generated otomatis saat service start (check console log)

Untuk menghilangkan auth di development, endpoints `/api/products/**` dan `/api/orders/**` sudah di-set `permitAll()`.

## 🐛 Troubleshooting

### 1. Connection Refused (Port 8080/8001/8002)

**Solusi:** Pastikan semua service sudah dijalankan di terminal terpisah dan tidak ada port conflict.

```bash
# Check port yang digunakan
netstat -ano | findstr :8080
netstat -ano | findstr :8001
netstat -ano | findstr :8002
```

### 2. Database Connection Error

**Solusi:** Pastikan PostgreSQL running dan database sudah dibuat:

```bash
# Restart PostgreSQL
net stop postgresql-x64-15
net start postgresql-x64-15

# Verify connection
psql -U postgres -d catalogue_db -c "SELECT 1"
```

### 3. Product Not Found saat Create Order

**Solusi:** Pastikan:
1. Product sudah dibuat di Catalogue Service
2. Product status adalah `ACTIVE`
3. Order Service dapat terhubung ke API Gateway (check URL di CatalogueClient)

```bash
# Test connection dari Order Service
curl http://localhost:8080/api/products/NSPD
```

### 4. Stock Jadi 0 setelah Pay (Expected: berkurang)

**Solusi:** Gunakan endpoint `/reduce-stock` bukan PATCH update biasa.

## 📁 Project Structure

```
Sertifikasi/
├── api-gateway/
│   ├── src/
│   ├── pom.xml
│   └── ...
├── catalogue-service/
│   ├── src/
│   │   ├── main/java/com/sertifikasi/catalogue/
│   │   │   ├── product/
│   │   │   │   ├── Product.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── ProductController.java
│   │   │   │   └── dto/
│   │   │   ├── exception/
│   │   │   └── config/
│   │   └── resources/
│   │       └── application.properties
│   ├── pom.xml
│   └── ...
├── order-service/
│   ├── src/
│   │   ├── main/java/com/sertifikasi/order/
│   │   │   ├── order/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── dto/
│   │   │   ├── orderitem/
│   │   │   ├── client/
│   │   │   │   └── CatalogueClient.java
│   │   │   ├── exception/
│   │   │   └── config/
│   │   └── resources/
│   │       └── application.properties
│   ├── pom.xml
│   └── ...
└── README.md
```

## 🔄 Flow Diagram

```
Client Request
    ↓
API Gateway (8080)
    ↓
    ├─→ /api/products/** → Catalogue Service (8001)
    └─→ /api/orders/** → Order Service (8002)
    
Order Payment Flow:
Create Order (PENDING)
    ↓
Call API Gateway /api/orders/{id}/pay
    ↓
Order Service hit Catalogue Service via API Gateway
    ↓
Reduce Product Stock
    ↓
Order Status → PAID
```

## 📚 Technology Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Database:** PostgreSQL
- **ORM:** Hibernate JPA
- **API Gateway:** Spring Cloud Gateway
- **Security:** Spring Security
- **Build Tool:** Maven
- **Testing:** JUnit 5, Mockito

## 📞 Support

Jika ada error atau pertanyaan:

1. Check service logs di terminal
2. Verify database connection
3. Check port availability
4. Review API documentation di file ini

## ✅ Checklist Menjalankan Project

- [ ] Java JDK 17+ installed
- [ ] PostgreSQL running dan database dibuat
- [ ] Git repository cloned
- [ ] Maven dependency downloaded (`mvn clean install`)
- [ ] API Gateway started (port 8080)
- [ ] Catalogue Service started (port 8001)
- [ ] Order Service started (port 8002)
- [ ] Test endpoints dengan Postman/curl
- [ ] Database tables created automatically (Hibernate ddl-auto=update)

**Selesai! 🎉**
