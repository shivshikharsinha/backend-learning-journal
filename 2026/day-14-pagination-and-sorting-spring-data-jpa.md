# Day 14 - Pagination & Sorting with Spring Data JPA

**Date:** 18th September 2026

## 🎯 Goal

Today I learned how to implement **Pagination and Sorting** using Spring Data JPA.

The goal was to understand how an API can return a limited number of records at a time and how the results can be sorted without writing custom SQL or repository methods.

---

## ✅ What I Learned

### 1. Why Pagination Is Needed

Our original endpoint:

```text
GET /products
```

returns all products.

This is fine when the database contains only a few products, but returning thousands or millions of records in a single response can cause:

- Large database queries
- Higher memory usage
- Larger HTTP responses
- Slower API responses
- More processing for the client

Pagination solves this by returning only a portion of the results at a time.

For example:

```text
100 products

Page 0 → Products 1–10
Page 1 → Products 11–20
Page 2 → Products 21–30
...
```

---

### 2. `Pageable`

Spring Data provides the `Pageable` abstraction to represent pagination information.

Conceptually:

```text
Pageable
 ├── page number
 ├── page size
 └── sorting
```

For example:

```java
Pageable pageable = PageRequest.of(0, 10);
```

means:

```text
Page = 0
Size = 10
```

Page numbering starts from `0`.

---

### 3. `Page<Product>`

Previously, a normal query returned:

```java
List<Product>
```

For pagination, I used:

```java
Page<Product>
```

A `Page` contains the actual products along with pagination metadata.

The response can contain information such as:

```text
content
total elements
total pages
current page
page size
number of elements
first page
last page
sorting information
```

---

### 4. Repository Support

Because my repository extends:

```java
JpaRepository<Product, Long>
```

Spring Data already provides:

```java
findAll(Pageable pageable)
```

I did not need to create a new repository method for pagination.

---

### 5. Service Layer

I added:

```java
public Page<Product> getProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
}
```

The Service passes the `Pageable` object to the repository.

The flow is:

```text
Controller
    ↓
Pageable
    ↓
Service
    ↓
repository.findAll(pageable)
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
MySQL
```

---

### 6. Controller and Query Parameters

I added a separate endpoint for learning pagination:

```java
@GetMapping("/page")
public Page<Product> getProducts(Pageable pageable) {
    return productService.getProducts(pageable);
}
```

Spring MVC automatically converts query parameters into the `Pageable` object.

For example:

```text
GET /products/page?page=0&size=2
```

is interpreted as:

```text
page = 0
size = 2
```

---

### 7. Pagination in Practice

My database contained three products.

When I requested:

```text
GET /products/page?page=0&size=2
```

the response contained the first two products.

Important metadata included:

```json
{
    "number": 0,
    "numberOfElements": 2,
    "totalElements": 3,
    "totalPages": 2,
    "first": true,
    "last": false
}
```

This means:

```text
Current page      = 0
Products returned = 2
Total products    = 3
Total pages       = 2
First page        = true
Last page         = false
```

I then tested page `1` and received the remaining product.

---

### 8. Sorting

`Pageable` also supports sorting.

I tested:

```text
GET /products/page?page=0&size=10&sort=price,desc
```

This means:

```text
page      = 0
size      = 10
property  = price
direction = desc
```

The products were returned from highest price to lowest price.

I also tested:

```text
GET /products/page?page=0&size=10&sort=price,asc
```

which returned the products from lowest price to highest price.

---

### 9. Pagination and Sorting Together

Pagination and sorting can be combined in the same request:

```text
GET /products/page?page=0&size=2&sort=price,desc
```

Conceptually:

```text
Pageable
│
├── Pagination
│   ├── page = 0
│   └── size = 2
│
└── Sorting
    ├── property = price
    └── direction = DESC
```

---

## 💡 Key Takeaways

- Pagination prevents an API from returning every record at once.
- `Pageable` represents pagination and sorting information.
- Page numbering starts at `0`.
- `Page<Product>` contains both data and pagination metadata.
- `JpaRepository` already provides `findAll(Pageable pageable)`.
- No custom repository method is required for basic pagination.
- Spring MVC can automatically create `Pageable` from query parameters.
- Sorting can be supplied through the `sort` query parameter.
- Pagination and sorting can be combined.
- The existing Controller → Service → Repository architecture remains unchanged.

---

## 🧠 Mental Model

```text
HTTP Request

GET /products/page?page=0&size=2&sort=price,desc
                         ↓
                  Spring MVC
                         ↓
                    Pageable
              ┌─────────────────┐
              │ page = 0        │
              │ size = 2        │
              │ price DESC      │
              └─────────────────┘
                         ↓
                      Service
                         ↓
            findAll(pageable)
                         ↓
                 Spring Data JPA
                         ↓
                     Hibernate
                         ↓
                       MySQL
                         ↓
                   Page<Product>
                         ↓
                    JSON Response
```

---

## ⚙️ Internal Working

The request:

```text
?page=0&size=2&sort=price,desc
```

is converted into a `Pageable` object by Spring MVC.

The Service receives it:

```java
public Page<Product> getProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
}
```

The repository passes the pagination and sorting information to Spring Data JPA.

Spring Data and Hibernate then generate the appropriate SQL for the database.

The database returns the required result set, which is wrapped into a `Page<Product>` containing both the products and pagination metadata.

---

## ❌ Common Mistakes

### 1. Thinking page numbering starts at 1

Spring Data uses zero-based page numbering:

```text
Page 0 → first page
Page 1 → second page
Page 2 → third page
```

---

### 2. Manually calculating pagination in the Service

I don't need to manually calculate:

```text
offset
limit
total pages
```

Spring Data handles the pagination using `Pageable`.

---

### 3. Creating another repository method unnecessarily

Because `JpaRepository` already provides:

```java
findAll(Pageable pageable)
```

I don't need to create another repository method for basic pagination.

---

### 4. Confusing `List` and `Page`

```java
List<Product>
```

contains the matching products.

```java
Page<Product>
```

contains the products plus pagination information.

---

## 💼 Interview Takeaways

### What is `Pageable`?

`Pageable` represents pagination and sorting information for a database query.

### What is `Page<T>`?

`Page<T>` contains a page of results along with metadata such as total elements, total pages, current page, and page size.

### Does `JpaRepository` support pagination?

Yes. It provides:

```java
Page<T> findAll(Pageable pageable);
```

### Does pagination require a custom repository query?

No. Basic pagination is already supported by Spring Data JPA.

### How does Spring receive pagination parameters?

Spring MVC can bind query parameters such as:

```text
?page=0&size=10
```

to a `Pageable` parameter.

---

## 📝 Code Written Today

### ProductService

```java
public Page<Product> getProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
}
```

### ProductController

```java
@GetMapping("/page")
public Page<Product> getProducts(Pageable pageable) {
    return productService.getProducts(pageable);
}
```

### Example Requests

```text
GET /products/page?page=0&size=2
```

```text
GET /products/page?page=1&size=2
```

```text
GET /products/page?page=0&size=10&sort=price,desc
```

```text
GET /products/page?page=0&size=10&sort=price,asc
```

---

## 💭 Reflection

Today I learned how pagination and sorting can be added to a Spring Boot API without writing custom SQL.

The most useful part was seeing that `Pageable` carries both pagination and sorting information.

I tested the API with different page numbers, page sizes, and sorting directions and observed the resulting data and metadata.

The important idea for me is:

```text
Pageable
    ↓
Pagination + Sorting
    ↓
Repository
    ↓
Hibernate
    ↓
MySQL
```

This makes the API much more practical for applications that can contain large amounts of data.

---

## 🚀 Next Goal

Continue exploring practical Spring Data JPA features and complete the remaining important parts of **Sprint 2 – MySQL Integration** before moving to the next sprint.

---

## 🧩 Connect the Dots

```text
Spring Boot
     ↓
Controller
     ↓
Pageable
     ↓
Service
     ↓
Spring Data JPA Repository
     ↓
JPA
     ↓
Hibernate
     ↓
JDBC / HikariCP
     ↓
MySQL

Pagination + Sorting
        ↓
     Pageable
        ↓
    Page<Product>
        ↓
    JSON Response
```

Today I connected pagination and sorting with the JPA repository layer and saw how Spring Boot carries request parameters through the application to the database query.
