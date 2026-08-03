# Day 1 - Spring Core: IoC, Beans & ApplicationContext

**Date:** 29th July 2026

---

## 🎯 Goal

Understand the core concepts of the Spring Framework and how Spring manages objects.

---

## ✅ What I Learned

### Inversion of Control (IoC)

Instead of creating objects manually using `new`, Spring takes responsibility for creating and managing application objects.

Example:

```java
ProductService service = new ProductService();
```

becomes

```text
Spring Container

↓

Creates ProductService

↓

Provides it whenever required
```

---

### Spring Container

The Spring Container is responsible for:

- Creating objects (Beans)
- Managing their lifecycle
- Injecting dependencies
- Making them available throughout the application

---

### ApplicationContext

The `ApplicationContext` is the most commonly used implementation of the Spring Container.

Think of it as a warehouse that stores all Spring-managed objects.

```
ApplicationContext

├── ProductController
├── ProductService
├── ObjectMapper
├── DispatcherServlet
└── ...
```

---

### Spring Beans

A Bean is simply an object created and managed by Spring.

Spring only creates Beans for classes discovered during Component Scanning.

---

### Component Scanning

Spring scans the project's base package during application startup.

Whenever it finds classes annotated with:

- `@Component`
- `@Service`
- `@Repository`
- `@Controller`
- `@RestController`

it creates Bean instances and stores them inside the ApplicationContext.

---

## 💡 Key Takeaways

- Spring manages application objects called Beans.
- Beans live inside the ApplicationContext.
- The Spring Container creates and manages Beans.
- Component Scanning automatically discovers annotated classes.
- IoC means Spring controls object creation instead of the developer.

---

## 🧠 Mental Model

```
Spring Boot

        │

        ▼

Component Scan

        │

        ▼

ApplicationContext

        │

        ▼

Stores Beans

        │

        ▼

Application Uses Beans
```

---

## ❌ Common Mistakes

- Creating Spring-managed objects using `new`.
- Assuming every class automatically becomes a Bean.
- Confusing Spring Boot with the ApplicationContext.
- Forgetting that only scanned packages are considered.

---

## 💼 Interview Takeaways

- What is IoC?
- What is a Spring Bean?
- What is the Spring Container?
- What is ApplicationContext?
- What is Component Scanning?

---

## 📝 Code Written Today

- Created the first `@Service`.
- Understood how Spring discovers annotated classes.
- Explored the relationship between Beans and the ApplicationContext.

---

## 💭 Reflection

Today I understood that Spring's biggest responsibility is not building REST APIs—it's managing application objects. The concepts of IoC, Beans, and the ApplicationContext helped me understand what Spring is actually doing behind the scenes instead of treating it like magic.

---

## 🚀 Next Goal

- Learn Spring Boot startup sequence.
- Understand Maven dependencies.
- Explore Embedded Tomcat.
- Build the first REST API.

---

## 🧩 Connect the Dots

```
Java

↓

Spring Framework

↓

IoC Container

↓

ApplicationContext

↓

Beans

↓

Dependency Injection (Next)
```