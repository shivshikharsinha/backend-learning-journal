# Day 4 - Building the Service Layer & Repository Pattern

**Date:** 1st August 2026

---

## 🎯 Goal

Implement the Service layer, understand the Repository Pattern, and learn how responsibilities are divided across different layers of a Spring Boot application.

---

## ✅ What I Learned

### Why We Need a Repository Layer

Instead of storing or retrieving data inside the Service, a dedicated Repository layer is responsible for all data access operations.

```
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Data Source
```

This keeps each layer focused on a single responsibility.

---

### Implementing an In-Memory Repository

Since a database hasn't been introduced yet, I created an in-memory repository using:

```java
private final List<Product> products = new ArrayList<>();
```

This simulates a database and allows me to focus on architecture before learning Spring Data JPA.

---

### CRUD Operations

Implemented repository methods:

- `findAll()`
- `save()`
- `findById()`

Learned that a Product ID is **not** the same as a List index.

Instead of:

```java
products.get(id)
```

I searched the collection using:

```java
for (Product product : products)
```

and compared IDs using:

```java
product.getId().equals(id)
```

---

### Why Optional Instead of null?

Instead of returning:

```java
return null;
```

I learned to return:

```java
Optional<Product>
```

This makes it explicit that a product may or may not exist and reduces the chances of `NullPointerException`.

---

### Business Decisions Belong in the Service

The Repository simply returns:

```java
Optional<Product>
```

The Service decides what to do when the product doesn't exist.

Example:

```java
return productRepository.findById(id)
        .orElseThrow(() ->
                new RuntimeException("Product not found with id: " + id));
```

This keeps business logic separate from data access.

---

### Constructor Dependency Injection

Injected `ProductRepository` into `ProductService` using constructor injection.

```java
private final ProductRepository productRepository;

public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
}
```

This follows the preferred Dependency Injection approach in modern Spring Boot.

---

## 💡 Key Takeaways

- Repositories should only access data.
- Services contain business rules.
- Controllers should never communicate directly with repositories.
- `Optional` is preferred over returning `null`.
- Business decisions belong in the Service layer.
- Constructor Injection keeps dependencies explicit and immutable.
- Product IDs are business identifiers, not List indexes.

---

## 🧠 Mental Model

```
HTTP Request

        │

        ▼

Controller

        │

        ▼

Service

        │

Business Logic

        │

        ▼

Repository

        │

Data Access

        ▼

List<Product>
(Currently acting as Database)
```

---

## ⚙️ Internal Working

```
Client

↓

ProductController

↓

ProductService

↓

ProductRepository

↓

List<Product>

↓

Product Object

↓

Service

↓

Controller

↓

JSON Response
```

---

## ❌ Common Mistakes

- Returning `null` instead of `Optional`.
- Comparing wrapper classes using `==` instead of `.equals()`.
- Treating Product IDs as List indexes.
- Putting business logic inside the Repository.
- Creating dependencies manually using `new`.

---

## 💼 Interview Takeaways

- What is the Repository Pattern?
- Why should Services contain business logic?
- Why use Constructor Injection?
- Why is `Optional` preferred over `null`?
- Difference between a Product ID and a List index.
- Why shouldn't Controllers directly access Repositories?

---

## 📝 Code Written Today

Created:

- `ProductRepository`
- `ProductService`

Implemented:

- `findAll()`
- `save()`
- `findById()`
- `getAllProducts()`
- `getProductById()`

Used:

- Constructor Dependency Injection
- `Optional`
- `orElseThrow()`

---

## 💭 Reflection

Today I understood that good backend development is not just about making APIs work—it's about designing software where each layer has a clear responsibility. The Repository is responsible for data access, the Service makes business decisions, and the Controller only handles HTTP communication.

I also learned that using `Optional` leads to cleaner and safer code by making the absence of data explicit instead of relying on `null`.

---

## 🚀 Next Goal

- Complete the remaining CRUD operations.
- Build REST APIs for all Product operations.
- Create custom exceptions.
- Learn Global Exception Handling using `@ControllerAdvice`.
- Replace the in-memory repository with Spring Data JPA and MySQL.

---

## 🧩 Connect the Dots

```
Spring Container

↓

ApplicationContext

↓

Beans

↓

Dependency Injection

↓

Controller

↓

Service

↓

Repository

↓

Data Source
```