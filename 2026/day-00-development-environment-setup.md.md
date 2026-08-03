# Day 0 - Development Environment Setup

**Date:** 28th July 2026

---

## 🎯 Goal

Prepare a modern Java backend development environment for building production-ready Spring Boot applications.

---

## ✅ What I Set Up

- Installed **Java 25 LTS**
- Configured **JAVA_HOME**
- Installed **Apache Maven**
- Installed **IntelliJ IDEA Community Edition**
- Created a GitHub learning journal repository
- Installed Git and configured GitHub
- Generated my first Spring Boot project using Spring Initializr
- Imported the project into IntelliJ
- Verified Maven dependencies were downloaded successfully
- Successfully ran my first Spring Boot application

---

## 📚 What I Learned

### Java Version

- Chose **Java 25 LTS** as the primary development version.
- Learned why using an LTS version is recommended for long-term projects.

---

### Maven

- Maven is a build automation and dependency management tool.
- Instead of downloading libraries manually, Maven downloads and manages them automatically through `pom.xml`.

Example:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

---

### Spring Initializr

Learned how Spring Initializr generates a production-ready Spring Boot project with the required project structure and dependencies.

---

### Project Structure

```
cartify-backend/

├── src/
│   ├── main/
│   └── test/
│
├── pom.xml
│
└── mvnw
```

---

### GitHub Learning Journal

Created a GitHub repository to document my backend engineering journey.

Repository Structure:

```
backend-learning-journal/

├── journal/
├── notes/
├── interview-questions/
├── mistakes/
├── resources/
└── README.md
```

---

## 💡 Key Takeaways

- Java is the programming language.
- Maven manages project dependencies and builds the application.
- Spring Initializr bootstraps a Spring Boot project.
- IntelliJ IDEA provides development and debugging tools.
- Git tracks code changes.
- GitHub stores code remotely and documents learning.

---

## 🧠 Mental Model

```
Developer

        │

        ▼

Spring Initializr

        │

        ▼

Spring Boot Project

        │

        ▼

IntelliJ

        │

        ▼

Maven

        │

        ▼

Downloads Dependencies

        │

        ▼

Run Application
```

---

## ❌ Common Mistakes

- Using a non-LTS Java version without understanding compatibility.
- Forgetting to configure JAVA_HOME.
- Manually downloading libraries instead of using Maven.
- Editing generated Maven files without understanding their purpose.

---

## 💼 Interview Takeaways

- What is Maven?
- What is Spring Initializr?
- What is an LTS version of Java?
- What is `pom.xml`?
- Why use IntelliJ IDEA for Spring Boot development?

---

## 💭 Reflection

Today wasn't about writing code—it was about building the foundation for backend development. I understood the purpose of each tool in the development ecosystem and created a structured GitHub learning journal to document my progress throughout this journey.

---

## 🚀 Next Goal

- Understand Spring Framework.
- Learn IoC and Dependency Injection.
- Explore the Spring Container and ApplicationContext.
