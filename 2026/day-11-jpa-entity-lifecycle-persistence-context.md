# Day 11 - JPA Entity Lifecycle & Persistence Context

**Date:** 13th September 2026

---

## 🎯 Goal

Understand the JPA entity lifecycle, Persistence Context, dirty checking, transactions, and how Spring Data JPA's `save()` relates to `persist()` and `merge()`.

---

## ✅ What I Learned

### JPA Entity States

JPA entities can exist in four important states:

- NEW
- MANAGED
- DETACHED
- REMOVED

These states describe the relationship between a Java entity and the Persistence Context.

---

### NEW Entity

A newly created entity that is not associated with the Persistence Context is in the NEW state.

Example:

```java
Product product = new Product(null, "Camera", 75000.0);
```

At this point:

```text
Java Object
    ↓
NEW
    ↓
Not managed by Persistence Context
```

---

### MANAGED Entity

A managed entity is associated with the Persistence Context and is tracked by Hibernate.

Example:

```java
Product product = productRepository.findById(1L)
        .orElseThrow();
```

Conceptually:

```text
Product
   ↓
Persistence Context
   ↓
MANAGED
```

Because Hibernate is tracking the entity, changes to its fields can be detected through dirty checking.

---

### DETACHED Entity

A detached entity was previously managed but is no longer associated with the current Persistence Context.

The Java object still exists and the database row still exists, but Hibernate is no longer tracking that object.

Conceptually:

```text
MANAGED
   ↓
Persistence Context ends
   ↓
DETACHED
```

Changing a detached entity does not automatically cause an UPDATE.

```java
Product product = ...; // detached

product.setPrice(160000.0);
```

The Java object changes, but Hibernate is not automatically tracking that change.

---

### REMOVED Entity

A managed entity can be marked for deletion using `remove()`.

```java
entityManager.remove(product);
```

Conceptually:

```text
MANAGED
   ↓
remove()
   ↓
REMOVED
   ↓
flush()
   ↓
DELETE
```

REMOVED does not necessarily mean that the SQL DELETE executes immediately.

The entity is marked for deletion and the DELETE can be issued during flush.

---

### Entity Lifecycle

The overall lifecycle can be visualized as:

```text
NEW
 │
 │ persist/save
 ▼
MANAGED
 │
 ├─────────────────────┐
 │                     │
 │ remove()            │ Persistence Context ends
 ▼                     ▼
REMOVED             DETACHED
 │                     │
 │ flush               │ merge()
 ▼                     ▼
DELETE              MANAGED
```

The important distinction is:

- NEW → Hibernate does not manage the entity.
- MANAGED → Hibernate tracks the entity.
- DETACHED → Hibernate no longer tracks the entity.
- REMOVED → A managed entity has been marked for deletion.

---

### Dirty Checking

Dirty checking is the mechanism Hibernate uses to detect changes made to managed entities.

Example:

```java
@Transactional
public void updatePrice(Long id) {

    Product product = productRepository.findById(id)
            .orElseThrow();

    product.setPrice(160000.0);
}
```

Conceptually:

```text
Original state
price = 165000

        ↓

Current state
price = 160000

        ↓

Hibernate detects the difference

        ↓

Dirty Checking

        ↓

UPDATE during flush
```

Hibernate can generate an UPDATE such as:

```sql
UPDATE products
SET price = 160000
WHERE id = 1;
```

An explicit `save()` is not fundamentally required for this managed entity update in the shown transactional scenario.

---

### Flush vs Commit

These are two different concepts.

**Dirty Checking**

Detects that a managed entity has changed.

**Flush**

Synchronizes the Persistence Context with the database and can cause SQL to execute.

**Commit**

Completes the transaction.

The flow is:

```text
Java changes
    ↓
Dirty Checking
    ↓
Flush
    ↓
SQL
    ↓
Commit
```

Important:

> Flush and commit are not the same operation.

A flush can happen before transaction completion, and a transaction can still ultimately roll back.

---

### `@Transactional`

`@Transactional` establishes a transactional context for a method.

Example:

```java
@Transactional
public void updatePrice(Long id) {

    Product product = productRepository.findById(id)
            .orElseThrow();

    product.setPrice(160000.0);
}
```

Conceptual flow:

```text
@Transactional
    ↓
Transaction starts
    ↓
findById()
    ↓
Managed Product
    ↓
setPrice()
    ↓
Dirty Checking
    ↓
Flush
    ↓
UPDATE
    ↓
Commit
```

`@Transactional` does not mean every Java statement immediately becomes SQL.

---

### `save()` vs `persist()` vs `merge()`

Spring Data JPA provides:

```java
productRepository.save(product);
```

The behavior depends on whether the entity is new or existing.

For a new entity:

```text
NEW
 ↓
save()
 ↓
persist()
 ↓
MANAGED
 ↓
flush()
 ↓
INSERT
```

For an existing or detached entity:

```text
DETACHED
   ↓
save()
   ↓
merge()
   ↓
MANAGED instance
   ↓
flush()
   ↓
UPDATE
```

A very important point about `merge()`:

```java
Product managed = entityManager.merge(detached);
```

The original detached object does not become managed.

`merge()` returns a managed instance.

```text
detachedProduct → remains DETACHED

merge(detachedProduct)
        ↓
managedProduct → MANAGED
```

---

### Persistence Context

The Persistence Context is the environment in which JPA keeps track of managed entities.

Conceptually:

```text
MySQL
  ↓
Hibernate
  ↓
Persistence Context
  ↓
Product Object
```

The Persistence Context is central to:

- Entity management
- Dirty checking
- First-level caching
- Maintaining entity identity within the context

---

### First-Level Cache

The Persistence Context also acts as Hibernate/JPA's first-level cache.

Consider:

```java
@Transactional
public void test() {

    Product p1 = productRepository.findById(1L)
            .orElseThrow();

    Product p2 = productRepository.findById(1L)
            .orElseThrow();
}
```

Within the same Persistence Context, JPA maintains an identity guarantee for the entity.

Conceptually:

```text
First find
    ↓
Persistence Context
    ↓
Product #1

Second find
    ↓
Persistence Context
    ↓
Existing Product #1
```

Therefore:

```java
p1 == p2
```

can be `true` within the same Persistence Context.

---

### Persistence Context and Dirty Checking

Consider:

```java
Product p1 = productRepository.findById(1L)
        .orElseThrow();

p1.setPrice(160000.0);

Product p2 = productRepository.findById(1L)
        .orElseThrow();
```

Because `p1` is already managed and Product ID 1 is already present in the Persistence Context, the second lookup can return the same managed instance.

Therefore:

```java
p2.getPrice()
```

can return:

```text
160000.0
```

even before the transaction has completed.

---

## 💡 Key Takeaways

- JPA entities have four important states: NEW, MANAGED, DETACHED, and REMOVED.
- A MANAGED entity is tracked by the Persistence Context.
- Dirty checking detects changes to managed entities.
- Dirty checking does not itself execute SQL.
- Flush synchronizes Persistence Context changes with the database.
- Commit completes the transaction.
- `save()` does not simply mean INSERT.
- New entities can be handled using `persist()`.
- Existing or detached entities can be handled using `merge()`.
- `merge()` returns a managed instance; the original detached object remains detached.
- The Persistence Context acts as the first-level cache.
- Within one Persistence Context, the same entity identity maps to the same managed entity instance.
- `@Transactional` provides the transactional context in which these lifecycle and dirty-checking behaviors occur.

---

## 🧠 Mental Model

```text
Java Object
     ↓
JPA Entity
     ↓
Persistence Context
     ↓
MANAGED
     ↓
Dirty Checking
     ↓
Flush
     ↓
SQL
     ↓
Transaction Commit
     ↓
MySQL
```

Entity states:

```text
NEW
 │
 │ persist/save
 ▼
MANAGED
 │
 ├───────────────┐
 │               │
 │ remove()      │ Persistence Context ends
 ▼               ▼
REMOVED        DETACHED
 │               │
 │ flush         │ merge()
 ▼               ▼
DELETE         MANAGED
```

---

## ⚙️ Internal Working

```text
@Transactional method starts

↓

Transaction / Persistence Context

↓

findById()

↓

Hibernate checks Persistence Context

↓

If entity is already managed
    ↓
Reuse managed entity

Otherwise
    ↓
Load entity from database
    ↓
Put entity into Persistence Context

↓

Entity is MANAGED

↓

Java field changes

↓

Hibernate performs Dirty Checking

↓

Flush

↓

Hibernate generates SQL

↓

Transaction Commit

↓

Database updated
```

---

## ❌ Common Mistakes

- Thinking `save()` always means INSERT.
- Thinking `setPrice()` immediately executes SQL.
- Confusing dirty checking with flush.
- Confusing flush with commit.
- Thinking DETACHED means deleted.
- Thinking REMOVED means the DELETE SQL has necessarily executed immediately.
- Assuming a detached entity is still automatically tracked.
- Thinking `merge()` makes the original detached object managed.
- Assuming different Persistence Contexts must contain the same Java object instance.
- Going too deep into Hibernate internals before understanding the practical JPA lifecycle.

---

## 💼 Interview Takeaways

- What are the four JPA entity states?
- What is a Persistence Context?
- What is dirty checking?
- What is the difference between flush and commit?
- Why can Hibernate update a managed entity without calling `save()`?
- What is the difference between a managed and detached entity?
- What happens when `remove()` is called?
- What is the difference between `persist()` and `merge()`?
- Does `merge()` make the original object managed?
- What is the first-level cache?
- Why can two `findById()` calls return the same entity instance within one Persistence Context?
- What role does `@Transactional` play in JPA?

---

## 📝 Code Written Today

- Worked with JPA entity lifecycle concepts.
- Used `@Transactional` in update examples.
- Used `findById()` to obtain managed entities.
- Used `setPrice()` to demonstrate dirty checking.
- Explored `entityManager.remove()` for the REMOVED state.
- Explored `persist()` and `merge()` conceptually.
- Explored the Persistence Context and first-level cache.
- Compared managed and detached entities.

---

## 💭 Reflection

Today helped connect several JPA concepts that initially looked like separate features.

I learned that Hibernate is not simply executing SQL every time I call a repository method. The Persistence Context keeps track of managed entities, dirty checking detects changes, and flush synchronizes those changes with the database.

The biggest takeaway was understanding the difference between **MANAGED** and **DETACHED** entities and how that affects whether Hibernate can automatically detect changes.

I also understood why `@Transactional` is important when working with JPA and why `save()` is not synonymous with an immediate SQL INSERT or UPDATE.

I don't need to memorize Hibernate internals at this stage. The goal is to understand the practical lifecycle well enough to reason about what Spring Data JPA is doing behind the scenes.

---

## 🚀 Next Goal

- Continue Sprint 2 practical MySQL/JPA development.
- Build proper CRUD operations using `JpaRepository`.
- Implement Product update functionality.
- Implement Product delete functionality.
- Explore Spring Data JPA query methods.
- Complete the remaining practical parts of Sprint 2.

---

## 🧩 Connect the Dots

```text
Java

↓

Spring Boot

↓

Spring Data JPA

↓

JPA

↓

Hibernate

↓

Persistence Context

↓

Managed Entity

↓

Dirty Checking

↓

Flush

↓

SQL

↓

JDBC

↓

MySQL
```
