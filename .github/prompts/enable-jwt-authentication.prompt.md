---
name: enable-jwt-authentication
description: Use this prompt to enable JWT authentication in a Spring Boot application. It will guide you through the necessary steps, including adding dependencies, configuring security settings, and implementing JWT token generation and validation.
---

## Enable JWT Authentication in Spring Boot Application

- Add the necessary dependencies for JWT in your `pom.xml` or `build.gradle` file. For Maven, include:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```
- Enable authentication to all endpoints except for login, registration, and public endpoints. You can do this by configuring the `WebSecurityConfigurerAdapter` class.