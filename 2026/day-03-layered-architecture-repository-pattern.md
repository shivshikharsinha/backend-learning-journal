# Day 3 - Layered Architecture, Dependency Injection & Repository Pattern

**Date:** 1st August 2026

---

## 🎯 Goal

Understand how a production-style Spring Boot application is structured by implementing the Controller → Service → Repository architecture and learning the purpose of each layer.

---

## ✅ What I Learned

### Layered Architecture

A Spring Boot application follows a layered architecture where each layer has a specific responsibility.

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

This separation makes the application easier to maintain, test, and scale.

---

### Controller

The Controller is responsible for handling HTTP requests and returning HTTP responses.

It should **not** contain business logic.

Example:

```java
@GetMapping("/{id}")
public Product getProductById(@PathVariable Long id) {
    return productService.getProductById(id);
}
```

---

### Service

The Service layer contains the application's business logic.

Examples:

- Product must exist.
- Maximum purchase quantity is 5.
- Apply discounts.
- Validate business rules.

The Service acts as the bridge between the Controller and Repository.

---

### Repository

The Repository is responsible only for data access.

For now, I created an in-memory repository using:

```java
private final List<Product> products = new ArrayList<>();
```

Later, this will be replaced by MySQL using Spring Data JPA.

---

### Constructor Dependency Injection

Instead of creating dependencies manually:

```java
ProductRepository repository = new ProductRepository();
```

Spring injects them automatically through constructors.

Example:

```java
private final ProductRepository productRepository;

public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
}
```

Constructor Injection is the preferred approach in modern Spring Boot.

---

### Repository Methods

Implemented:

- `findAll()`
- `save()`
- `findById()`

Learned how to search a collection using a for-each loop instead of treating Product IDs as List indexes.

---

### Optional

Instead of returning:

```java
return null;
```

the repository now returns:

```java
Optional<Product>
```

This makes it explicit that a value may or may not exist.

The Service decides what to do if the Optional is empty.

Example:

```java
return productRepository.findById(id)
        .orElseThrow(() ->
                new RuntimeException("Product not found with id: " + id));
```

---

### Fail Fast

Spring follows the Fail Fast principle.

If a required dependency is missing, the application fails during startup instead of failing later at runtime.

This helps catch configuration errors early.

---

## 💡 Key Takeaways

- Controller handles HTTP communication.
- Service contains business logic.
- Repository only performs data access.
- Constructor Injection is preferred over Field Injection.
- Dependencies should be injected by Spring instead of created manually.
- `Optional` is a safer alternative to returning `null`.
- Spring follows the Fail Fast philosophy.
- Every layer should have a single responsibility.

---

## 🧠 Mental Model

```
Client

        │

        ▼

Controller

        │

        ▼

Service

        │

        ▼

Repository

        │

        ▼

Database (Currently List<Product>)
```

---

## ⚙️ Internal Working

```
Spring Boot Starts

↓

Component Scan

↓

Creates ProductRepository Bean

↓

Creates ProductService Bean
(needs ProductRepository)

↓

Injects ProductRepository

↓

Creates ProductController Bean
(needs ProductService)

↓

Injects ProductService

↓

Application Ready
```

---

## ❌ Common Mistakes

- Putting business logic inside Controllers.
- Returning `null` instead of `Optional`.
- Comparing `Long` objects using `==` instead of `.equals()`.
- Creating Spring Beans manually using `new`.
- Mixing business logic with data access logic.

---

## 💼 Interview Takeaways

- Explain Layered Architecture.
- Why do we need a Service layer?
- What is the Repository Pattern?
- Why is Constructor Injection preferred?
- Why should repositories return `Optional`?
- What is the Fail Fast principle?

---

## 📝 Code Written Today

- Created `ProductService`.
- Created `ProductRepository`.
- Implemented Constructor Dependency Injection.
- Implemented:
  - `findAll()`
  - `save()`
  - `findById()`
- Used `Optional<Product>` instead of `null`.
- Used `orElseThrow()` to handle missing products.

---

## 💭 Reflection

Today was one of the most important milestones in my Spring Boot journey.

I stopped thinking of Spring as just a collection of annotations and started understanding the architectural decisions behind it. Instead of memorizing where code should go, I learned **why** Controllers, Services, and Repositories exist and how each layer contributes to building clean, maintainable software.

I also realized that Dependency Injection is not just a Spring feature—it is a design principle that promotes loose coupling, testability, and maintainability.

---

## 🚀 Next Goal

- Complete all ProductService methods.
- Implement CRUD operations.
- Create custom exceptions.
- Replace the in-memory repository with MySQL using Spring Data JPA.

---

## 🧩 Connect the Dots

```
Java

↓

Spring Container

↓

ApplicationContext

↓

Component Scanning

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