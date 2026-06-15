
## Project Structure

```
bookstore/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/bookstore/
    │   │   ├── BookstoreApplication.java          ← Main entry point
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java            ← Spring Security + JWT filter setup
    │   │   │   └── SwaggerConfig.java             ← OpenAPI/Swagger annotations
    │   │   ├── controller/
    │   │   │   ├── AuthController.java            ← POST /api/register, /api/login
    │   │   │   ├── BookController.java            ← GET/POST/PUT/DELETE /api/books
    │   │   │   └── OrderController.java           ← GET/POST/PUT /api/orders
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── BookRequest.java
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── OrderRequest.java
    │   │   │   │   └── OrderStatusRequest.java
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java           ← Generic wrapper for all responses
    │   │   │       ├── AuthResponse.java
    │   │   │       ├── BookResponse.java
    │   │   │       └── OrderResponse.java
    │   │   ├── entity/
    │   │   │   ├── Book.java
    │   │   │   ├── User.java
    │   │   │   ├── Order.java
    │   │   │   └── OrderItem.java
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── BadRequestException.java
    │   │   │   └── GlobalExceptionHandler.java    ← @RestControllerAdvice
    │   │   ├── repository/
    │   │   │   ├── BookRepository.java
    │   │   │   ├── UserRepository.java
    │   │   │   └── OrderRepository.java
    │   │   ├── security/
    │   │   │   ├── JwtUtils.java                  ← Token generation + validation
    │   │   │   ├── AuthTokenFilter.java           ← Intercepts every request
    │   │   │   └── UserDetailsServiceImpl.java
    │   │   └── service/
    │   │       ├── AuthService.java
    │   │       ├── BookService.java
    │   │       ├── OrderService.java
    │   │       └── impl/
    │   │           ├── AuthServiceImpl.java
    │   │           ├── BookServiceImpl.java
    │   │           └── OrderServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        ├── java/com/bookstore/
        │   ├── BookServiceTest.java               ← Unit tests (Mockito)
        │   └── AuthControllerTest.java            ← Integration tests (MockMvc + H2)
        └── resources/
            └── application.properties             ← H2 in-memory test DB
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |

---

## Setup & Run

### 1. Create MySQL Database
```sql
CREATE DATABASE bookstore_db;
```

### 2. Configure Database Credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build the Project
```bash
cd bookstore
mvn clean install
```

### 4. Run the Application
```bash
mvn spring-boot:run
```
The server starts at **http://localhost:8080**

### 5. Run Tests
```bash
mvn test
```
Tests use H2 in-memory database — no MySQL needed for testing.

---

## API Endpoints

### Authentication

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/register` | Public | Register new user |
| POST | `/api/login` | Public | Login, returns JWT |

**Register body:**
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "password": "secret123",
  "role": "CUSTOMER"
}
```

**Login body:**
```json
{
  "email": "alice@example.com",
  "password": "secret123"
}
```

### Books

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/books` | Public | List all books (paginated) |
| GET | `/api/books?search=clean` | Public | Search by title or author |
| GET | `/api/books?genre=Fiction` | Public | Filter by genre |
| GET | `/api/books/{id}` | Public | Get single book |
| POST | `/api/books` | Admin | Create a book |
| PUT | `/api/books/{id}` | Admin | Update a book |
| DELETE | `/api/books/{id}` | Admin | Delete a book |

**Pagination params:** `page`, `size`, `sortBy`, `sortDir`

### Orders

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/orders` | Admin | List all orders |
| GET | `/api/orders/my-orders` | Customer | My orders |
| GET | `/api/orders/{id}` | Auth | Get single order |
| POST | `/api/orders` | Customer | Place a new order |
| PUT | `/api/orders/{id}/status` | Admin | Update order status |

**Place order body:**
```json
{
  "items": [
    { "bookId": 1, "quantity": 2 },
    { "bookId": 3, "quantity": 1 }
  ]
}
```

**Update status body:**
```json
{
  "orderStatus": "SHIPPED",
  "paymentStatus": "PAID"
}
```

---

## Authentication

All protected endpoints require the JWT token in the `Authorization` header:
```
Authorization: Bearer <your_jwt_token>
```

---

## Swagger Documentation

Once running, visit:
```
http://localhost:8080/swagger-ui.html
```
Click **Authorize** → paste your JWT token to test protected endpoints.

---

## HTTP Status Codes Used

| Code | Meaning |
|------|---------|
| 200 | OK — success |
| 201 | Created — resource created |
| 400 | Bad Request — validation error / duplicate |
| 401 | Unauthorized — invalid/missing token |
| 403 | Forbidden — insufficient role |
| 404 | Not Found — resource doesn't exist |
| 500 | Internal Server Error |

---

## Response Format

All endpoints return a consistent wrapper:
```json
{
  "success": true,
  "message": "Books retrieved successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Order Status Flow

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
                              ↘ CANCELLED
```

## Payment Status Values
`PENDING` | `PAID` | `FAILED` | `REFUNDED`
