# Day 13 - Spring Data JPA Query Methods

**Date:** 17th September 2026

## 🎯 Goal

Today I learned how to create custom database queries using **Spring Data JPA Query Methods** without writing SQL manually.

The main goal was to understand how Spring Data reads repository method names and derives the corresponding database query.

---

## ✅ What I Learned

### 1. Spring Data JPA Query Methods

Spring Data JPA allows me to define custom query methods directly inside my repository interface.

For example:

```java
List<Product> findByName(String name);
```

I don't need to write an implementation or SQL query manually. Spring Data interprets the method name and generates the required query.

---

### 2. How `findByName()` Works

I added this method to `ProductRepository`:

```java
List<Product> findByName(String name);
```

The method name can be understood as:

```text
find
 ↓
By
 ↓
Name
```

Spring Data understands that `Name` refers to the `name` field of the `Product` entity.

Conceptually:

```sql
SELECT *
FROM products
WHERE name = ?;
```

---

### 3. Query Method for `GreaterThan`

I also created:

```java
List<Product> findByPriceGreaterThan(Double price);
```

Spring Data interprets:

```text
findBy
  ↓
Price
  ↓
GreaterThan
```

Conceptually:

```sql
WHERE price > ?
```

For example:

```java
productRepository.findByPriceGreaterThan(100000.0);
```

returned products whose price was greater than `100000`.

---

### 4. Repository Layer

My `ProductRepository` now contains:

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByName(String name);

    List<Product> findByPriceGreaterThan(Double price);
}
```

I only declare the methods. I don't write their implementations.

Spring Data creates the repository implementation at runtime.

---

### 5. Service Layer

I exposed the repository functionality through the Service layer.

For searching by name:

```java
public List<Product> getProductsByName(String name) {
    return productRepository.findByName(name);
}
```

For products above a specific price:

```java
public List<Product> getProductsByPriceGreaterThan(Double price) {
    return productRepository.findByPriceGreaterThan(price);
}
```

This keeps the layered architecture intact:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
MySQL
```

---

### 6. Controller Endpoints

I added:

```java
@GetMapping("/name/{name}")
public List<Product> getProductsByName(@PathVariable String name) {
    return productService.getProductsByName(name);
}
```

And:

```java
@GetMapping("/price/greater-than/{price}")
public List<Product> getProductsByPriceGreaterThan(
        @PathVariable Double price) {

    return productService.getProductsByPriceGreaterThan(price);
}
```

For example:

```text
GET /products/name/Macbook%20Air%20M4
```

returned the matching Macbook product.

And:

```text
GET /products/price/greater-than/100000
```

returned the Macbook and Sony camera, while the iPhone priced at `99000` was excluded.

---

### 7. Seeing the Generated SQL

I already had SQL logging enabled:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

When I called the price query, Hibernate generated SQL similar to:

```sql
select
    p1_0.id,
    p1_0.name,
    p1_0.price
from
    products p1_0
where
    p1_0.price > ?
```

The parameter was:

```text
100000.0
```

This showed the complete connection:

```text
findByPriceGreaterThan()
        ↓
Spring Data JPA
        ↓
Hibernate
        ↓
SQL
        ↓
MySQL
```

---

### 8. Query Method Naming Convention

I learned that Spring Data supports predefined keywords in repository method names.

Examples:

```text
findByName
        ↓
name = ?

findByPriceGreaterThan
        ↓
price > ?

findByPriceLessThan
        ↓
price < ?

findByPriceGreaterThanEqual
        ↓
price >= ?

findByPriceBetween
        ↓
price BETWEEN ? AND ?

findByNameAndPriceGreaterThan
        ↓
name = ? AND price > ?
```

I don't need to memorize every keyword. The important thing is understanding the pattern and knowing that the method name must follow Spring Data's supported query-method naming conventions.

---

## 💡 Key Takeaways

- Spring Data JPA can derive database queries from repository method names.
- I don't need to write SQL for simple derived queries.
- `findByName()` searches using the `name` field.
- `findByPriceGreaterThan()` creates a greater-than condition.
- Spring Data creates the repository implementation at runtime.
- Query methods still follow the Controller → Service → Repository architecture.
- Query methods can return a `List<Product>` when multiple records may match.
- Method names must follow Spring Data's supported query-method conventions.
- Hibernate converts the derived query into SQL that MySQL executes.
- I don't need to implement every query variation; understanding the naming pattern is more important.

---

## 🧠 Mental Model

```text
Repository Method
        ↓
Spring Data JPA Parser
        ↓
Method Name Interpretation
        ↓
Query Generation
        ↓
Hibernate
        ↓
SQL
        ↓
MySQL
```

For example:

```text
findByPriceGreaterThan(100000)
        ↓
price > 100000
        ↓
Hibernate
        ↓
SELECT ... FROM products WHERE price > ?
        ↓
MySQL
```

---

## ⚙️ Internal Working

When the application starts, Spring Data JPA discovers the repository interface and its query methods.

For:

```java
List<Product> findByPriceGreaterThan(Double price);
```

Spring Data recognizes:

```text
findBy
    ↓
Price
    ↓
GreaterThan
```

It validates the method against the entity's fields and creates the repository implementation.

When the method is called:

```java
productRepository.findByPriceGreaterThan(100000.0);
```

the request flows through:

```text
Service
    ↓
Repository Proxy
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
SQL
    ↓
MySQL
```

---

## ❌ Common Mistakes

### 1. Writing arbitrary English in the method name

This is not how Spring Data derives queries:

```java
findProductsWherePriceIsGreater(...)
```

Instead, use supported query keywords:

```java
findByPriceGreaterThan(...)
```

### 2. Implementing the repository method manually

For a derived query, Spring Data provides the implementation.

### 3. Putting database logic directly in the Controller

The architecture remains:

```text
Controller
    ↓
Service
    ↓
Repository
```

### 4. Returning a single object when multiple records can match

For:

```java
findByName(String name)
```

I used:

```java
List<Product>
```

because multiple products could potentially have the same name.

---

## 💼 Interview Takeaways

### What are Spring Data JPA derived query methods?

They are repository methods whose names follow predefined conventions, allowing Spring Data to derive the database query automatically.

### Does Spring Data require an implementation for a derived query?

No. Spring Data creates the repository implementation at runtime.

### What does `findByPriceGreaterThan()` mean?

It means:

```text
Find Product entities where price > the supplied value.
```

### Why use `List<Product>`?

Because the query may return multiple matching records.

### Does the method name have to follow a specific convention?

Yes. Spring Data understands specific keywords and patterns. Arbitrary method names do not automatically become queries.

---

## 📝 Code Written Today

### ProductRepository

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByName(String name);

    List<Product> findByPriceGreaterThan(Double price);
}
```

### ProductService

```java
public List<Product> getProductsByName(String name) {
    return productRepository.findByName(name);
}

public List<Product> getProductsByPriceGreaterThan(Double price) {
    return productRepository.findByPriceGreaterThan(price);
}
```

### ProductController

```java
@GetMapping("/name/{name}")
public List<Product> getProductsByName(@PathVariable String name) {
    return productService.getProductsByName(name);
}
```

```java
@GetMapping("/price/greater-than/{price}")
public List<Product> getProductsByPriceGreaterThan(
        @PathVariable Double price) {

    return productService.getProductsByPriceGreaterThan(price);
}
```

---

## 💭 Reflection

Today I understood how Spring Data JPA can generate queries from repository method names.

The most useful part was actually testing the API and seeing the SQL generated by Hibernate.

I wrote:

```java
findByPriceGreaterThan(...)
```

and could see the corresponding SQL condition:

```sql
WHERE price > ?
```

This made the relationship between my Java repository code and the database much clearer.

I also learned that I don't need to implement every query variation. Understanding the naming pattern is more important, and I can refer to the supported keywords when I need a specific query.

---

## 🚀 Next Goal

Next, I will learn **Pagination & Sorting**.

Instead of returning every product from:

```text
GET /products
```

I will learn how to return products in smaller pages using:

```text
Page
Pageable
Sort
```

For example:

```text
GET /products?page=0&size=10
```

---

## 🧩 Connect the Dots

```text
Spring Boot
     ↓
Controller
     ↓
Service
     ↓
Spring Data JPA Repository
     ↓
Derived Query Method
     ↓
JPA
     ↓
Hibernate
     ↓
JDBC / HikariCP
     ↓
MySQL
```

The important connection from today is:

```text
Java Method Name
        ↓
Spring Data JPA
        ↓
Generated Query
        ↓
Hibernate
        ↓
SQL
        ↓
Database Result
        ↓
Product Objects
        ↓
JSON Response
```
