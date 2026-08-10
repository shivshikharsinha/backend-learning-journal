# Day 6 - Controller Layer, REST APIs & Request Flow
**Date:** 06th August 2026
## 🎯 Goal
Connect the Controller layer with the existing Service and Repository layers and understand the HTTP request-response flow in Spring Boot.
### Topics
Controller → Service → Repository · `DispatcherServlet` · REST endpoints · HTTP methods · `@PathVariable` · `@RequestParam` · `@RequestBody` · Constructor DI · Jackson · Postman · HTTP errors
---
## 1. Controller Layer
The **Controller** is the HTTP/API layer. It receives requests, extracts request data, and delegates application work to the Service.
> **Controllers should not contain business logic.**

```text
Client → Controller → Service → Repository → Database
```

| Layer | Responsibility |
|---|---|
| Controller | HTTP/API handling |
| Service | Business logic and validation |
| Repository | Data access/persistence |
| Database | Persistent storage |

The Controller should not directly access the Repository.

## 2. HTTP Request Flow
```text
HTTP Request → Tomcat → DispatcherServlet → Controller → Service → Repository → Database
```
Response:
```text
Database → Repository → Service → Controller → Jackson → HTTP Response
```

- **Tomcat:** Web server/Servlet container that receives HTTP requests.
- **DispatcherServlet:** Central **Front Controller** of Spring MVC that finds the appropriate Controller method.

## 3. Constructor Dependency Injection
`ProductService` was injected into `ProductController`:

```java
private final ProductService productService;

public ProductController(ProductService productService) {
    this.productService = productService;
}
```

Instead of manually creating the Service:
```java
ProductService productService = new ProductService();
```
Spring's Dependency Injection container provides it.

`final` prevents reassignment. Constructor injection makes dependencies explicit and improves testability.

## 4. REST Endpoints
| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/products` | Get all products |
| `GET` | `/products/{id}` | Get product by ID |
| `POST` | `/products` | Create product |
| `DELETE` | `/products/{id}` | Delete product |

```text
Create → POST   Read → GET   Update → Not implemented   Delete → DELETE
```

## 5. GET All Products
```java
@GetMapping
public List<Product> getProducts() {
    return productService.getAllProducts();
}
```
```http
GET /products
```

Flow:
```text
GET /products → DispatcherServlet → ProductController
             → ProductService.getAllProducts()
             → ProductRepository.findAll() → HTTP Response
```

`@GetMapping` maps an HTTP GET request to a Controller method.

## 6. GET Product By ID
```java
@GetMapping("/{id}")
public Product getProductById(@PathVariable Long id) {
    return productService.getProductById(id);
}
```
```http
GET /products/1
```

`@PathVariable` extracts values from the URL path:
```text
/products/10 → id = 10
```

## 7. `@RequestParam`
`@RequestParam` extracts query parameters:
```text
/products?id=10
```
```java
@GetMapping
public Product getProduct(@RequestParam Long id) {
    return productService.getProductById(id);
}
```

| | `@PathVariable` | `@RequestParam` |
|---|---|---|
| Location | URL path | Query string |
| Example | `/products/10` | `/products?id=10` |
| Typical use | Identify resource | Filter/modify request |

## 8. `@RequestBody` and Jackson
POST data is usually sent in the request body:

```json
{"id":2,"name":"iPhone 17 Pro","price":130000}
```

```java
@PostMapping
public Product createProduct(@RequestBody Product product) {
    return productService.createProduct(product);
}
```

`@RequestBody` deserializes the body into a Java object. Jackson handles:
```text
JSON → Java Object
Java Object → JSON
```

## 9. POST `/products`
```http
POST /products
```
Body:
```json
{"id":2,"name":"iPhone 17 Pro","price":130000}
```

Flow:
```text
POST → DispatcherServlet → ProductController
     → @RequestBody/Jackson → Product
     → ProductService → ProductRepository.save() → Database
```

## 10. DELETE `/products/{id}`
```java
@DeleteMapping("/{id}")
public void deleteProductById(@PathVariable Long id) {
    productService.deleteProduct(id);
}
```
```http
DELETE /products/2
```

```text
DELETE /products/2 → @PathVariable(id=2)
                   → ProductService.deleteProduct(2)
                   → ProductRepository.deleteById(2)
```

The Controller delegates deletion; it does not perform database operations itself.

## 11. Why No Business Logic in Controller?
Avoid:
```java
@PostMapping
public Product createProduct(@RequestBody Product product) {
    // validation
    // business logic
    // database operation
    return ...;
}
```

Prefer:
```java
@PostMapping
public Product createProduct(@RequestBody Product product) {
    return productService.createProduct(product);
}
```

Responsibilities:
```text
Controller → HTTP/API concerns
Service    → Validation + Business Logic
Repository → Data Access
```

## 12. Testing with Postman
```http
GET    http://localhost:8080/products
GET    http://localhost:8080/products/1
POST   http://localhost:8080/products
DELETE http://localhost:8080/products/2
```

POST body:
```json
{"id":2,"name":"iPhone 17 Pro","price":130000}
```

## 13. Errors Observed
### 404 Not Found
While testing POST, `/products/` was initially used instead of `/products`, resulting in `404 Not Found`. This demonstrated that the requested URL must match the Controller mapping.

### 500 Internal Server Error
For duplicate Product IDs:
```java
throw new RuntimeException("Product ID already exists.");
```
Because it was not handled properly, the API returned `500 Internal Server Error`.

This led to:
```text
Custom Exceptions → Global Exception Handling → Proper HTTP Status Codes
```

## 💡 Key Takeaways
- Controller handles HTTP/API concerns.
- Service contains business logic and validation.
- Repository handles persistence.
- Controller should not directly access Repository.
- `DispatcherServlet` dispatches requests to Controller methods.
- Tomcat receives HTTP requests.
- `@GetMapping`, `@PostMapping`, `@DeleteMapping` map HTTP methods.
- `@PathVariable` extracts URL path values.
- `@RequestParam` extracts query parameters.
- `@RequestBody` converts JSON into a Java object.
- Jackson converts JSON ↔ Java objects.
- Constructor injection makes dependencies explicit and testable.
- Unhandled exceptions can result in HTTP `500`.

## 🧠 Mental Model
```text
HTTP
  ↓
Controller → "How do I handle HTTP?"
  ↓
Service → "What should the application do?"
  ↓
Repository → "How do I access the data?"
  ↓
Database
```

## 💼 Interview Takeaways
- **Controller:** Handles HTTP requests/responses and delegates to Service.
- **DispatcherServlet:** Spring MVC Front Controller that dispatches requests.
- **`@PathVariable`:** Extracts a value from the URL path.
- **`@RequestParam`:** Extracts a query parameter.
- **`@RequestBody`:** Deserializes the HTTP body into a Java object.
- **Jackson:** Converts JSON ↔ Java objects.
- **Constructor injection:** Makes dependencies explicit, testable, and compatible with `final`.

## 📝 Code Written Today
Created:
```text
ProductController
```
Implemented:
```text
GET /products
GET /products/{id}
POST /products
DELETE /products/{id}
```
Used:
```text
@GetMapping  @PostMapping  @DeleteMapping
@PathVariable  @RequestParam  @RequestBody
```

Learned:
```text
Tomcat · DispatcherServlet · Jackson · Constructor Injection
REST API Request Flow · Controller-Service-Repository Architecture
```

## 💭 Reflection
Today I connected the existing Service and Repository layers to the HTTP layer through the Controller.

The key lesson was:
```text
Controller → HTTP/API
Service    → Business Logic
Repository → Persistence
```

Testing with Postman made the request-response flow clearer. The `500 Internal Server Error` also showed why exceptions need to be translated into meaningful HTTP responses.

## 🚀 Next Goal: Exception Handling
- Custom exceptions and `RuntimeException`
- `ProductNotFoundException`
- `ProductAlreadyExistsException`
- `InvalidProductException`
- `@ControllerAdvice` and `@ExceptionHandler`
- `GlobalExceptionHandler`
- `ResponseEntity` and `ErrorResponse`
- HTTP `400`, `404` and `409`
- Request deserialization errors

### Connect the Dots
```text
HTTP Request → Tomcat → DispatcherServlet → ProductController
             → ProductService → ProductRepository → Database
```

Exception flow:
```text
Controller → Service → Exception → Global Exception Handler
           → ErrorResponse → HTTP Response
```

## 📌 Summary
Day 6 connected the **Service and Repository layers** to the external world through a **REST Controller**.

Core architecture:
```text
HTTP Request
 ↓
Tomcat
 ↓
DispatcherServlet
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Database
```

This establishes the basic request-response architecture of the Cartify Spring Boot application and prepares it for **centralized exception handling and proper API error responses**.
