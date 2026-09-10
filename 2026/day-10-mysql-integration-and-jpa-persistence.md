# Day 10 - Sprint 2: MySQL Integration & JPA Persistence

## Date
11th September 2026

## Sprint
**Sprint 2 - MySQL Integration**

---

# 1. Starting Sprint 2

After completing **Sprint 5 - Validation & Exception Handling**, we started Sprint 2.

The focus of Sprint 2 is to replace the application's temporary in-memory data storage with a real relational database.

### Previous architecture

Before MySQL, products were stored in a Java `List<Product>`:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
List<Product>
```

The problem with this approach is that the data lives only inside the application's memory.

If the application stops:

```text
Application stopped
       ↓
JVM memory cleared
       ↓
List<Product> lost
       ↓
Data lost
```

We therefore need persistent storage.

### New architecture

```text
Controller
    ↓
Service
    ↓
Spring Data JPA Repository
    ↓
Hibernate
    ↓
JDBC
    ↓
MySQL
```

Now the data is stored outside the application and survives application restarts.

---

# 2. Why MySQL?

MySQL is a relational database.

A relational database stores information in:

- Databases
- Tables
- Rows
- Columns

For Cartify, we created:

```text
Database: cartify
Table:    products
```

A simplified representation is:

```text
products
+----+---------------------+--------+
| id | name                | price  |
+----+---------------------+--------+
|  1 | Macbook Air         | 165000 |
|  2 | Sony ZV-E10 Mark II | 150000 |
+----+---------------------+--------+
```

---

# 3. Creating the Database

The Cartify database was created using MySQL command line:

```sql
CREATE DATABASE cartify;
```

We then configured Spring Boot to connect to it.

The datasource URL is:

```text
jdbc:mysql://localhost:3306/cartify
```

The important pieces are:

```text
jdbc:mysql://
        ↓
JDBC connection

localhost
        ↓
MySQL is running on the local machine

3306
        ↓
Default MySQL port

cartify
        ↓
Database name
```

---

# 4. Understanding JDBC

JDBC stands for **Java Database Connectivity**.

It is Java's standard API for communicating with relational databases.

Conceptually:

```text
Java Application
       ↓
JDBC
       ↓
Database
```

Different databases have different drivers.

For MySQL, we use **MySQL Connector/J**.

The dependency added to `pom.xml` was:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

The driver allows Java applications to communicate with MySQL.

---

# 5. Understanding JPA, Hibernate and Spring Data JPA

One of the most important concepts from today was understanding the difference between these technologies.

The complete stack is:

```text
Java Application
       ↓
Spring Data JPA
       ↓
JPA
       ↓
Hibernate
       ↓
JDBC
       ↓
MySQL
```

## JPA

JPA stands for **Java Persistence API**.

JPA is a specification.

It defines concepts and APIs for mapping Java objects to relational database data.

JPA itself is not the database and is not the implementation doing all the work.

## Hibernate

Hibernate is an implementation of JPA.

Hibernate performs ORM-related work such as:

- Mapping Java entities to database tables
- Mapping database rows to Java objects
- Generating SQL
- Managing entity state

## Spring Data JPA

Spring Data JPA provides a higher-level repository abstraction.

Instead of writing database access code manually, we can use:

```java
productRepository.save(product);

productRepository.findAll();

productRepository.findById(id);

productRepository.deleteById(id);
```

---

# 6. Spring Data JPA Dependency

The following dependency was added:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

This brings the required Spring Data JPA functionality into the application.

---

# 7. Converting Product into a JPA Entity

Our `Product` class was already using JPA annotations, but today we connected it to an actual database.

The important annotations are:

```java
@Entity
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Conceptually:

```text
Java Class
    ↓
@Entity
    ↓
Persistent Entity
    ↓
Database Table
```

For our application:

```text
Product
   ↓
products
```

---

# 8. Understanding `@Entity`

We use:

```java
@Entity
public class Product {
    ...
}
```

`@Entity` tells JPA/Hibernate that `Product` is a persistent entity.

Hibernate can therefore map instances of `Product` to rows in the database.

Conceptually:

```text
Java Object
    ↓
Hibernate
    ↓
Database Row
```

---

# 9. Understanding `@Id`

The Product entity contains:

```java
@Id
private Long id;
```

`@Id` tells JPA that this field represents the entity's primary key.

The database therefore has:

```text
id
```

as the primary key of the `products` table.

Important distinction:

```text
@Id
    ↓
JPA primary key mapping

@NotNull
    ↓
Bean Validation rule
```

They are not the same thing.

---

# 10. Generated IDs

We use:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

This means the ID is generated by the database using its identity/auto-increment mechanism.

For example, the client sends:

```json
{
  "name": "Macbook Air",
  "price": 165000
}
```

There is no ID.

The database generates:

```text
id = 1
```

For another product:

```json
{
  "name": "Sony ZV-E10 Mark II",
  "price": 150000
}
```

the database generates:

```text
id = 2
```

### Why is this useful?

The client does not need to know what the next available ID is.

Instead:

```text
Client
  ↓
Product without ID
  ↓
Application
  ↓
MySQL
  ↓
Database generates ID
```

This avoids client-side ID collision problems and keeps primary-key generation under database control.

---

# 11. Why `Long` Instead of `long`?

The ID is:

```java
private Long id;
```

instead of:

```java
private long id;
```

A new entity can have:

```text
id = null
```

before it is persisted.

That is useful because:

```text
null ID
   ↓
new entity
   ↓
save
   ↓
database generates ID
```

A primitive `long` cannot represent `null`.

---

# 12. JPA No-Argument Constructor

A no-argument constructor was added to the entity:

```java
public Product() {
}
```

The parameterized constructor was retained:

```java
public Product(Long id, String name, Double price) {
    this.id = id;
    this.name = name;
    this.price = price;
}
```

The no-argument constructor is important for JPA/Hibernate because Hibernate needs to be able to instantiate entity objects when reading data from the database.

---

# 13. Database Schema Generation

The application is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This allows Hibernate to update the database schema based on the entity mappings.

After starting the application, Hibernate created the `products` table.

The table was verified using:

```sql
DESCRIBE products;
```

The resulting table was:

```text
+-------+--------------+------+-----+---------+----------------+
| Field | Type         | Null | Key | Default | Extra          |
+-------+--------------+------+-----+---------+----------------+
| id    | bigint       | NO   | PRI | NULL    | auto_increment |
| name  | varchar(255) | NO   |     | NULL    |                |
| price | double       | NO   |     | NULL    |                |
+-------+--------------+------+-----+---------+----------------+
```

---

# 14. Understanding the Database Mapping

The Java fields were mapped approximately like this:

```text
Java                  MySQL
--------------------------------
Long                  BIGINT
String                VARCHAR(255)
Double                DOUBLE
@Id                   PRIMARY KEY
IDENTITY              AUTO_INCREMENT
```

The important observation was:

```text
@GeneratedValue(IDENTITY)
        ↓
AUTO_INCREMENT
```

So MySQL is responsible for generating the ID.

---

# 15. Replacing the Custom Repository

Before Sprint 2, the repository was a custom implementation backed by a `List<Product>`.

It contained manually implemented operations such as:

```java
save()
findById()
findAll()
deleteById()
```

We replaced it with:

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

This is one of the biggest changes in the application.

We no longer need to manually implement basic CRUD operations.

---

# 16. Understanding `JpaRepository<Product, Long>`

The declaration:

```java
JpaRepository<Product, Long>
```

contains two important types.

```text
Product
   ↓
Entity type

Long
   ↓
Primary key type
```

So:

```java
JpaRepository<Product, Long>
```

means:

> This repository manages `Product` entities whose primary key is of type `Long`.

---

# 17. Where Does `save()` Come From?

Our repository is only:

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

There is no implementation written by us.

Yet this works:

```java
productRepository.save(product);
```

Spring Data JPA creates/provides the repository implementation at runtime.

Conceptually:

```text
ProductRepository
       ↓
Spring Data JPA implementation
       ↓
save()
findAll()
findById()
deleteById()
...
```

This is an important benefit of Spring Data JPA.

---

# 18. The `createProduct()` Problem

Initially, the service still contained the old ID-based duplicate check.

The old logic was conceptually:

```java
Long productId = product.getId();

if (productRepository.findById(productId).isPresent()) {
    throw new ProductAlreadyExistsException(
        "Product ID already exists."
    );
}
```

This was appropriate when the application was manually dealing with IDs.

After introducing generated IDs, a new product has:

```text
product.getId()
       ↓
null
```

Therefore this was effectively:

```java
productRepository.findById(null);
```

which is invalid.

The database should generate the ID for a new product.

---

# 19. Final `createProduct()` Method

The service method was simplified to:

```java
public Product createProduct(Product product) {
    return productRepository.save(product);
}
```

The old manual object validation was already removed because Bean Validation handles object-level input validation.

The old manual ID existence check is also not appropriate for generated IDs.

---

# 20. Creating Products in MySQL

Two products were successfully created through the REST API.

The database contained:

```text
+----+---------------------+--------+
| id | name                | price  |
+----+---------------------+--------+
|  1 | Macbook Air         | 165000 |
|  2 | Sony ZV-E10 Mark II | 150000 |
+----+---------------------+--------+
```

This was the first direct confirmation that:

```text
REST API
   ↓
Service
   ↓
Spring Data JPA
   ↓
Hibernate
   ↓
JDBC
   ↓
MySQL
```

was working end-to-end.

---

# 21. Understanding `save()`

When we execute:

```java
productRepository.save(product);
```

Spring Data JPA and Hibernate handle the persistence operation.

For a new entity, conceptually:

```text
Product
id = null
name = "Keyboard"
price = 2000
```

becomes an INSERT operation.

Conceptually:

```sql
INSERT INTO products (name, price)
VALUES (?, ?);
```

The database then generates the ID.

For an existing entity, `save()` can result in an UPDATE operation.

This distinction will be explored further through JPA entity states.

---

# 22. Enabling Hibernate SQL Logging

To observe what Hibernate is doing, SQL logging was enabled with:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

We also discussed:

```properties
logging.level.org.hibernate.SQL=DEBUG
```

as another way to make Hibernate SQL logging more explicit.

---

# 23. Seeing Hibernate Generate SQL

After restarting the application and calling:

```http
GET /products
```

the application eventually executes:

```java
productRepository.findAll();
```

Hibernate generated SQL similar to:

```sql
select
    p1_0.id,
    p1_0.name,
    p1_0.price
from
    products p1_0
```

The important observation:

**We did not write this SQL ourselves.**

Hibernate generated it based on the entity and repository operation.

---

# 24. Understanding `findAll()`

Our Java code can simply say:

```java
productRepository.findAll();
```

The conceptual flow is:

```text
ProductController
       ↓
ProductService
       ↓
productRepository.findAll()
       ↓
Spring Data JPA
       ↓
Hibernate
       ↓
SQL generated
       ↓
JDBC
       ↓
MySQL
       ↓
Database rows
       ↓
Hibernate maps rows
       ↓
Product objects
       ↓
Service
       ↓
Controller
       ↓
Jackson
       ↓
JSON response
```

---

# 25. Database Row → Java Object

Suppose MySQL has:

```text
+----+--------+-------+
| id | name   | price |
+----+--------+-------+
|  5 | Camera | 75000 |
+----+--------+-------+
```

Hibernate maps this database row to a Java entity.

Conceptually:

```java
Product product =
    new Product(5L, "Camera", 75000.0);
```

So:

```text
Database Row
      ↓
   Hibernate
      ↓
Product Object
```

---

# 26. Responsibility of Each Layer

Understanding the responsibility of each technology is important.

## MySQL

Stores persistent data.

```text
Data
 ↓
MySQL
```

## JDBC

Provides Java's standard database connectivity API.

```text
Java
 ↓
JDBC
 ↓
Database
```

## Hibernate

Handles ORM and generates SQL.

```text
Java Entity
    ↕
Hibernate
    ↕
Database
```

## Spring Data JPA

Provides repository abstractions:

```java
save()
findAll()
findById()
deleteById()
```

## Jackson

Converts Java objects to JSON and JSON to Java objects as part of HTTP request/response handling.

Example:

```text
Product Object
      ↓
    Jackson
      ↓
{
    "id": 5,
    "name": "Camera",
    "price": 75000
}
```

---

# 27. Understanding `findById()`

`JpaRepository` provides:

```java
findById(id)
```

The return type is:

```java
Optional<Product>
```

Why?

Because a product may or may not exist.

For example:

```text
GET /products/1
       ↓
Database row found
       ↓
Optional<Product>
       ↓
Product
```

But:

```text
GET /products/999
       ↓
No database row
       ↓
Optional.empty()
```

Therefore the service can use:

```java
productRepository.findById(id)
        .orElseThrow(...);
```

This allows the application to explicitly handle the not-found case.

---

# 28. Understanding HikariCP

During startup, the logs showed:

```text
HikariPool-1 - Added connection
```

HikariCP is the database connection pool used by the application.

Instead of creating and destroying a database connection for every request, the application maintains reusable connections.

Conceptually:

```text
              ┌── Connection
              ├── Connection
Application ──┼── Connection
              ├── Connection
              └── Connection
                     ↓
                   MySQL
```

The application can borrow a connection when needed and return it to the pool.

This improves database connection management and performance.

---

# 29. Verifying the Application Connection

The Spring Boot startup logs confirmed:

```text
Database JDBC URL [jdbc:mysql://localhost:3306/cartify]
Database driver: MySQL Connector/J
Database dialect: MySQLDialect
Database version: 8.4.11
```

The logs also showed:

```text
Initialized JPA EntityManagerFactory
```

This confirmed that:

- Spring Boot started successfully.
- Spring Data JPA detected the repository.
- Hibernate initialized.
- HikariCP established a connection.
- MySQL was reachable.
- JPA initialized successfully.

---

# 30. Complete Architecture After Sprint 2 Progress

The application now follows:

```text
                        HTTP Request
                             │
                             ▼
                    ProductController
                             │
                             ▼
                       ProductService
                             │
                             ▼
                    ProductRepository
                             │
                             ▼
                    Spring Data JPA
                             │
                             ▼
                          JPA
                             │
                             ▼
                        Hibernate
                             │
                             ▼
                           JDBC
                             │
                             ▼
                         HikariCP
                             │
                             ▼
                           MySQL
                             │
                             ▼
                      products table
```

For a response:

```text
MySQL
  ↓
JDBC
  ↓
Hibernate
  ↓
Product Object
  ↓
Service
  ↓
Controller
  ↓
Jackson
  ↓
JSON Response
```

---

# 31. Important Mental Model

The most important mental model from today is:

```text
Java Object
     ↓
JPA / Hibernate
     ↓
SQL
     ↓
Database
```

And in the opposite direction:

```text
Database Row
     ↓
SQL Result
     ↓
JPA / Hibernate
     ↓
Java Object
```

This is the basic idea behind **Object-Relational Mapping (ORM)**.

---

# 32. What We Learned About Persistence

Before MySQL:

```text
Product
   ↓
List<Product>
```

The application was responsible for keeping the data in memory.

After MySQL:

```text
Product
   ↓
Spring Data JPA
   ↓
Hibernate
   ↓
JDBC
   ↓
MySQL
```

The database is now responsible for persistent storage.

The application can restart without losing the stored products.

---

# 33. Important Lessons

- MySQL provides persistent storage.
- JDBC provides Java database connectivity.
- JPA is a specification.
- Hibernate is an implementation of JPA.
- Spring Data JPA provides repository abstractions.
- `@Entity` tells JPA that a class represents persistent data.
- `@Id` defines the primary key field.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` lets MySQL generate IDs.
- `Long` allows a new entity ID to initially be `null`.
- JPA entities need a no-argument constructor.
- `JpaRepository<Product, Long>` provides common CRUD operations.
- Spring Data JPA creates/provides repository implementations at runtime.
- `save()` handles persistence of entities.
- `findAll()` results in a SQL `SELECT`.
- `findById()` returns `Optional<Product>`.
- Hibernate maps database rows to Java entity objects.
- Hibernate can generate SQL without us writing SQL manually.
- HikariCP manages reusable database connections.
- Jackson converts Java objects to JSON responses.
- Database-generated IDs are preferable to client-generated IDs for this use case.

---

# 34. Key Debugging Lesson

We encountered a `500 Internal Server Error` while creating the first MySQL-backed product.

The cause was the old service logic attempting to check the product ID before saving:

```java
Long productId = product.getId();

productRepository.findById(productId);
```

Because the ID is now generated by MySQL:

```text
New Product
    ↓
id = null
```

The lookup became:

```java
productRepository.findById(null);
```

The fix was to remove the old ID lookup and allow the repository to save the new entity:

```java
public Product createProduct(Product product) {
    return productRepository.save(product);
}
```

This was an important example of how changing from application-managed IDs to database-generated IDs affects service-layer logic.

---

# 35. Current Sprint Status

```text
Sprint 2 — MySQL Integration

Database created                         ✅
MySQL connection configured              ✅
MySQL driver added                       ✅
Spring Data JPA added                    ✅
Product mapped as JPA entity             ✅
Generated ID configured                  ✅
No-argument JPA constructor added        ✅
products table created                   ✅
Custom repository replaced               ✅
JpaRepository configured                 ✅
Product creation persisted               ✅
Database records verified                ✅
Hibernate SQL logging verified           ✅
findAll() SQL observed                   ✅
JPA Entity States                        ⬜ Next
Update flow                              ⬜
Delete flow                              ⬜
Transactions                             ⬜
```

---

# 36. Next Topic

The next topic is:

## JPA Entity States

We will learn about:

```text
New
 ↓
Managed
 ↓
Detached
 ↓
Removed
```

This will explain what happens internally when we:

- Create a product
- Retrieve a product
- Modify a product
- Update a product
- Delete a product
- Call `save()`
- Work with transactions

This will take us deeper into how Hibernate manages entities and will make the UPDATE operation much easier to understand.

---

# Final Takeaway

Today we moved Cartify from a simple in-memory application to a backend that can persist real data.

The most important transformation was:

```text
BEFORE

Java
 ↓
List<Product>


AFTER

Java
 ↓
Spring Data JPA
 ↓
Hibernate
 ↓
JDBC
 ↓
MySQL
```

We also verified the complete flow ourselves by creating products and seeing the generated SQL.

This gives us the foundation needed to understand the deeper parts of JPA and database-backed Spring Boot applications.
