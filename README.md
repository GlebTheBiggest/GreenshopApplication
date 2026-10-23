# Greenshop

An online plant/garden store backend built with Spring Boot. Portfolio project focused on
demonstrating a clean, idiomatic Spring Boot setup: JWT authentication with role-based
access control, a product catalog with categories and images, shopping cart, order
placement, and Stripe payments.

## Tech stack

- **Java 21**, **Spring Boot 3.5** (Web, Data JPA, Security, Validation, Mail)
- **PostgreSQL** via Spring Data JPA / Hibernate
- **JWT** (`jjwt`) for stateless authentication, with access + refresh tokens and
  server-side token revocation
- **Stripe** (`stripe-java`) for payment intents
- **SpringDoc OpenAPI** for interactive API docs (Swagger UI)
- **ModelMapper** for entity/DTO mapping
- **Maven** for builds

## Features

- Registration/login with JWT access + refresh tokens
- Role-based access control: `USER`, `MANAGER`, `ADMIN`, with fine-grained permissions
  (e.g. `management:read`, `admin:delete`) on top of roles
- Product catalog with categories, brand/name search, and image upload/download
- Shopping cart and cart items per user
- Order placement with Stripe payment intent creation
- Management (back-office) endpoints for products, categories, images, orders and users

## API overview

All endpoints are served under the configurable prefix `API_PREFIX` (default `/api/v1`).

| Area | Base path | Who can access |
|---|---|---|
| Auth | `/auth/register`, `/auth/authenticate`, `/auth/refresh-token`, `/auth/logout` | Public |
| Products (storefront) | `/user/products/**` | Public / authenticated user |
| Cart | `/user/carts/**`, `/cartItems/**` | Authenticated user |
| Orders | `/user/orders/**` | Authenticated user |
| Management: products, categories, images, orders, users, carts | `/management/**` | `ADMIN`, `MANAGER` |
| Admin | `/admin/**` | `ADMIN` |

Full, always-up-to-date documentation is available via Swagger UI once the app is running
(see [Running locally](#running-locally)).

## Project layout

```
src/main/java/.../
├── config/      Security, JWT filter, Stripe, ModelMapper/PasswordEncoder beans
├── controller/  REST controllers, split into auth/ user/ manager/ admin/
├── service/     Business logic, one package per domain (interface + impl)
├── model/       JPA entities
├── repository/  Spring Data JPA repositories
├── dto/         Response-facing data shapes
├── request/     Request bodies
├── response/    Response wrappers
├── enums/       Role, Permission, OrderStatus, PaymentStatus, TokenType
└── exceptions/  Custom exceptions
```

## Known limitations

This is a learning/portfolio project and intentionally still has gaps on the roadmap:
no Docker/CI setup yet, limited automated tests, and some authorization checks that are
being hardened incrementally.
