# Day 12 - Practical JPA CRUD: Update & Delete

**Date:** 14th September 2026

## 🎯 Goal

Today I continued the practical JPA work in my Cartify backend and completed the **Update and Delete operations** using Spring Data JPA.

The focus was on understanding how JPA works with existing entities, generated IDs, transactions, and entity deletion.

---

## ✅ What I Learned

### 1. Updating an Existing Entity

For an update operation, I should first find the existing product using its ID.

```java
Product existingProduct = productRepository.findById(id)
        .orElseThrow(() ->
                new ProductNotFoundException("Product not found."));
```

This gives me the actual entity stored in the database.

The important idea is:

```text
Request Object
     ↓
Find Existing Entity
     ↓
Modify Existing Entity
     ↓
JPA Dirty Checking
     ↓
Database UPDATE
```

I should not simply replace the existing entity with the request object because the existing entity is the one managed by the JPA persistence context.

---

### 2. Using `@Transactional` for Updates

My update method uses:

```java
@Transactional
public Product updateProduct(Long id, Product updatedProduct) {
    ...
}
```

Inside the transaction, the entity retrieved using `findById()` is managed by JPA.

When I modify it:

```java
existingProduct.setName(updatedProduct.getName());
existingProduct.setPrice(updatedProduct.getPrice());
```

Hibernate detects these changes through **dirty checking** and generates the required SQL during flush.

I don't need to explicitly call `save()` after modifying the managed entity.

---

### 3. PUT Mapping

The update endpoint is:

```java
@PutMapping("/{id}")
public Product updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody Product product) {

    return productService.updateProduct(id, product);
}
```

The ID comes from the URL:

```text
PUT /products/1
```

while the updated values come from the request body.

Example:

```json
{
    "name": "Macbook Air M4",
    "price": 170000
}
```

---

### 4. Validation During Update

I tested an invalid update request:

```json
{
    "name": "",
    "price": 170000
}
```

Because the controller uses:

```java
@Valid @RequestBody Product product
```

Bean Validation runs before the request reaches the service.

The response correctly returns:

```text
400 Bad Request
name: must not be blank
```

This reinforced the difference between:

```text
Validation
    ↓
Is the request data valid?

Business Logic
    ↓
Does the requested operation make sense?

Database
    ↓
Can the data be persisted?
```

---

### 5. Deleting an Entity

The delete operation is implemented using:

```java
public void deleteProduct(Long id) {

    if (id == null) {
        throw new RuntimeException("Product ID cannot be null.");
    }

    Product product = productRepository.findById(id)
            .orElseThrow(() ->
                    new ProductNotFoundException("Product not found."));

    productRepository.delete(product);
}
```

The flow is:

```text
DELETE /products/{id}
        ↓
Controller
        ↓
Service
        ↓
findById()
        ↓
Product exists?
   ├── No → ProductNotFoundException → 404
   └── Yes
        ↓
delete(product)
        ↓
Entity marked REMOVED
        ↓
Flush / Commit
        ↓
Database DELETE
```

Using `delete(product)` also connects directly with the JPA entity lifecycle that I learned previously.

---

### 6. DELETE Mapping

The controller endpoint is:

```java
@DeleteMapping("/{id}")
public void deleteProductById(@PathVariable Long id) {
    productService.deleteProduct(id);
}
```

Example request:

```text
DELETE /products/2
```

If the product exists, it is deleted.

If it doesn't exist, the service throws `ProductNotFoundException`, which is converted into a `404 Not Found` response by the global exception handler.

---

### 7. Generated IDs and `AUTO_INCREMENT`

I also encountered an important practical behavior with generated IDs.

My entity uses:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

This means the database generates the ID.

For example:

```text
ID   Product
1    Macbook Air
2    Sony ZV-E10 Mark II
```

If ID `1` is deleted, the next product can receive ID `3`:

```text
2    Sony ZV-E10 Mark II
3    Macbook Air
```

The database does not need to reuse ID `1`.

This taught me that database IDs are **identifiers**, not row numbers that must remain continuous.

---

### 8. Create vs Update IDs

For a CREATE request, I should normally allow the database to generate the ID:

```json
{
    "name": "Macbook Air",
    "price": 165000
}
```

For an UPDATE request, the ID identifies the existing resource:

```text
PUT /products/3
```

with:

```json
{
    "name": "Macbook Air M4",
    "price": 170000
}
```

The distinction is:

```text
CREATE
POST /products
        ↓
Database generates ID


UPDATE
PUT /products/3
        ↓
Update existing product with ID 3
```

---

## 💡 Key Takeaways

- UPDATE should operate on the existing persistent entity.
- `findById()` can be used to verify that a product exists.
- A managed entity can be modified directly and Hibernate can detect the changes through dirty checking.
- `@Transactional` provides the transaction boundary for the update operation.
- `delete(product)` works with the JPA entity lifecycle.
- `@DeleteMapping` maps HTTP DELETE requests to the service layer.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` allows the database to generate IDs.
- Deleted auto-increment IDs do not necessarily get reused.
- IDs should be treated as identifiers, not continuous numbering.
- CREATE and UPDATE use IDs differently.

---

## 🧠 Mental Model

```text
                    CARTIFY CRUD

CREATE
POST /products
      ↓
New Product
      ↓
Repository.save()
      ↓
MySQL generates ID


READ
GET /products/{id}
      ↓
Repository.findById()
      ↓
Product


UPDATE
PUT /products/{id}
      ↓
Find existing entity
      ↓
Managed Entity
      ↓
Modify fields
      ↓
Dirty Checking
      ↓
UPDATE


DELETE
DELETE /products/{id}
      ↓
Find existing entity
      ↓
Managed Entity
      ↓
delete(product)
      ↓
REMOVED
      ↓
DELETE
```

---

## ⚙️ Internal Working

For an update:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
@Transactional
     ↓
Repository.findById()
     ↓
Persistence Context
     ↓
Managed Entity
     ↓
setName() / setPrice()
     ↓
Dirty Checking
     ↓
Flush
     ↓
MySQL
```

For a delete:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Repository.findById()
     ↓
Managed Entity
     ↓
delete(entity)
     ↓
REMOVED state
     ↓
Flush
     ↓
MySQL DELETE
```

---

## ❌ Common Mistakes

### 1. Updating the request object instead of the existing entity

The request body represents the new values. The existing entity represents the persistent record.

The safer pattern is:

```java
Product existingProduct = productRepository.findById(id)
        .orElseThrow(...);

existingProduct.setName(updatedProduct.getName());
existingProduct.setPrice(updatedProduct.getPrice());
```

---

### 2. Assuming `save()` is always required after an update

When the entity is already managed inside a transaction, changing its fields is enough for dirty checking to detect the changes.

---

### 3. Expecting deleted IDs to be reused

Deleting ID `1` does not mean the next inserted record must receive ID `1`.

With auto-generated IDs, gaps are normal.

---

### 4. Providing an ID during normal creation

With:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

the database should normally generate the ID during creation.

---

## 💼 Interview Takeaways

### What happens when a managed entity is modified?

Hibernate uses **dirty checking** to detect changes and generates the appropriate SQL during flush.

### Is `save()` always required after modifying a managed entity?

No. A managed entity inside a transaction can be updated automatically through dirty checking.

### Why can auto-increment IDs have gaps?

Because deleted IDs are not necessarily reused. The generated ID is an identifier, not a row number.

### What is the typical flow for deleting an entity?

```text
Find Entity
    ↓
Verify it exists
    ↓
Delete Entity
    ↓
Flush / Commit
```

---

## 📝 Code Written Today

### Update Service

```java
@Transactional
public Product updateProduct(Long id, Product updatedProduct) {

    Product existingProduct = productRepository.findById(id)
            .orElseThrow(() ->
                    new ProductNotFoundException("Product not found."));

    existingProduct.setName(updatedProduct.getName());
    existingProduct.setPrice(updatedProduct.getPrice());

    return existingProduct;
}
```

### Update Controller

```java
@PutMapping("/{id}")
public Product updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody Product product) {

    return productService.updateProduct(id, product);
}
```

### Delete Service

```java
public void deleteProduct(Long id) {

    if (id == null) {
        throw new RuntimeException("Product ID cannot be null.");
    }

    Product product = productRepository.findById(id)
            .orElseThrow(() ->
                    new ProductNotFoundException("Product not found."));

    productRepository.delete(product);
}
```

### Delete Controller

```java
@DeleteMapping("/{id}")
public void deleteProductById(@PathVariable Long id) {
    productService.deleteProduct(id);
}
```

---

## 💭 Reflection

Today I moved from understanding JPA concepts theoretically to using them in actual CRUD operations.

The most important connection for me was between **managed entities, dirty checking, transactions, and database updates**.

I also learned that database-generated IDs are not meant to be continuous. Deleting a record can leave a gap in the IDs, and that is completely normal.

The practical CRUD flow is now much clearer:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
MySQL
```

---

## 🚀 Next Goal

Before moving forward, I want to understand **Spring Data JPA Query Methods**.

I will learn how Spring Data can derive database queries directly from repository method names, for example:

```java
List<Product> findByName(String name);
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
JPA
     ↓
Hibernate
     ↓
JDBC / HikariCP
     ↓
MySQL

        +

JPA Entity Lifecycle
        ↓
NEW → MANAGED → REMOVED
        ↓
Dirty Checking
        ↓
SQL
```

This connects the concepts I learned in the previous JPA lifecycle lessons with real CRUD operations in my Cartify backend.
