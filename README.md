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

## Running locally

### Prerequisites

- JDK 21
- A PostgreSQL database — either a local instance, a managed one (Neon/RDS/etc.), or
  the bundled `docker-compose.yml` (see below)
- A Stripe account (test mode is fine) if you want payment creation to actually work;
  any placeholder value lets the app start, but payment calls will fail

### Environment variables

Configuration is driven entirely by environment variables (see
`src/main/resources/application.yml`). The project uses
[`spring-dotenv`](https://github.com/paulschwarz/spring-dotenv), which loads a `.env`
file **from the project root** (`./.env`) if present — not from `src/`. Create one there,
or export the variables another way (shell, IDE run config, Docker env file, etc.):

| Variable | Example | Notes |
|---|---|---|
| `SPRING_APPLICATION_NAME` | `GreenshopApplication` | |
| `SERVER_PORT` | `8080` | |
| `API_PREFIX` | `/api/v1` | |
| `JWT_SECRET_KEY` | *(random Base64 string)* | HMAC signing key, keep secret |
| `JWT_EXPIRATION` | `3600000` | Access token TTL, ms |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh token TTL, ms |
| `STRIPE_API_KEY` | `sk_test_...` | Stripe secret key |
| `STRIPE_PUBLIC_KEY` | `pk_test_...` | Stripe publishable key |
| `DB_URL` | `jdbc:postgresql://localhost:5433/greenshop_db` | port `5433` matches the bundled `docker-compose.yml` |
| `DB_USERNAME` | `greenshop` | also used as `POSTGRES_USER` by `docker-compose.yml` |
| `DB_PASSWORD` | *(your password)* | also used as `POSTGRES_PASSWORD` by `docker-compose.yml` |
| `DB_DRIVER` | `org.postgresql.Driver` | |

Never commit a real `.env` file — it's already covered by `.gitignore`.

### Database via Docker Compose

If you don't already have a local Postgres, `docker-compose.yml` spins one up using the
same `DB_USERNAME`/`DB_PASSWORD` from your `.env` (Docker Compose loads `./.env`
automatically):

```bash
docker compose up -d
```

The container is configured (via `PGPORT`) to listen on `5433` instead of the Postgres
default `5432`, which is why the `DB_URL` example above uses `5433`. Data is not
persisted across `docker compose down -v`.

### Run

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:${SERVER_PORT}`. On first run, Hibernate creates the
schema automatically (`spring.jpa.hibernate.ddl-auto=update`).

### Explore the API

Once running, open:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Raw OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Known limitations

This is a learning/portfolio project and intentionally still has gaps on the roadmap:
no Docker/CI setup yet, limited automated tests, and some authorization checks that are
being hardened incrementally.
