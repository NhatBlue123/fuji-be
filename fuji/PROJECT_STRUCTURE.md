# 📁 Cấu trúc dự án Spring Boot - FUJI

## 🗂️ Cấu trúc thư mục

```
src/main/java/com/example/fuji/
│
├── 📂 config/              # Configuration files
│   ├── CorsConfig.java           # CORS configuration (cho phép Frontend gọi API)
│   └── SecurityConfig.java       # Security + JWT configuration
│
├── 📂 controller/          # API Controllers (Routes)
│   └── *Controller.java          # Xử lý HTTP requests, định nghĩa endpoints
│
├── 📂 dto/                 # Data Transfer Objects
│   ├── ApiResponse.java          # Standard API response wrapper
│   └── *DTO.java                 # Request/Response objects + Validation
│
├── 📂 entity/              # Database Models
│   └── *.java                    # JPA Entities (giống Schema trong Mongoose)
│
├── 📂 repository/          # Database Layer
│   └── *Repository.java          # Query database (extends JpaRepository)
│
├── 📂 service/             # Business Logic
│   └── *Service.java             # Xử lý logic nghiệp vụ
│
├── 📂 exception/           # Error Handling
│   ├── GlobalExceptionHandler.java    # Global exception handler
│   ├── ErrorResponse.java             # Error response format
│   ├── ResourceNotFoundException.java # 404 exception
│   ├── BadRequestException.java       # 400 exception
│   └── UnauthorizedException.java     # 401 exception
│
├── 📂 middleware/          # Filters & Interceptors
│   └── LoggingFilter.java        # Request/Response logging
│
├── 📂 security/            # Security components
│   └── (JWT filters, Auth providers...)
│
├── 📂 util/                # Helper utilities
│   ├── DateUtil.java             # Date/Time helpers
│   └── StringUtil.java           # String helpers
│
└── FujiApplication.java    # Main entry point

src/main/resources/
├── application.properties  # Configuration file (giống .env)
└── static/                # Static files (images, css, js...)
```

---

## 🔄 Request Flow

```
Client Request
    ↓
Filter (LoggingFilter)
    ↓
Security (JWT validation)
    ↓
Controller (@RestController)
    ↓
DTO Validation (@Valid)
    ↓
Service (@Service)
    ↓
Repository (JpaRepository)
    ↓
Database (MySQL)
    ↓
Response (ApiResponse wrapper)
```

---

## 📝 Quy tắc đặt tên

### Entity (Model)
```java
User.java
Post.java
Product.java
```

### Repository
```java
UserRepository.java
PostRepository.java
ProductRepository.java
```

### Service
```java
UserService.java
PostService.java
ProductService.java
```

### Controller
```java
UserController.java
PostController.java
ProductController.java
```

### DTO
```java
UserDTO.java              // Request DTO
UserResponseDTO.java      // Response DTO
CreateUserDTO.java        // Specific request
UpdateUserDTO.java        // Specific request
```

---

## 🎯 Cách tạo một feature mới (Ví dụ: User CRUD)

### 1️⃣ Tạo Entity
```java
// entity/User.java
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
}
```

### 2️⃣ Tạo Repository
```java
// repository/UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

### 3️⃣ Tạo DTO
```java
// dto/UserDTO.java
@Data
public class UserDTO {
    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank
    private String name;
}
```

### 4️⃣ Tạo Service
```java
// service/UserService.java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(UserDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        return userRepository.save(user);
    }
}
```

### 5️⃣ Tạo Controller
```java
// controller/UserController.java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserDTO dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.ok(ApiResponse.success("User created", user));
    }
}
```

---

## 🔧 Configuration Files

### application.properties
- Database connection (MySQL)
- Mail server (SMTP)
- File upload limits
- Logging levels
- JWT secret (khi implement)
- Redis config (khi dùng)
- Cloudinary config (khi dùng)

### CorsConfig.java
- Cho phép Frontend (Next.js/React) gọi API
- Config allowed origins, methods, headers

### SecurityConfig.java
- Disable CSRF (vì dùng JWT)
- Config public/protected routes
- Password encoder (BCrypt)

---

## 🚀 Các annotations quan trọng

### Entity
- `@Entity` - Đánh dấu class là table
- `@Table(name = "...")` - Tên table
- `@Id` - Primary key
- `@GeneratedValue` - Auto increment
- `@Column` - Config column
- `@OneToMany`, `@ManyToOne` - Relationships

### Repository
- `@Repository` - Đánh dấu Repository layer
- Extends `JpaRepository<Entity, ID>`

### Service
- `@Service` - Đánh dấu Service layer
- `@Transactional` - Transaction management

### Controller
- `@RestController` - Controller trả về JSON
- `@RequestMapping("/api/...")` - Base URL
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` - HTTP methods
- `@PathVariable` - Lấy param từ URL
- `@RequestParam` - Lấy query param
- `@RequestBody` - Lấy data từ body
- `@Valid` - Validate DTO

### DTO Validation
- `@NotNull` - Không được null
- `@NotBlank` - Không được empty
- `@Email` - Phải là email
- `@Size(min, max)` - Độ dài
- `@Min`, `@Max` - Giá trị số

### Config
- `@Configuration` - Config class
- `@Bean` - Tạo bean
- `@Component` - Component class

### Lombok
- `@Data` - Tự tạo getter/setter/toString
- `@RequiredArgsConstructor` - Tự tạo constructor
- `@Builder` - Builder pattern
- `@Slf4j` - Logger

---

## 📦 Dependencies quan trọng (pom.xml)

```xml
<!-- Spring Boot Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

---

## 🎯 Best Practices

1. **Luôn dùng DTO** - Không trả về Entity trực tiếp
2. **Validate input** - Dùng `@Valid` + DTO validation
3. **Exception handling** - Dùng custom exceptions
4. **Logging** - Log requests/responses
5. **Transaction** - Dùng `@Transactional` cho operations quan trọng
6. **API Response** - Dùng `ApiResponse` wrapper
7. **Security** - Protect routes với JWT
8. **CORS** - Config đúng origins

---

## 🔍 Testing API

### Swagger UI
```
http://localhost:8181/swagger-ui.html
```

### Postman
```
GET    http://localhost:8181/api/users
POST   http://localhost:8181/api/users
GET    http://localhost:8181/api/users/{id}
PUT    http://localhost:8181/api/users/{id}
DELETE http://localhost:8181/api/users/{id}
```

---

## 📚 So sánh với Node.js

| Node.js (Express) | Spring Boot |
|-------------------|-------------|
| `app.use(cors())` | `CorsConfig.java` |
| `app.use(middleware)` | `Filter` / `Interceptor` |
| `router.get('/users')` | `@GetMapping("/users")` |
| `User.findById()` | `userRepository.findById()` |
| `bcrypt.hash()` | `passwordEncoder.encode()` |
| `multer` | `MultipartFile` |
| `.env` | `application.properties` |
| `try-catch` | `@ExceptionHandler` |

---

## 🚀 Chạy ứng dụng

```bash
# Build project
./mvnw clean install

# Run application
./mvnw spring-boot:run

# Access API
http://localhost:8181/api/...
```

---

Chúc bạn code vui! 🎉
