# Day 2 - Spring Boot Startup & Building My First REST API

**Date:** 30th July 2026

---

## 🎯 Goal

Understand how a Spring Boot application starts internally and build my first REST API.

---

## ✅ What I Learned

### Maven

Maven is a build automation and dependency management tool.

Instead of manually downloading JAR files, Maven downloads and manages project dependencies using the `pom.xml` file.

Example:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

---

### Spring Boot Startup

When the application starts:

1. The JVM executes the `main()` method.
2. `SpringApplication.run()` creates the ApplicationContext.
3. Component Scanning discovers Spring Beans.
4. Dependencies are injected.
5. Spring Boot performs Auto Configuration.
6. Embedded Tomcat starts.
7. The application is ready to receive HTTP requests.

---

### Auto Configuration

Spring Boot automatically configures commonly used components based on the dependencies present in the project.

For example, adding:

```xml
spring-boot-starter-webmvc
```

automatically configures:

- Embedded Tomcat
- Spring MVC
- DispatcherServlet
- Jackson

without writing configuration manually.

---

### Embedded Tomcat

Unlike traditional Java web applications, Spring Boot includes its own web server.

Running the application automatically starts Tomcat on port **8080**.

---

### DispatcherServlet

Every HTTP request first reaches the DispatcherServlet.

It acts as the **Front Controller** and is responsible for:

- Receiving every request
- Finding the correct controller
- Invoking the controller method
- Returning the response

---

### Building My First REST API

Created my first controller using:

```java
@RestController
@RequestMapping("/products")
public class ProductController {

}
```

Implemented:

- `@GetMapping`
- `@PostMapping`

---

### Request Mapping

`@RequestMapping` defines the common URL for a controller.

Example:

```java
@RequestMapping("/products")
```

Combined with:

```java
@GetMapping
```

creates:

```
GET /products
```

---

### JSON Serialization

When a controller returns a Java object:

```java
return new Product(...);
```

Spring uses Jackson to convert it into JSON before sending it to the client.

```
Java Object

↓

Jackson

↓

JSON
```

---

### JSON Deserialization

When a client sends JSON:

```json
{
    "id":1,
    "name":"iPhone",
    "price":99999
}
```

`@RequestBody` tells Spring to use Jackson to convert the JSON into a Java object.

```
JSON

↓

Jackson

↓

Java Object
```

---

## 💡 Key Takeaways

- Maven manages project dependencies automatically.
- Spring Boot configures many components through Auto Configuration.
- Embedded Tomcat eliminates the need for an external web server.
- DispatcherServlet is the entry point for every HTTP request.
- `@RestController` returns JSON responses by default.
- Jackson handles both serialization and deserialization.
- `@RequestBody` reads the HTTP request body and converts JSON into Java objects.

---

## 🧠 Mental Model

```
Browser

        │

        ▼

Tomcat

        │

        ▼

DispatcherServlet

        │

        ▼

Controller

        │

        ▼

Jackson

        │

        ▼

JSON Response
```

---

## ❌ Common Mistakes

- Thinking Spring directly handles HTTP requests instead of Tomcat.
- Assuming DispatcherServlet calls methods randomly.
- Forgetting `@RequestBody` in POST APIs.
- Believing Spring itself converts JSON instead of Jackson.
- Confusing `@Controller` with `@RestController`.

---

## 💼 Interview Takeaways

- What happens when a Spring Boot application starts?
- What is Auto Configuration?
- What is Embedded Tomcat?
- What is DispatcherServlet?
- Difference between `@Controller` and `@RestController`.
- Explain Serialization and Deserialization.
- What is the purpose of `@RequestBody`?

---

## 📝 Code Written Today

- Created the first Spring Boot project.
- Built the first REST Controller.
- Implemented GET and POST endpoints.
- Successfully tested APIs using Postman.
- Returned Java objects as JSON.
- Accepted JSON request bodies using `@RequestBody`.

---

## 💭 Reflection

Today I learned that Spring Boot is much more than a framework for creating APIs. I understood the complete startup process—from the JVM executing the `main()` method to Tomcat starting, the DispatcherServlet routing requests, and Jackson converting Java objects to JSON. This helped me understand what actually happens behind the scenes whenever an API is called.

---

## 🚀 Next Goal

- Understand Dependency Injection.
- Learn Constructor Injection.
- Build a layered architecture using Controller, Service, and Repository.
- Implement an in-memory Repository.

---

## 🧩 Connect the Dots

```
Java

↓

SpringApplication.run()

↓

ApplicationContext

↓

Component Scan

↓

Beans Created

↓

Tomcat Starts

↓

DispatcherServlet

↓

Controller

↓

Jackson

↓

JSON Response
```