# Day 8 - Bean Validation & Global Validation Error Handling

**Date:** 02nd September 2026

---

## 🎯 Goal

Move object-level validation out of the Service layer and introduce Spring Boot's Bean Validation mechanism.

The main goals were:

- Understand Bean Validation.
- Use validation annotations on the `Product` object.
- Use `@Valid` in the Controller.
- Understand `MethodArgumentNotValidException`.
- Handle validation errors globally.
- Return meaningful validation messages.
- Separate Object Validation from Business Validation.
- Remove redundant custom validation exceptions.

---

## ✅ What I Learned

### 1. Bean Validation

Bean Validation allows validation rules to be defined directly on the model instead of manually checking every field inside the Service.

Previously, `ProductService` contained validation such as:

```java
if (productId == null) {
    throw new InvalidProductException("Product ID cannot be null.");
}

if (productName == null || productName.isBlank()) {
    throw new InvalidProductException("Product name cannot be blank.");
}

if (productPrice == null) {
    throw new InvalidProductException("Product price cannot be null.");
}

if (productPrice < 0) {
    throw new InvalidProductException("Product price cannot be negative.");
}
```

After introducing Bean Validation, these checks were moved to the `Product` class.

---

## 📦 2. Validation Dependency

Added the following dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

This enables Bean Validation support in the Spring Boot application.

---

## 🏷️ 3. Validation Annotations

### Product ID

```java
@NotNull
private Long id;
```

This ensures that the Product ID is provided.

---

### Product Name

```java
@NotBlank
private String name;
```

`@NotBlank` ensures that the name is:

- Not `null`
- Not empty
- Not only whitespace

Examples:

```text
null       ❌
""         ❌
"   "      ❌
"MacBook"  ✅
```

---

### Product Price

The price uses two validation annotations:

```java
@NotNull
@PositiveOrZero
private Double price;
```

`@NotNull` ensures that a price is provided.

`@PositiveOrZero` ensures that the price is greater than or equal to zero.

| Price | Result |
|---:|:---:|
| `null` | ❌ |
| `-500` | ❌ |
| `0` | ✅ |
| `50000` | ✅ |

---

## 🔍 4. `@Valid`

Adding validation annotations to the `Product` class is not enough.

The Controller must trigger validation using `@Valid`.

```java
@PostMapping
public Product createProduct(@Valid @RequestBody Product product) {
    return productService.createProduct(product);
}
```

The important part is:

```java
@Valid
```

This tells Spring to validate the `Product` object before passing it to the Service.

---

## 🔄 5. Validation Flow

A valid request follows:

```text
HTTP Request
     ↓
@RequestBody
     ↓
Product Object
     ↓
@Valid
     ↓
Bean Validation
     ↓
Validation Successful
     ↓
Controller
     ↓
Service
     ↓
Repository
```

If validation fails:

```text
HTTP Request
     ↓
@Valid
     ↓
Bean Validation
     ↓
MethodArgumentNotValidException
     ↓
GlobalExceptionHandler
     ↓
ErrorResponse
     ↓
400 Bad Request
```

---

## ⚠️ 6. `MethodArgumentNotValidException`

When Bean Validation fails for a request body, Spring throws:

```java
MethodArgumentNotValidException
```

This is a Spring-provided exception.

The correct import is:

```java
import org.springframework.web.bind.MethodArgumentNotValidException;
```

A custom exception with the same name should not be created.

---

## 🌎 7. Global Exception Handling

Validation errors are handled in `GlobalExceptionHandler` using:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
```

The response status is:

```text
400 Bad Request
```

because the client has provided invalid request data.

---

## 🔎 8. `BindingResult`

`MethodArgumentNotValidException` contains the validation results.

They can be accessed using:

```java
ex.getBindingResult()
```

Individual field errors can be retrieved using:

```java
ex.getBindingResult().getFieldErrors()
```

This returns a list of `FieldError` objects.

---

## 🧩 9. `FieldError`

Each `FieldError` represents an individual validation failure.

For example:

```text
field          → name
defaultMessage → must not be blank
```

The field name can be obtained using:

```java
error.getField()
```

The validation message can be obtained using:

```java
error.getDefaultMessage()
```

---

## 🌊 10. Processing Validation Errors with Streams

All validation errors are processed using a Stream:

```java
ex.getBindingResult()
        .getFieldErrors()
        .stream()
```

Each `FieldError` is converted into a readable String using `map()`:

```java
.map(error -> error.getField() + ": " + error.getDefaultMessage())
```

For example:

```text
name: must not be blank
```

---

## 🔗 11. Combining Multiple Errors

Multiple validation messages are combined using:

```java
Collectors.joining("; ")
```

The complete logic is:

```java
String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining("; "));
```

For example:

```text
name: must not be blank; price: must be greater than or equal to 0
```

This allows the API to return multiple validation errors in one response.

---

## 🛠️ 12. Final Validation Exception Handler

The final handler is:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {

    String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    message
            ));
}
```

---

## 🧪 13. Testing Validation

For example, if the Product ID is missing and the price is negative:

```json
{
    "name": "MacBook Pro",
    "price": -500
}
```

The API returns:

```json
{
    "status": 400,
    "message": "id: must not be null; price: must be greater than or equal to 0"
}
```

This is better than returning only:

```json
{
    "status": 400,
    "message": "Invalid Request Body"
}
```

because the response now tells the client exactly which fields failed validation.

---

## 🏗️ 14. Object Validation vs Business Validation

One of the most important concepts from this lesson was separating two types of validation.

### Object Validation

Object validation asks:

> Is this Product itself valid?

Examples:

```text
Is ID present?
Is name blank?
Is price present?
Is price negative?
```

These rules belong to Bean Validation.

```text
Product
   │
   ├── @NotNull
   ├── @NotBlank
   └── @PositiveOrZero
```

---

### Business Validation

Business validation asks:

> Is this operation allowed according to the application's business rules?

For example:

```java
if (productRepository.findById(productId).isPresent()) {
    throw new ProductAlreadyExistsException(
            "Product ID already exists."
    );
}
```

This belongs in the Service because the application needs to check existing data through the Repository.

---

## 🧹 15. Cleaning the Service Layer

Before Bean Validation, `createProduct()` contained several manual validation checks:

```java
if (productId == null) {
    throw new InvalidProductException("Product ID cannot be null.");
}

if (productName == null || productName.isBlank()) {
    throw new InvalidProductException("Product name cannot be blank.");
}

if (productPrice == null) {
    throw new InvalidProductException("Product price cannot be null.");
}

if (productPrice < 0) {
    throw new InvalidProductException("Product price cannot be negative.");
}
```

These checks became redundant after introducing Bean Validation.

The Service can now focus on business validation:

```java
public Product createProduct(Product product) {

    Long productId = product.getId();

    if (productRepository.findById(productId).isPresent()) {
        throw new ProductAlreadyExistsException(
                "Product ID already exists."
        );
    }

    return productRepository.save(product);
}
```

---

## 🗑️ 16. Removing `InvalidProductException`

The custom `InvalidProductException` was originally used for basic Product validation.

After introducing Bean Validation, it was no longer required for these object-level validation rules.

The validation flow is now:

```text
Bean Validation
      ↓
MethodArgumentNotValidException
      ↓
GlobalExceptionHandler
      ↓
ErrorResponse
```

Business-specific exceptions are still handled separately.

---

## 📊 17. Current Exception Handling

| Exception | HTTP Status | Responsibility |
|---|---:|---|
| `ProductNotFoundException` | 404 | Product does not exist |
| `ProductAlreadyExistsException` | 409 | Product ID already exists |
| `MethodArgumentNotValidException` | 400 | Bean Validation failed |
| `HttpMessageNotReadableException` | 400 | Invalid/unreadable request body |

---

## 🧠 18. Important Concepts

### `@NotNull`

Ensures a value is not `null`.

```java
@NotNull
private Long id;
```

### `@NotBlank`

Ensures a String is not `null`, empty, or whitespace-only.

```java
@NotBlank
private String name;
```

### `@PositiveOrZero`

Ensures a numeric value is greater than or equal to zero.

```java
@PositiveOrZero
private Double price;
```

### `@Valid`

Triggers validation on the incoming object.

```java
@Valid @RequestBody Product product
```

### `BindingResult`

Contains validation results.

```java
ex.getBindingResult()
```

### `FieldError`

Represents an individual validation failure.

```java
error.getField()
error.getDefaultMessage()
```

### `map()`

Transforms each Stream element.

```java
.map(error -> error.getField() + ": " + error.getDefaultMessage())
```

### `Collectors.joining()`

Combines multiple Strings into one String.

```java
.collect(Collectors.joining("; "))
```

---

## 💡 19. Key Takeaways

- Bean Validation allows validation rules to be declared on the model.
- `@Valid` activates validation for incoming request objects.
- `@NotNull` checks for null values.
- `@NotBlank` validates String values.
- `@PositiveOrZero` prevents negative prices.
- `MethodArgumentNotValidException` is provided by Spring.
- `BindingResult` contains validation results.
- `FieldError` represents individual validation failures.
- Streams can process multiple validation errors.
- `map()` converts validation errors into readable messages.
- `Collectors.joining()` combines multiple messages.
- Object validation belongs in Bean Validation.
- Business validation belongs in the Service.
- Duplicate validation should be removed from the Service.
- Custom exceptions should represent meaningful application or business conditions.

---

## ❌ 20. Common Mistakes

- Forgetting `@Valid` in the Controller.
- Adding validation annotations without triggering validation.
- Creating a custom `MethodArgumentNotValidException`.
- Returning `404` for invalid request data.
- Keeping duplicate object validation in the Service.
- Using `ex.getMessage()` when a clean field-level validation message is required.
- Returning only the first validation error.
- Moving business validation into Bean Validation.

---

## 💼 21. Interview Takeaways

- What is Bean Validation?
- What does `@Valid` do?
- What is the difference between `@NotNull` and `@NotBlank`?
- What does `@PositiveOrZero` do?
- What happens when `@Valid` fails?
- What is `MethodArgumentNotValidException`?
- What is `BindingResult`?
- What is a `FieldError`?
- How do you retrieve a field name from a `FieldError`?
- How do you retrieve its validation message?
- Why should object validation and business validation be separated?
- Why does checking whether a Product ID exists belong in the Service?
- Why was `InvalidProductException` removed?
- How can multiple validation errors be combined into one response?

---

## 📝 22. Code Changes

### Added

- `spring-boot-starter-validation`
- `@NotNull`
- `@NotBlank`
- `@PositiveOrZero`
- `@Valid`
- `MethodArgumentNotValidException` handling
- `BindingResult` processing
- `FieldError` processing
- Stream-based validation error formatting

### Modified

- `Product`
- `ProductController`
- `ProductService`
- `GlobalExceptionHandler`

### Removed

- Duplicate object validation from `ProductService`
- `InvalidProductException`

---

## 💭 23. Reflection

Today's lesson helped me understand where validation should live in a Spring Boot application.

Previously, the Service was responsible for checking whether every Product field was valid. After introducing Bean Validation, these rules can be declared directly on the Product object.

The most important concept was separating object validation from business validation.

Bean Validation can determine whether a Product contains valid values, but it cannot determine whether a Product ID already exists in the Repository. That check belongs to the Service.

I also learned how Spring exposes validation failures through `MethodArgumentNotValidException`, how `BindingResult` contains the individual errors, and how Java Streams can transform those errors into a clean API response.

---

## 🧩 24. Final Architecture

```text
                    HTTP Request
                         │
                         ▼
                 ProductController
                         │
                       @Valid
                         │
                         ▼
                  Bean Validation
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
          @NotNull    @NotBlank  @PositiveOrZero
              │          │          │
              └──────────┼──────────┘
                         │
                  Validation OK
                         │
                         ▼
                   ProductService
                         │
                  Business Rules
                         │
              Product ID exists?
                         │
                         ▼
                  ProductRepository
                         │
                         ▼
                       Save
```

Validation failure:

```text
Bean Validation
      │
      ▼
MethodArgumentNotValidException
      │
      ▼
GlobalExceptionHandler
      │
      ▼
ErrorResponse
      │
      ▼
400 Bad Request
```

---

## 🚀 Next Goal

- Continue improving exception handling.
- Review HTTP status codes.
- Improve API error-response design.
- Continue building the Product CRUD API.
- Learn more about Spring Boot request/response handling.
- Strengthen the separation between Controller, Service, Repository, and validation responsibilities.

---

## 🏁 Day 8 Summary

Day 8 moved basic Product validation from the Service layer to Spring Boot's Bean Validation mechanism.

The Product now declares its validation rules, the Controller activates validation using `@Valid`, and the Global Exception Handler converts validation failures into a consistent `ErrorResponse`.

The Service is now primarily responsible for business rules.

The resulting architecture provides a cleaner separation of responsibilities:

```text
Controller
    ↓
Bean Validation
    ↓
Global Exception Handler
    ↓
ErrorResponse

Valid Request
    ↓
Service
    ↓
Business Validation
    ↓
Repository
```
