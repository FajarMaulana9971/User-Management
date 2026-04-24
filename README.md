# User Management API

Sistem RESTful API manajemen **User dan Hobi** dengan standar enterprise — mendukung audit history, caching terdistribusi, migrasi database terkontrol, serta pengolahan media (profile picture).

---

## Tech Stack

| Layer | Teknologi |
|---|---|
| Framework | Spring Boot 3.5.x |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| Auditing | Hibernate Envers |
| Caching & Blacklist | Redis (Lettuce) |
| Security | Spring Security + JWT (JJWT 0.12.x) |
| Media | Spring Multipart + Thumbnailator |
| Mapper | MapStruct 1.5 |
| Dokumentasi | SpringDoc OpenAPI (Swagger UI) |

---

## Fitur Utama

### A. Database Audit Trail (Hibernate Envers)
- Setiap INSERT, UPDATE, DELETE pada entitas `User` dan `Hobby` dicatat otomatis ke tabel `_AUD`
- Endpoint `GET /api/v1/users/{id}/history` menampilkan riwayat lengkap: siapa yang mengubah, kapan, dan snapshot data pada setiap revisi

### B. Media Processing (Profile Picture)
- Upload foto profil dengan validasi format (`.jpg`, `.png`) dan ukuran maksimal 2MB
- Image compression otomatis sebelum disimpan menggunakan Thumbnailator
- Storage abstraction layer — mudah dipindah dari Local Storage ke AWS S3 tanpa mengubah business logic

### C. Redis Implementation
- **Distributed Caching**: list dan detail user/hobby di-cache di Redis dengan TTL 5 menit; `@CacheEvict` menjaga konsistensi saat data berubah
- **JWT Blacklisting**: saat logout, token masuk Redis blacklist dengan TTL otomatis mengikuti waktu expired token

### D. Database Migration (Flyway)
- Seluruh skema tabel — termasuk tabel audit Envers — didefinisikan eksplisit via script Flyway
- `ddl-auto=validate` (bukan `update`)

### E. Security & Transactional
- **RBAC**: role `ADMIN` (full access) dan `USER` (self access only)
- **Atomicity**: simpan data user + hobi + upload foto dalam satu transaksi — jika upload gagal, data user tidak tersimpan di DB

### F. Unit Testing
- Unit test untuk `UserService`, `HobbyService`, dan `JwtService` menggunakan JUnit 5 + Mockito
- Test menggunakan H2 in-memory database (tanpa koneksi ke PostgreSQL nyata)

---

## Struktur Proyek

```
src/
├── main/
│   ├── java/com/unictive/usermanagement/
│   │   ├── configs/          # Security, Redis, App properties
│   │   ├── controllers/      # REST controllers
│   │   ├── dto/
│   │   │   ├── requests/     # Request DTOs
│   │   │   └── responses/    # Response DTOs + base wrappers
│   │   ├── entities/         # JPA entities + BaseEntity + CustomRevisionEntity
│   │   ├── enums/            # RoleName
│   │   ├── exceptions/       # Custom exceptions + GlobalExceptionHandler
│   │   ├── mappers/          # MapStruct mappers
│   │   ├── repositories/     # Spring Data JPA repositories
│   │   ├── security/         # JwtService, JwtAuthenticationFilter, JwtBlacklistService
│   │   └── services/
│   │       ├── implementations/
│   │       └── interfaces/
│   └── resources/
│       ├── db/migration/     # Flyway scripts (V1, V2, V3)
│       └── application.properties
└── test/
    └── java/com/unictive/usermanagement/services/
        ├── HobbyServiceTest.java
        ├── JwtServiceTest.java
        └── UserServiceTest.java
```

---

## Prasyarat

Pastikan service berikut sudah berjalan sebelum menjalankan aplikasi:

| Service | Versi | Default Port |
|---|---|---|
| JDK | 21+ | — |
| PostgreSQL | 15+ | 5432 |
| Redis | 7+ | 6379 |
| Maven | 3.9+ | — |

---

## Cara Menjalankan

### 1. Siapkan PostgreSQL

Buat database dan user:

```sql
-- Masuk sebagai superuser postgres
psql -U postgres

-- Buat database
CREATE DATABASE unictive_test;

-- (Opsional) Buat user khusus
CREATE USER unictive_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE unictive_test TO unictive_user;
```

> Flyway akan otomatis membuat seluruh tabel saat aplikasi pertama kali dijalankan.

### 2. Siapkan Redis

**Menggunakan Docker (recommended):**

```bash
docker run -d \
  --name redis-unictive \
  -p 6379:6379 \
  redis:7-alpine
```

**Atau install langsung di Windows (WSL/native):**

```bash
# Ubuntu/WSL
sudo apt install redis-server
sudo service redis-server start

# Verifikasi
redis-cli ping   # harus balas: PONG
```

**Dengan password (opsional):**

```bash
docker run -d \
  --name redis-unictive \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --requirepass your_redis_password
```

### 3. Konfigurasi Environment

Edit `src/main/resources/application.properties` atau set environment variable:

```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/unictive_test
DB_USERNAME=postgres
DB_PASSWORD=toor

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=          # kosongkan jika tidak pakai password

# JWT Secret (min 32 karakter, base64)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Upload directory
UPLOAD_DIR=uploads
```

Atau export sebagai environment variable di terminal:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/unictive_test
export DB_USERNAME=postgres
export DB_PASSWORD=toor
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 4. Jalankan Aplikasi

```bash
# Clone project
git clone <repository-url>
cd Spring-Postgre-Base-Project

# Build dan jalankan
./mvnw spring-boot:run

# Atau build JAR terlebih dahulu
./mvnw clean package -DskipTests
java -jar target/user-management-1.0.0.jar
```

Aplikasi akan berjalan di `http://localhost:8080`

### 5. Verifikasi

```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
# Buka browser: http://localhost:8080/swagger-ui.html
```

---

## Akun Default (Dummy)

Akun ini dibuat otomatis via Flyway seed migration (V3):

| Field | Value |
|---|---|
| Username | `superadmin` |
| Password | `admin` |
| Role | `ADMIN` |
| Email | `superadmin@unictive.com` |

> Password disimpan dalam bentuk BCrypt hash di database.

---

## Panduan Testing via Swagger UI

Buka `http://localhost:8080/swagger-ui.html`

### Step 1 — Login

```
POST /api/v1/auth/login
```

```json
{
  "username": "superadmin",
  "password": "admin"
}
```

Salin `accessToken` dari response.

### Step 2 — Authorize di Swagger

Klik tombol **Authorize** (kanan atas) → masukkan:

```
Bearer <accessToken>
```

### Step 3 — Jelajahi endpoint

Semua endpoint kini bisa diakses sesuai role.

---

## API Endpoints

### Authentication

| Method | Endpoint | Auth | Deskripsi |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Public | Login, terima JWT token |
| POST | `/api/v1/auth/register` | Public | Register akun baru |
| POST | `/api/v1/auth/logout` | Bearer | Logout, token masuk blacklist |
| POST | `/api/v1/auth/refresh` | — | Refresh access token |

### Users

| Method | Endpoint | Auth | Deskripsi |
|---|---|---|---|
| GET | `/api/v1/users` | ADMIN / USER | List semua user (paginated) |
| GET | `/api/v1/users/{id}` | ADMIN / Owner | Detail user |
| POST | `/api/v1/users` | ADMIN | Buat user baru (multipart) |
| PUT | `/api/v1/users/{id}` | ADMIN / Owner | Update data user |
| PATCH | `/api/v1/users/{id}/profile-picture` | ADMIN / Owner | Upload foto profil |
| DELETE | `/api/v1/users/{id}` | ADMIN | Hapus user |
| GET | `/api/v1/users/{id}/history` | ADMIN | Riwayat audit user |

### Hobbies

| Method | Endpoint | Auth | Deskripsi |
|---|---|---|---|
| GET | `/api/v1/hobbies` | ADMIN / USER | List semua hobby (paginated) |
| GET | `/api/v1/hobbies/{id}` | ADMIN / USER | Detail hobby |
| POST | `/api/v1/hobbies` | ADMIN | Buat hobby baru |
| PUT | `/api/v1/hobbies/{id}` | ADMIN | Update hobby |
| DELETE | `/api/v1/hobbies/{id}` | ADMIN | Hapus hobby |

---

## Contoh Request

### Buat User Baru (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token>" \
  -F 'user={"username":"johndoe","email":"john@example.com","password":"secret123","fullName":"John Doe","hobbyIds":[1,2]};type=application/json' \
  -F 'profilePicture=@photo.jpg;type=image/jpeg'
```

> **Penting:** tambahkan `;type=application/json` pada part `user` agar tidak dikirim sebagai `application/octet-stream`.

### Lihat Audit History

```bash
curl -X GET http://localhost:8080/api/v1/users/1/history \
  -H "Authorization: Bearer <token>"
```

---

## Menjalankan Unit Test

```bash
# Jalankan semua test
./mvnw test

# Jalankan test spesifik
./mvnw test -Dtest=UserServiceTest
./mvnw test -Dtest=HobbyServiceTest
./mvnw test -Dtest=JwtServiceTest
```

Test menggunakan H2 in-memory — tidak memerlukan PostgreSQL atau Redis yang berjalan.

---

## Database Schema

```
roles           → id, name, created_at, updated_at, created_by, updated_by
users           → id, username, email, password, full_name, profile_picture,
                  is_active, role_id, created_at, updated_at, created_by, updated_by
hobbies         → id, name, description, created_at, updated_at, created_by, updated_by
user_hobbies    → user_id, hobby_id
revinfo         → rev, revtstmp, changed_by
users_aud       → id, rev, revtype, (+ semua kolom users)
hobbies_aud     → id, rev, revtype, (+ semua kolom hobbies)
user_hobbies_aud → user_id, hobby_id, rev, revtype
```

---

## Troubleshooting

**`Application failed to start` — schema validation error**

Flyway belum dijalankan atau schema tidak sinkron. Coba:
```bash
# Reset database (hati-hati: menghapus semua data)
psql -U postgres -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;" unictive_test
./mvnw spring-boot:run
```

**`Connection refused` — Redis tidak berjalan**

```bash
# Cek status Redis
redis-cli ping

# Atau jalankan via Docker
docker start redis-unictive
```

**`Content-Type 'application/octet-stream' is not supported`**

Pastikan part `user` dikirim dengan content type yang benar:
```bash
-F 'user={...};type=application/json'   # curl
```
Di Postman: klik titik tiga (...) pada field `user` → set Content-Type ke `application/json`.

**Port 6379 sudah dipakai**

```bash
# Cek proses yang menggunakan port
netstat -ano | findstr :6379         # Windows
lsof -i :6379                        # Linux/Mac

# Ganti port Redis di application.properties
spring.data.redis.port=6380
```
