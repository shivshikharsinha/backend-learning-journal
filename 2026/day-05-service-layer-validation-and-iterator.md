# Day 5 - Service Layer Design, Validation & Iterator

**Date:** 04th August 2026

---

## 🎯 Goal

Complete the Service layer by implementing business logic, understand object validation vs business validation, and learn how Java Iterators work internally.

---

## ✅ What I Learned

### Completing the Service Layer

Implemented the remaining methods in `ProductService`:

- `createProduct()`
- `deleteProduct()`

The Service is now responsible for:

- Validating incoming objects.
- Enforcing business rules.
- Delegating data access to the Repository.

---

### Guard Clauses

Instead of writing nested `if-else` statements, I learned to fail early.

Example:

```java
if (product == null) {
    throw new RuntimeException("Product cannot be null.");
}

if (productId == null) {
    throw new RuntimeException("Product ID cannot be null.");
}
```

This makes methods easier to read and avoids unnecessary nesting.

---

### Object Validation

Object validation checks whether the incoming object itself is valid.

Examples:

- Product object is not null.
- Product ID is not null.
- Product name is not blank.
- Product price is not negative.

These validations do **not** require the Repository.

---

### Business Validation

Business validation checks rules against existing data.

Example:

```java
if (productRepository.findById(productId).isPresent()) {
    throw new RuntimeException("Product ID already exists.");
}
```

The Repository is required because existing data must be checked.

---

### Reusing Business Logic

Instead of duplicating validation code, I reused existing service methods.

Example:

```java
getProductById(id);

productRepository.deleteById(id);
```

`getProductById()` already verifies that the product exists, so there is no need to repeat the same logic.

This follows the **Don't Repeat Yourself (DRY)** principle.

---

### Choosing Return Types

Different return types communicate different intentions.

| Return Type | Meaning |
|-------------|---------|
| `Product` | Operation succeeded and returns the created/found object. |
| `Optional<Product>` | The object may or may not exist. |
| `boolean` | Only a yes/no answer is required. |
| `void` | The operation succeeds or throws an exception. |

The return type is part of the method's design, not just its syntax.

---

### Java Iterator

Learned how Java safely traverses collections using an `Iterator`.

Important methods:

- `hasNext()`
- `next()`
- `remove()`

`Iterator.remove()` removes the **last element returned by `next()`** from the original collection.

---

### Why for-each Cannot Remove Elements

A `for-each` loop internally uses an `Iterator`.

Calling:

```java
products.remove(product);
```

while iterating changes the collection without informing the iterator, causing:

```
ConcurrentModificationException
```

The safe approach is:

```java
iterator.remove();
```

or, in modern Java,

```java
products.removeIf(product -> product.getId().equals(id));
```

---

## 💡 Key Takeaways

- Service methods should validate before performing operations.
- Separate Object Validation from Business Validation.
- Reuse existing service methods instead of duplicating logic.
- Use Guard Clauses to keep methods clean and readable.
- Return types should reflect the method's responsibility.
- `Iterator.remove()` safely removes elements during iteration.
- `removeIf()` is the preferred approach in modern Java for conditional removal.

---

## 🧠 Mental Model

```
Incoming Request

        │

        ▼

Object Validation

        │

        ▼

Business Validation

        │

        ▼

Repository Operation

        │

        ▼

Return Result
```

---

## ⚙️ Internal Working

```
deleteProduct(id)

        │

        ▼

Validate ID

        │

        ▼

getProductById(id)

        │

(Product Exists?)

        │

        ▼

Repository.deleteById(id)

        │

        ▼

Success
```

---

## ❌ Common Mistakes

- Returning `null` instead of `Optional`.
- Writing one large validation `if` instead of Guard Clauses.
- Duplicating business validation logic.
- Using `products.remove()` inside a `for-each` loop.
- Confusing List indexes with Product IDs.
- Returning `boolean` when an exception communicates failure more clearly.

---

## 💼 Interview Takeaways

- What is the difference between Object Validation and Business Validation?
- What are Guard Clauses?
- Why should Services contain business logic?
- Why is `Iterator.remove()` safe?
- Why does `ConcurrentModificationException` occur?
- Explain `Optional` and when to use it.
- How do you decide a method's return type?

---

## 📝 Code Written Today

Implemented:

- `createProduct()`
- `deleteProduct()`

Added:

- Object Validation
- Business Validation
- Guard Clauses

Learned:

- Iterator
- `Iterator.remove()`
- `removeIf()`
- Method return type design

---

## 💭 Reflection

Today's lesson helped me understand that writing backend code is more than making APIs work. A good Service method validates input, enforces business rules, avoids duplicate logic, and delegates persistence to the Repository.

I also learned that choosing an appropriate return type is part of API design and that understanding Java's `Iterator` makes modern collection methods like `removeIf()` much easier to understand.

---

## 🚀 Next Goal

- Complete `ProductRepository.deleteById()`.
- Connect the Controller with the Service.
- Build complete CRUD APIs.
- Learn proper HTTP status codes.
- Create custom exceptions.
- Introduce Global Exception Handling using `@ControllerAdvice`.

---

## 🧩 Connect the Dots

```
Spring Container

        │

        ▼

Dependency Injection

        │

        ▼

Controller

        │

        ▼

Service

        │
   Object Validation
        │
   Business Validation
        │

        ▼

Repository

        │

        ▼

Java Collections

        │

        ▼

Iterator / removeIf()

        │

        ▼

Data Updated
```