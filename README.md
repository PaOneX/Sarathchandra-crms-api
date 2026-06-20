# Car Rental Management System API

Spring Boot REST API for a car rental platform with JWT authentication, role-based access, MySQL + Flyway, Supabase/local file storage, WhatsApp booking notifications, and Stripe Checkout payments.

## Requirements

- Java 17+
- MySQL 8+
- Maven 3.9+ (or use `./mvnw`)
- Stripe account (for payments)
- Optional: Supabase project (file storage), WhatsApp Cloud API, Google OAuth client

## Quick Start (Local Development)

1. Copy environment template:

```bash
cp .env.example .env
```

2. Fill in `.env` values (minimum: `DB_PASSWORD`, `JWT_SECRET`).

3. Create the database:

```sql
CREATE DATABASE car_rental_db;
```

4. Optional: seed demo cars for local testing:

```bash
mysql -u root -p car_rental_db < scripts/seed-dev-cars.sql
```

5. Run with the dev profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

API: `http://localhost:8081`  
Health: `http://localhost:8081/actuator/health`

## Configuration

Configuration is split by profile:

| File | Purpose |
|------|---------|
| `application.properties` | Shared defaults, env-var placeholders (no secrets) |
| `application-dev.properties` | Local dev: DEBUG logging, admin seeder enabled |
| `application-prod.properties` | Production: INFO logging, admin seeder disabled |

See [`.env.example`](.env.example) for all environment variables.

### Important variables

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | JDBC MySQL URL |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials |
| `JWT_SECRET` | HS256 signing key (min 256 bits recommended) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend URLs |
| `STRIPE_SECRET_KEY` | Stripe secret key |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret |
| `STRIPE_SUCCESS_URL` / `STRIPE_CANCEL_URL` | Redirect URLs after checkout |
| `SUPABASE_URL` / `SUPABASE_SERVICE_ROLE_KEY` | Optional cloud storage |
| `ADMIN_SEED_ENABLED` | Set `false` in production |

**Rotate any secrets that were previously committed to version control.**

## Cloud Deployment

Works on Render, Railway, AWS, and similar platforms.

1. Set `SPRING_PROFILES_ACTIVE=prod`
2. Set `PORT` (most cloud hosts inject this automatically)
3. Configure all required env vars from `.env.example`
4. Point health check to `/actuator/health`
5. Use a managed MySQL instance; set `DATABASE_URL` with SSL enabled
6. Set `CORS_ALLOWED_ORIGINS` to your production frontend URL(s)
7. Disable admin seeder: `ADMIN_SEED_ENABLED=false`

### Stripe webhook

1. In Stripe Dashboard → Developers → Webhooks, add endpoint:
   `https://your-api-domain.com/api/webhooks/stripe`
2. Subscribe to `checkout.session.completed`
3. Copy the signing secret into `STRIPE_WEBHOOK_SECRET`

## Payment Flow (Stripe)

1. Customer creates a booking (`POST /api/bookings`)
2. Admin approves booking (`PATCH /api/bookings/{id}/status` → `APPROVED`)
3. Customer starts checkout (`POST /api/payments/checkout` with `{ "bookingId": 1 }`)
4. API returns `{ checkoutUrl, sessionId, paymentId }`
5. Redirect customer to `checkoutUrl`
6. Stripe sends webhook → payment and booking marked `COMPLETED`

## API Overview

| Controller | Base path | Notes |
|------------|-----------|-------|
| Auth | `/api/auth` | Register, login, Google OAuth |
| Cars | `/api/cars` | Public browse; admin CRUD |
| Bookings | `/api/bookings` | Customer + admin flows |
| Payments | `/api/payments` | Stripe checkout |
| Users | `/api/users` | Profile and admin user management |
| Fleet | `/api/fleet` | Fleet manager car status |
| Reports | `/api/reports` | Admin analytics |
| Webhooks | `/api/webhooks/stripe` | Stripe only (public, signature verified) |

See [FRONTEND_GUIDE.md](FRONTEND_GUIDE.md) for full frontend integration details.

## Testing

```bash
./mvnw clean test
```

Tests use the `test` profile with an in-memory H2 database.

## Project Structure

```
src/main/java/com/icet/carrental/
  controller/     REST endpoints
  service/        Business logic
  repository/     JDBC data access
  config/         Security, CORS, Stripe, storage
  security/       JWT filter and utilities
  db/migration/   Flyway SQL migrations
scripts/
  seed-dev-cars.sql   Optional local demo data (not applied in production migrations)
```

## License

Internal / educational use — adjust as needed for your organization.
