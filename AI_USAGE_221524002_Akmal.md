# AI_USAGE.md

Isi file ini jika menggunakan AI secara signifikan selama pengerjaan.

## Tool AI yang Digunakan

Contoh:

- ChatGPT
- GitHub Copilot (Claude Haiku 4.5)
- Google AI
- Lainnya:

## Bagian yang Dibantu AI

Tuliskan bagian mana saja yang dibantu AI.

1. Perbedaan Spring Boot monolith dan Spring Boot microservice, termasuk perbedaan struktur project-nya.
2. Perbedaan `group_id`, `artifact_id`, dan `package name` dalam Spring Initializr.
3. Dependency yang umum digunakan dalam pembuatan aplikasi web menggunakan Spring Boot.
4. Alasan penggunaan API Gateway dalam arsitektur microservices.
5. Pengganti konfigurasi `spring.cloud.gateway.routes` yang sudah deprecated pada Spring Cloud Gateway.
6. Cara mengubah pencarian data dari `findById` menjadi berdasarkan SKU/code barang.
7. Cara implementasi PATCH update menggunakan DTO.
8. Masalah penggunaan dependency Spring Security yang menghasilkan password dummy saat aplikasi dijalankan, tetapi ketika dites melalui Postman tetap mendapatkan response `401 Unauthorized`.
9. Penggunaan `throw exception`, tetapi message error yang ditampilkan terlalu panjang, sehingga perlu dibuat `GlobalExceptionHandler`.
10. Error duplikasi kolom `order_id` pada `Order.java` dan `OrderItem.java`, terutama pada relasi entity dan penggunaan cascade.
11. Error produk dengan kode tertentu tidak ditemukan, meskipun sudah menggunakan `findByCode`, serta solusi dengan menyesuaikan URL client agar mengarah ke API Gateway pada port `8080`.
12. Bug logic pada proses pengurangan stok produk, sehingga perlu dibuat endpoint khusus `reduce-stock` untuk mengurangi stok.
13. Pembuatan endpoint dan DTO untuk fitur `reduce-stock`.
14. Pembuatan struktur folder `product-service` dan `order-service` dengan menerapkan DTO pattern.
15. Implementasi `OrderStatus` enum.
16. Implementasi relasi `@OneToMany` dengan `CascadeType` pada order dan order item.
17. Pembuatan `CatalogueClient` untuk komunikasi antar-service atau inter-service communication.
18. Implementasi `ProductStatus` enum dengan default status `ACTIVE`.




## Prompt Penting yang Digunakan

Prompt penting yang saya perintahkan:
1. Apakah controller tepat seperti ini? Lalu service apa isinya? Bukankah lebih tepat menggunakan DTO? (Untuk cek controller yang saya buat)
2. Jika seperti ini yang dapat diupdate hanya stock saja dong? Bagaimana dengan name dan price? (Saya membuat endpoint patch produk, tapi hanya update stok saja tidak bisa update nama atau harga)
3. Sudah pakai exception tapi responnya 500 internal server error (Sudah throw exception tapi error code nya tidak sesuai message)
4. Modifikasi endpoint patch untuk status, hanya product dengan status ACTIVE yang dapat dibeli, default statusnya adalah ACTIVE, modifikasi semua hal yang berkaitan (Untuk menambahkan error handling hanya produk dengan status aktive yang dapat dibeli)
5. Saya create product dengan code BTGR dengan quantity 5, terus saya create order BTGR dengan quantity 1, terus saya pay, seharusnya kan stock jadi 4, saat saya cek stock BTGR jadi 1, ada yang salah ini (Untuk memperbaiki bug stock product)

## Modifikasi yang Dilakukan Sendiri

Jelaskan perubahan yang dilakukan setelah menerima jawaban AI.
0. Membuat 3 services
1. Menyesuaikan parameter yang digunakan di endpoint dari ID jadi code.
2. Menyesuaikan endpoint sesuai requirement (PATCH untuk partial update).
3. Menambahkan validasi agar hanya product ACTIVE yang bisa di-order.
4. Menambahkan cascade delete pada relasi OneToMany.
5. Mengonfigurasi API Gateway routes ke port yang sesuai.
6. Menguji semua endpoint menggunakan Postman.
7. Memperbaiki service URL dari 8001 ke 8080 (via API Gateway).
8. Membuat endpoint reduce-stock untuk fix bug pengurangan stock.
9. Menambahkan logika agar code setiap produk unqiue

## Bagian yang Sudah Dipahami

Tuliskan secara singkat apa yang sudah dipahami.

0. Saya jadi paham perbedaan monolith vs microservices dan keuntungan/kerugiannya
1. Saya jadi paham flow untuk lingkup setiap service
2. Saya jadi paham initialize springboot
3. Saya jadi paham fungsi api gateway pada microsevices
4. Saya jadi paham implementasi microservices itu bagaimana
5. Saya jadi paham fungsi repository di dalam service itu apa
6. Saya jadi paham fungsi constructor injection
7. Saya jadi paham pentingnya message error, exception

## Bagian yang Masih Membingungkan

Tuliskan jika masih ada bagian yang belum dipahami.

Contoh:

1. Untuk masalah library dan sintaks saya belum terlalu mendalami, jadi masih pakai AI, tapi flownya sih sudah cukup paham, keterkaitan antar file itu kemana mana nya.
2. Saya belum paham spring security sebenarnya, setelah dependency diinstal, saat saya mau create product atau order itu malah unauthorized, dan solusinya adalah permit.*, lalu apa fungsi security nya kalau permit all.
