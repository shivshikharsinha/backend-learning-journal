# Day 9 - Validation & Global Exception Handling

**Date:** 08th September 2026

## Overview

Today we completed **Sprint 5 – Validation & Exception Handling**.

The focus was on understanding how Spring Boot handles different types of errors, how exceptions are mapped to HTTP status codes, and why exception handling should be centralized using `GlobalExceptionHandler`.

---

## 1. Validation vs Business Validation

We established two different types of validation.

### Object/Input Validation

This checks whether the incoming data itself is valid.

Examples:

```java
@NotNull
private Long id;

@NotBlank
private String name;

@NotNull
@PositiveOrZero
private Double price;
```

These validations are handled by **Bean Validation** before the Service layer is executed.

For example:

```json
{
    "id": 1,
    "name": "",
    "price": -50
}
```

The `name` and `price` fields fail validation, resulting in:

```text
400 Bad Request
```

### Business Validation

Business validation checks whether the request is valid according to the application's current state or business rules.

Example:

```java
if (productRepository.findById(productId).isPresent()) {
    throw new ProductAlreadyExistsException(
            "Product ID already exists."
    );
}
```

Here, the request data can be completely valid, but the Product ID already exists.

Therefore:

```text
ProductAlreadyExistsException → 409 Conflict
```

### Important distinction

```text
Object/Input Validation
        ↓
Bean Validation
        ↓
400 Bad Request

Business Validation
        ↓
Service Layer
        ↓
Business Exception
        ↓
Appropriate HTTP status
```

---

## 2. `MethodArgumentNotValidException`

When `@Valid` detects Bean Validation failures, Spring throws:

```java
MethodArgumentNotValidException
```

Example:

```java
@PostMapping
public Product createProduct(
        @Valid @RequestBody Product product
) {
    return productService.createProduct(product);
}
```

If multiple fields are invalid, we can extract their errors using:

```java
ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error ->
                error.getField() + ": " + error.getDefaultMessage()
        )
        .collect(Collectors.joining("; "));
```

Example response:

```json
{
    "status": 400,
    "message": "id: must not be null; price: must be greater than or equal to 0"
}
```

The important point is that the Service does not need to manually repeat these object-validation checks.

---

## 3. `HttpMessageNotReadableException`

This occurs when Spring cannot properly read or parse the incoming request body.

For example, if JSON contains a value that cannot be converted to the expected Java type, Spring can throw:

```java
HttpMessageNotReadableException
```

We handle it as:

```text
400 Bad Request
```

This represents an invalid or unreadable request body.

---

## 4. `MethodArgumentTypeMismatchException`

We also learned about invalid path-variable or parameter types.

Controller:

```java
@GetMapping("/{id}")
public Product getProduct(@PathVariable Long id) {
    return productService.getProductById(id);
}
```

Request:

```text
GET /products/abc
```

Spring extracts `"abc"` from the URL and tries to convert it to the declared type:

```text
"abc" → Long
```

The conversion fails, resulting in:

```java
MethodArgumentTypeMismatchException
```

This happens while Spring is resolving the controller method arguments, so the controller method body is not executed.

We handle this as:

```text
400 Bad Request
```

Example handler:

```java
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ErrorResponse> handleTypeMismatchException(
        MethodArgumentTypeMismatchException ex) {

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid value for parameter: " + ex.getName()
            ));
}
```

---

## 5. Why Use `GlobalExceptionHandler`?

We discussed why exceptions should not be handled using repetitive `try-catch` blocks inside every Controller method.

Without centralized handling:

```text
Controller 1 → try/catch
Controller 2 → try/catch
Controller 3 → try/catch
Controller 4 → try/catch
```

This creates repetition and makes Controllers responsible for exception handling.

Instead:

```text
Controller
    ↓
Exception
    ↓
GlobalExceptionHandler
    ↓
HTTP Response
```

This provides:

- Centralized exception handling
- Cleaner Controllers
- Less repetitive code
- Clear separation of responsibilities
- Consistent API error responses

---

## 6. Specific vs Generic Exception Handling

We learned that Spring prefers a more specific exception handler when multiple handlers could match.

For example:

```java
@ExceptionHandler(ProductNotFoundException.class)
```

and:

```java
@ExceptionHandler(Exception.class)
```

If a `ProductNotFoundException` occurs, Spring uses the specific handler:

```text
ProductNotFoundException
        ↓
ProductNotFoundException handler
        ↓
404 Not Found
```

The generic `Exception.class` handler acts as a safety net for unexpected exceptions.

---

## 7. Generic Exception Handling

Unexpected programming errors can occur anywhere in the application.

Example:

```java
String name = null;
name.length();
```

This results in:

```java
NullPointerException
```

This is not a client/input error. It is an unexpected server-side failure.

Therefore:

```text
Unexpected Exception → 500 Internal Server Error
```

We added a generic handler:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(Exception ex) {

    // Log the actual exception here

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error"
            ));
}
```

### Important production principle

The actual exception should be logged internally, but the client should receive a safe generic message.

Do not expose:

- Stack traces
- Internal file paths
- Class names
- Database details
- Internal implementation details

The client should receive:

```json
{
    "status": 500,
    "message": "Internal Server Error"
}
```

---

## 8. HTTP Status Code Mental Model

The main status-code rules learned so far:

| Situation | Exception | HTTP Status |
|---|---|---:|
| Bean Validation fails | `MethodArgumentNotValidException` | 400 |
| Request body cannot be parsed/read | `HttpMessageNotReadableException` | 400 |
| Parameter cannot be converted to expected type | `MethodArgumentTypeMismatchException` | 400 |
| Product does not exist | `ProductNotFoundException` | 404 |
| Product ID already exists | `ProductAlreadyExistsException` | 409 |
| Unexpected application exception | Generic `Exception` | 500 |

### Simple rule

```text
400 → Client sent invalid input/request
404 → Requested resource does not exist
409 → Valid request conflicts with existing data/state
500 → Unexpected problem inside the server/application
```

---

## 9. Complete Request/Error Flow

The complete mental model is:

```text
HTTP Request
     ↓
Controller
     ↓
Request data / parameter processing
     ↓
Bean Validation / type conversion
     ↓
Service
     ↓
Repository
     ↓
Result
```

If an error occurs:

```text
Exception
     ↓
GlobalExceptionHandler
     ↓
Exception-to-HTTP-status mapping
     ↓
ErrorResponse
     ↓
Client
```

For unexpected exceptions:

```text
Unexpected Exception
     ↓
GlobalExceptionHandler
     ↓
Log actual exception internally
     ↓
Return safe generic response
     ↓
500 Internal Server Error
```

---

## 10. Sprint 5 Completion

### Sprint 5 – Validation & Exception Handling

**Status: COMPLETE**

Topics covered:

- Bean Validation
- `@Valid`
- `@NotNull`
- `@NotBlank`
- `@PositiveOrZero`
- Object/Input validation
- Business validation
- `MethodArgumentNotValidException`
- `HttpMessageNotReadableException`
- `MethodArgumentTypeMismatchException`
- Custom exceptions
- `@ExceptionHandler`
- `@ControllerAdvice`
- Centralized exception handling
- HTTP status codes: 400, 404, 409, 500
- Generic exception handling
- Safe error responses
- Separation of responsibilities between Controller, Service, and exception handling

---

## 11. Key Takeaways

### 1. Not every error belongs to the Service layer

Spring can detect errors before the Controller method executes, such as invalid parameter types and request-body parsing failures.

### 2. Bean Validation handles object/input validation

The Service should not unnecessarily repeat checks already handled by Bean Validation.

### 3. Business rules belong in the Service layer

Examples include checking whether a Product already exists.

### 4. Global exception handling keeps Controllers clean

Instead of repeating `try-catch` logic, exceptions can be mapped centrally.

### 5. HTTP status codes communicate the type of failure

```text
400 → Invalid request
404 → Resource not found
409 → Resource/state conflict
500 → Unexpected server error
```

### 6. Never expose internal server details to clients

Log detailed exceptions internally and return safe error messages externally.

---

## Next Step

According to the learning roadmap, the next major sprint is:

**Sprint 2 – MySQL Integration**

The goal will be to replace the current in-memory data storage with a real MySQL database and understand how Spring Boot interacts with persistent data.
