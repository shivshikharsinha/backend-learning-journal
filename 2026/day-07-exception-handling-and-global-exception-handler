# Day 7 - Exception Handling & Global Error Handling

**Date:** 10th August 2026

---

## 🎯 Goal

Understand how exceptions flow through a Spring Boot application, replace generic exceptions with meaningful custom exceptions, and create a centralized mechanism for returning proper HTTP error responses.

---

## ✅ What I Learned

### Custom Exceptions

Instead of using generic exceptions such as:

```java
throw new RuntimeException("Product ID already exists.");
```

we created specific exceptions based on the actual problem:

- `ProductNotFoundException`
- `ProductAlreadyExistsException`
- `InvalidProductException`

This makes the application easier to understand, debug, and maintain.

---

### RuntimeException

Our custom exceptions extend `RuntimeException`:

```java
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}
```

Similarly:

```java
public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}
```

and:

```java
public class InvalidProductException extends RuntimeException {

    public InvalidProductException(String message) {
        super(message);
    }
}
```

These are unchecked exceptions, so we don't need to explicitly declare them using `throws` in every method.

---

### Why Specific Exceptions?

A generic exception:

```java
throw new RuntimeException("Product not found.");
```

does not clearly communicate what type of application error occurred.

A specific exception:

```java
throw new ProductNotFoundException("Product not found.");
```

makes the intention clear.

The exception itself now communicates the problem.

---

### Global Exception Handling

Instead of putting `try-catch` blocks inside every Controller method, we created a centralized exception handler.

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    ...
}
```

The basic flow is:

```text
Controller
    ↓
Service
    ↓
Exception
    ↓
GlobalExceptionHandler
    ↓
HTTP Response
```

This keeps exception handling separate from the Controller's normal request-handling logic.

---

### `@ControllerAdvice`

`@ControllerAdvice` allows us to define exception handling logic that can be applied across Controllers.

Instead of:

```java
try {
    ...
} catch (...) {
    ...
}
```

inside every Controller method, exceptions can be handled centrally.

---

### `@ExceptionHandler`

Used `@ExceptionHandler` to tell Spring which method should handle a particular exception.

Example:

```java
@ExceptionHandler(InvalidProductException.class)
public ResponseEntity<ErrorResponse> handleInvalidProductException(
        InvalidProductException ex) {

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, ex.getMessage()));
}
```

The exception type specified in:

```java
@ExceptionHandler(InvalidProductException.class)
```

determines which exception this method handles.

---

### ResponseEntity

Used:

```java
ResponseEntity<ErrorResponse>
```

to represent the HTTP response.

`ResponseEntity` allows us to control:

- HTTP status
- Headers
- Response body

For example:

```java
return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, ex.getMessage()));
```

---

### ErrorResponse

Created an `ErrorResponse` DTO to provide a consistent structure for API errors.

Example:

```json
{
    "status": 400,
    "message": "Product price cannot be null."
}
```

Instead of returning raw Java exception information, the API can return a clean and structured response.

---

## 🌐 HTTP Status Codes

We mapped different application situations to appropriate HTTP status codes.

| Situation | Exception | Status |
|---|---|---:|
| Invalid Product | `InvalidProductException` | 400 |
| Product not found | `ProductNotFoundException` | 404 |
| Product already exists | `ProductAlreadyExistsException` | 409 |
| Invalid request body | `HttpMessageNotReadableException` | 400 |

---

### 400 Bad Request

Used when the request contains invalid product data.

Examples:

- Product is `null`
- Product ID is `null`
- Product name is blank
- Product price is `null`
- Product price is negative

Example:

```text
InvalidProductException
        ↓
400 Bad Request
```

---

### 404 Not Found

Used when the requested Product does not exist.

Example:

```text
GET /products/999
```

If Product `999` doesn't exist:

```text
ProductNotFoundException
        ↓
404 Not Found
```

---

### 409 Conflict

Used when the request conflicts with the current state of the application.

For example, when creating a Product with an ID that already exists:

```text
ProductAlreadyExistsException
        ↓
409 Conflict
```

A duplicate Product ID is better represented as a conflict rather than a generic bad request because the request itself can be structurally valid.

---

## 🔄 Exception Flow

The complete exception flow is:

```text
HTTP Request
      ↓
Controller
      ↓
Service
      ↓
Exception
      ↓
GlobalExceptionHandler
      ↓
ErrorResponse
      ↓
HTTP Response
```

For example:

```text
POST /products
      ↓
ProductController
      ↓
ProductService
      ↓
ProductAlreadyExistsException
      ↓
GlobalExceptionHandler
      ↓
409 Conflict
      ↓
ErrorResponse
```

---

## ⚠️ Jackson Deserialization Errors

We discovered that not every `400 Bad Request` originates from the Service.

For example:

```json
{
    "id": "3oloas",
    "name": "iPhone 17 Pro",
    "price": 130000
}
```

Our Product expects:

```java
private Long id;
```

Jackson tries to convert:

```text
"3oloas" → Long
```

which is impossible.

Therefore, the request fails during JSON deserialization.

The Service is never reached.

The flow becomes:

```text
JSON Request
     ↓
Jackson
     ↓
Deserialization fails
     ↓
HttpMessageNotReadableException
     ↓
400 Bad Request
```

---

## `HttpMessageNotReadableException`

We added a handler for invalid request bodies:

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<ErrorResponse> handleMessageNotReadable(
        HttpMessageNotReadableException ex) {

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid request body."
            ));
}
```

Now an invalid request such as:

```json
{
    "id": "3oloas",
    "name": "iPhone 17 Pro",
    "price": 130000
}
```

can return a clean response:

```json
{
    "status": 400,
    "message": "Invalid request body."
}
```

instead of exposing the long technical Jackson error.

---

## 🧩 `double` vs `Double`

While handling a missing Product price, we discovered an important Java concept.

Initially:

```java
private double price;
```

A primitive `double` cannot contain `null`.

Therefore, this is not possible:

```java
double price = null;
```

We changed it to:

```java
private Double price;
```

`Double` is a wrapper class and can represent `null`.

Now we can distinguish:

```text
price = 0
```

from:

```text
price = null
```

---

### Updating the Product Class

The change needed to be consistent.

Field:

```java
private Double price;
```

Constructor:

```java
public Product(Long id, String name, Double price) {
    this.id = id;
    this.name = name;
    this.price = price;
}
```

Getter:

```java
public Double getPrice() {
    return price;
}
```

Service:

```java
Double productPrice = product.getPrice();
```

Now this check is valid:

```java
if (productPrice == null) {
    throw new InvalidProductException(
            "Product price cannot be null."
    );
}
```

---

### Why the Getter Also Needed to Change

Changing only:

```java
private Double price;
```

was not enough.

If the getter remained:

```java
public double getPrice()
```

Java would unbox the `Double` into a primitive `double`.

Therefore, the getter also needs to return:

```java
public Double getPrice()
```

The type should remain consistent throughout the chain:

```text
Product field
     ↓
Double
     ↓
Constructor
     ↓
Double
     ↓
Getter
     ↓
Double
     ↓
Service
     ↓
Double
```

---

## 16. Different Error Points

One of the most important concepts from today was understanding that errors can happen at different stages.

```text
HTTP Request
      ↓
Jackson
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

For example:

### Invalid JSON type

```text
JSON
 ↓
Jackson ❌
 ↓
HttpMessageNotReadableException
```

### Invalid Product

```text
JSON
 ↓
Jackson
 ↓
Product
 ↓
Service ❌
 ↓
InvalidProductException
```

### Product doesn't exist

```text
JSON
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
ProductNotFoundException
```

This helped clarify that not every error belongs in the Service layer.

---

## 💡 Key Takeaways

- Generic exceptions are less meaningful than specific application exceptions.
- Custom exceptions make error handling clearer.
- `RuntimeException` is useful for our application exceptions.
- `@ControllerAdvice` provides centralized exception handling.
- `@ExceptionHandler` maps exceptions to handler methods.
- `ResponseEntity` allows control over status, headers and body.
- `ErrorResponse` provides a consistent API error structure.
- `400` represents an invalid request.
- `404` represents a resource that was not found.
- `409` represents a conflict with the current resource state.
- Jackson can reject invalid JSON before the Controller method executes.
- `HttpMessageNotReadableException` can be handled globally.
- `Double` can represent `null`, while primitive `double` cannot.
- Field, constructor, getter and Service types should remain consistent.
- Not every error should be handled in the Service layer.

---

## 🧠 Mental Model

```text
HTTP Request

      ↓

Jackson / Request Parsing

      ↓

Controller

      ↓

Service

      ↓

Repository

      ↓

Database
```

If something goes wrong:

```text
Exception
    ↓
GlobalExceptionHandler
    ↓
@ExceptionHandler
    ↓
ErrorResponse
    ↓
HTTP Response
```

Different problems can occur at different points:

```text
Jackson
   → Invalid request body

Service
   → Invalid Product
   → Product already exists
   → Product not found

Repository / Database
   → Persistence-related errors
```

---

## ⚙️ Internal Working

### Invalid Product

```text
Request
   ↓
Jackson
   ↓
Product Object
   ↓
ProductService
   ↓
Validation fails
   ↓
InvalidProductException
   ↓
GlobalExceptionHandler
   ↓
400 Bad Request
   ↓
ErrorResponse
```

### Product Not Found

```text
GET /products/999
        ↓
ProductController
        ↓
ProductService
        ↓
ProductRepository
        ↓
ProductNotFoundException
        ↓
GlobalExceptionHandler
        ↓
404 Not Found
```

### Duplicate Product

```text
POST /products
        ↓
ProductController
        ↓
ProductService
        ↓
Product ID already exists
        ↓
ProductAlreadyExistsException
        ↓
GlobalExceptionHandler
        ↓
409 Conflict
```

### Invalid JSON

```text
POST /products
        ↓
JSON
        ↓
Jackson
        ↓
Deserialization fails
        ↓
HttpMessageNotReadableException
        ↓
GlobalExceptionHandler
        ↓
400 Bad Request
```

---

## ❌ Common Mistakes

- Using generic `RuntimeException` for every application error.
- Handling every exception directly inside the Controller.
- Returning `500` when a more appropriate HTTP status exists.
- Exposing raw technical exception messages to API clients.
- Assuming every `400` error comes from the Service.
- Forgetting that Jackson can fail before the Controller method executes.
- Using primitive `double` when the field needs to support `null`.
- Changing `Double` only in the field while leaving the getter as `double`.
- Treating duplicate resources as a generic bad request instead of considering `409 Conflict`.

---

## 💼 Interview Takeaways

- What is the difference between checked and unchecked exceptions?
- Why create custom exceptions?
- Why extend `RuntimeException`?
- What is `@ControllerAdvice`?
- What is `@ExceptionHandler`?
- What is `ResponseEntity`?
- Why use an `ErrorResponse` DTO?
- What is the difference between `400`, `404`, and `409`?
- What is `HttpMessageNotReadableException`?
- When can Jackson throw an exception?
- Does every `400` error originate from the Service?
- What is the difference between `double` and `Double`?
- Why can `Double` contain `null` but `double` cannot?

---

## 📝 Code Written Today

Created:

```text
ProductNotFoundException
ProductAlreadyExistsException
InvalidProductException
GlobalExceptionHandler
ErrorResponse
```

Used:

```text
@ControllerAdvice
@ExceptionHandler
ResponseEntity
HttpStatus
```

Handled:

```text
400 Bad Request
404 Not Found
409 Conflict
HttpMessageNotReadableException
```

Updated:

```text
double → Double
```

in the Product model and related constructor, getter, and Service code.

---

## 💭 Reflection

Today's lesson helped me understand that throwing an exception and returning a proper API error response are two different things.

The Service can identify that something went wrong, but the application still needs a mechanism to translate that failure into an appropriate HTTP response.

The main flow I understood today is:

```text
Service
   ↓
Exception
   ↓
GlobalExceptionHandler
   ↓
HTTP Status + ErrorResponse
```

I also learned that errors can happen before the Service is reached. Jackson can reject an invalid request body during deserialization, which is why `HttpMessageNotReadableException` needs to be handled separately.

The `double` vs `Double` issue also helped reinforce the difference between Java primitives and wrapper classes.

---

## 🚀 Next Goal

- Learn Bean Validation.
- Learn `@Valid`.
- Learn `@NotNull`.
- Learn `@NotBlank`.
- Learn `@PositiveOrZero`.
- Move basic object validation from manual Service checks to validation annotations.
- Understand the difference between Bean Validation and business validation.
- Keep business rules inside the Service layer.

---

## 🧩 Connect the Dots

```text
Spring Boot

      ↓

HTTP Request

      ↓

Tomcat

      ↓

DispatcherServlet

      ↓

Controller

      ↓

Service
  │
  ├── Object Validation
  ├── Business Validation
  └── Business Logic
      ↓

Repository

      ↓

Database
```

Exception flow:

```text
Exception

      ↓

GlobalExceptionHandler

      ↓

@ExceptionHandler

      ↓

ResponseEntity<ErrorResponse>

      ↓

HTTP Response
```

The complete architecture now looks like:

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

Exception
      ↑
GlobalExceptionHandler
      ↓
ErrorResponse
      ↓
HTTP Response
```

---

## 📌 Summary

Day 7 introduced proper exception handling into Cartify.

The application moved from:

```text
Generic RuntimeException
        ↓
500 Internal Server Error
```

to:

```text
Specific Exception
        ↓
GlobalExceptionHandler
        ↓
Appropriate HTTP Status
        ↓
Structured ErrorResponse
```

This provides a cleaner and more maintainable foundation for the REST API and prepares the application for Bean Validation in the next lesson.
