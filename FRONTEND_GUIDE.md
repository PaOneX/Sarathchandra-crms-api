# Car Rental Management System — Frontend Build Guide

This document is the single source of truth for building a **separate frontend repository** that connects to this Spring Boot API.

**Backend repo:** `Car_Rental_Managemnt_System_api`  
**Base URL (local):** `http://localhost:8080`  
**API prefix:** `/api`

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [Architecture Overview](#2-architecture-overview)
3. [CORS (Required Backend Change)](#3-cors-required-backend-change)
4. [Authentication](#4-authentication)
5. [API Response Format](#5-api-response-format)
6. [Error Handling](#6-error-handling)
7. [Roles & Route Access](#7-roles--route-access)
8. [TypeScript Types](#8-typescript-types)
9. [API Reference](#9-api-reference)
10. [Suggested Frontend Structure](#10-suggested-frontend-structure)
11. [Pages & Features](#11-pages--features)
12. [User Flows](#12-user-flows)
13. [Example API Client](#13-example-api-client)
14. [Environment Variables](#14-environment-variables)
15. [Testing Checklist](#15-testing-checklist)

---

## 1. Quick Start

### Create the frontend repo

```bash
# React + TypeScript (recommended)
npm create vite@latest car-rental-frontend -- --template react-ts
cd car-rental-frontend
npm install react-router-dom axios @tanstack/react-query
npm install -D tailwindcss @tailwindcss/vite
```

### Minimum `.env` for the frontend

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_OAUTH_CLIENT_ID.apps.googleusercontent.com
```

> Use the **same Google Client ID** as `google.client-id` in the backend `application.properties`.

### Run both apps locally

| App      | Command (backend)     | URL                    |
|----------|-----------------------|------------------------|
| Backend  | `./mvnw spring-boot:run` | `http://localhost:8080` |
| Frontend | `npm run dev`         | `http://localhost:5173` |

---

## 2. Architecture Overview

```
┌─────────────────────┐         JWT Bearer          ┌──────────────────────────┐
│   React Frontend    │  ─────────────────────────► │  Spring Boot API :8080   │
│   (separate repo)   │         JSON / REST         │  MySQL + Flyway          │
└─────────────────────┘                             └──────────────────────────┘
         │
         └── Google Sign-In → idToken → POST /api/auth/google
```

- **Stateless JWT auth** — no server sessions; store the token client-side.
- **Role-based access** — UI routes and buttons depend on `role` from login/register/Google auth.
- **Uniform envelope** — every response uses `ApiResponse<T>` (see below).

---

## 3. CORS (Required Backend Change)

The backend **does not have CORS configured yet**. Browsers will block requests from `http://localhost:5173` until you add this to the backend repo.

Add a `CorsConfig.java` (or update `SecurityConfig`):

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "http://localhost:5173",
        "http://localhost:3000"
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

And in `SecurityConfig`:

```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

**Alternative (dev only):** Vite proxy in `vite.config.ts`:

```ts
export default defineConfig({
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
```

Then set `VITE_API_BASE_URL=/api` so requests go through the proxy (no CORS issue in dev).

---

## 4. Authentication

### 4.1 Register

```
POST /api/auth/register
Content-Type: application/json
```

**Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+94771234567"
}
```

**Success (201):** `data` contains `AuthResponse` (see types below).

### 4.2 Login

```
POST /api/auth/login
```

**Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Success (200):** `data` contains `AuthResponse`.

### 4.3 Google Sign-In

```
POST /api/auth/google
```

**Body:**
```json
{
  "idToken": "<Google ID token from client SDK>"
}
```

**Frontend flow:**
1. Use [@react-oauth/google](https://www.npmjs.com/package/@react-oauth/google) or Google Identity Services.
2. On success, send `credential` (ID token) to `/api/auth/google`.
3. Backend verifies token, creates/links user, returns JWT.

```bash
npm install @react-oauth/google
```

```tsx
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';

<GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
  <GoogleLogin
    onSuccess={async (res) => {
      const { data } = await api.post('/auth/google', { idToken: res.credential });
      saveAuth(data.data); // token + user info
    }}
    onError={() => console.error('Google login failed')}
  />
</GoogleOAuthProvider>
```

### 4.4 Using the JWT

All protected endpoints require:

```
Authorization: Bearer <token>
```

- Token expires in **24 hours** (`jwt.expiration=86400000` ms).
- On **401**, redirect to login and clear stored token.
- On **403**, show "Access denied" (wrong role).

**Recommended storage:** `localStorage` or `sessionStorage` for the token; keep `userId`, `name`, `email`, `role` in React context or Zustand.

---

## 5. API Response Format

Every endpoint returns:

```ts
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}
```

**Success example:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "profilePictureUrl": null,
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  }
}
```

**Error example:**
```json
{
  "success": false,
  "message": "Email already registered: john@example.com",
  "data": null
}
```

**Validation error (400):**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

---

## 6. Error Handling

| HTTP Status | Meaning | Frontend action |
|-------------|---------|-----------------|
| 400 | Validation / bad input | Show field errors from `data` map |
| 401 | Invalid credentials / no token | Redirect to login |
| 403 | Wrong role | Show access denied page |
| 404 | Resource not found | Show not-found UI |
| 409 | Conflict (car unavailable, booking conflict) | Show `message` to user |
| 502 | Storage upload failure (Supabase) | Show upload error toast |
| 500 | Server error | Generic error toast |

---

## 7. Roles & Route Access

| Role | Description | Main areas |
|------|-------------|------------|
| `CUSTOMER` | Rents cars | Browse cars, bookings, payments, profile |
| `ADMIN` | Full control | Users, cars, all bookings, reports |
| `FLEET_MANAGER` | Fleet ops | Fleet availability, maintenance, car status |
| `PAYMENT_MANAGER` | Payments | All payments list |

### Public routes (no auth)

| Route | API used |
|-------|----------|
| Home / car catalog | `GET /api/cars` |
| Car detail | `GET /api/cars/{id}` |
| Login / Register | `POST /api/auth/*` |

### Protected routes by role

| Route pattern | Roles |
|---------------|-------|
| `/bookings/my`, `/bookings/new` | `CUSTOMER` |
| `/payments/my` | `CUSTOMER` |
| `/admin/users`, `/admin/bookings`, `/admin/reports` | `ADMIN` |
| `/fleet/*` | `ADMIN`, `FLEET_MANAGER` |
| `/payments` (all) | `ADMIN`, `PAYMENT_MANAGER` |
| `/profile` | Any authenticated user |

---

## 8. TypeScript Types

Copy these into `src/types/api.ts` in your frontend repo.

```ts
// Enums (match backend exactly)
export type UserRole = 'CUSTOMER' | 'ADMIN' | 'FLEET_MANAGER' | 'PAYMENT_MANAGER';
export type CarStatus = 'AVAILABLE' | 'RENTED' | 'UNDER_MAINTENANCE';
export type FuelType = 'PETROL' | 'DIESEL' | 'ELECTRIC' | 'HYBRID';
export type BookingStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface AuthResponse {
  userId: number;
  name: string;
  email: string;
  role: UserRole;
  profilePictureUrl: string | null;
  token: string;
  tokenType: string; // "Bearer"
}

/** Frontend alias for logged-in user state (maps from AuthResponse) */
export type AuthUser = Omit<AuthResponse, 'token' | 'tokenType'>;

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  profilePictureUrl: string | null;
  role: UserRole;
  createdAt: string; // ISO datetime
}

export interface CarResponse {
  id: number;
  brand: string;
  model: string;
  fuelType: FuelType;
  seatingCapacity: number;
  dailyRate: number;
  status: CarStatus;
  year: number | null;
  licensePlate: string | null;
  description: string | null;
  imageUrls: string[];
  createdAt: string;
}

export interface BookingResponse {
  id: number;
  userId: number;
  customerName: string;
  carId: number;
  carBrand: string;
  carModel: string;
  startDate: string; // "YYYY-MM-DD"
  endDate: string;
  totalAmount: number;
  status: BookingStatus;
  createdAt: string;
}

export interface PaymentResponse {
  id: number;
  bookingId: number;
  amount: number;
  paymentMethod: string;
  transactionId: string | null;
  status: PaymentStatus;
  paidAt: string | null;
  createdAt: string;
}

// Request DTOs
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface GoogleAuthRequest {
  idToken: string;
}

export interface CarRequest {
  brand: string;
  model: string;
  fuelType: FuelType;
  seatingCapacity: number;
  dailyRate: number;
  year?: number;
  licensePlate?: string;
  description?: string;
}

export interface BookingRequest {
  carId: number;
  startDate: string; // "YYYY-MM-DD", must be future
  endDate: string;   // must be after startDate
}

export interface PaymentRequest {
  bookingId: number;
  paymentMethod: string;
  transactionId?: string;
}

export interface UpdateCarStatusRequest {
  status: CarStatus;
}

export interface UpdateBookingStatusRequest {
  status: BookingStatus;
}
```

---

## 9. API Reference

### Auth — `/api/auth` (public)

| Method | Path | Body | Response `data` |
|--------|------|------|-----------------|
| POST | `/register` | `RegisterRequest` | `AuthResponse` |
| POST | `/login` | `LoginRequest` | `AuthResponse` |
| POST | `/google` | `{ idToken }` | `AuthResponse` |

---

### Cars — `/api/cars`

| Method | Path | Auth | Query / Body | Response `data` |
|--------|------|------|--------------|-----------------|
| GET | `/` | Public | `?brand=&fuelType=&minPrice=&maxPrice=` | `CarResponse[]` |
| GET | `/{id}` | Public | — | `CarResponse` |
| POST | `/` | ADMIN | `CarRequest` | `CarResponse` |
| PUT | `/{id}` | ADMIN | `CarRequest` | `CarResponse` |
| DELETE | `/{id}` | ADMIN | — | `null` |
| PATCH | `/{id}/status` | ADMIN, FLEET_MANAGER | `{ status }` | `null` |
| POST | `/{id}/images` | ADMIN | `multipart/form-data` field `images` | `CarResponse` |
| DELETE | `/{id}/images/{imageId}` | ADMIN | — | `null` |

**Image upload rules:** JPEG, PNG, or WebP only; max 5 MB per file; max 5 images per car. Images are stored in Supabase Storage; `imageUrls` in `CarResponse` are public URLs.

**Filter examples:**
```
GET /api/cars?brand=Toyota&fuelType=HYBRID&minPrice=50&maxPrice=200
```

---

### Bookings — `/api/bookings`

| Method | Path | Auth | Body | Response `data` |
|--------|------|------|------|-----------------|
| GET | `/` | ADMIN | — | `BookingResponse[]` |
| GET | `/my` | CUSTOMER | — | `BookingResponse[]` |
| GET | `/{id}` | Owner or ADMIN | — | `BookingResponse` |
| POST | `/` | CUSTOMER | `BookingRequest` | `BookingResponse` |
| PUT | `/{id}` | CUSTOMER (own) | `BookingRequest` | `BookingResponse` |
| DELETE | `/{id}` | CUSTOMER (own) | — | `null` (cancel) |
| PATCH | `/{id}/status` | ADMIN | `{ status }` | `BookingResponse` |

**Booking status transitions (admin):** `PENDING` → `APPROVED` | `REJECTED`; customer can `CANCEL`; eventually `COMPLETED`.

---

### Payments — `/api/payments`

| Method | Path | Auth | Body | Response `data` |
|--------|------|------|------|-----------------|
| POST | `/` | CUSTOMER | `PaymentRequest` | `PaymentResponse` |
| GET | `/` | ADMIN, PAYMENT_MANAGER | — | `PaymentResponse[]` |
| GET | `/my` | CUSTOMER | — | `PaymentResponse[]` |
| GET | `/booking/{bookingId}` | Owner or privileged | — | `PaymentResponse` |

**Typical `paymentMethod` values:** `"CARD"`, `"CASH"`, `"BANK_TRANSFER"` (backend accepts any non-blank string).

---

### Users — `/api/users`

| Method | Path | Auth | Body | Response `data` |
|--------|------|------|------|-----------------|
| GET | `/me` | Any | — | `UserResponse` |
| PUT | `/me` | Any | `RegisterRequest` | `UserResponse` |
| POST | `/me/avatar` | Any | `multipart/form-data` field `file` | `UserResponse` |
| GET | `/` | ADMIN | — | `UserResponse[]` |
| GET | `/{id}` | ADMIN | — | `UserResponse` |
| PUT | `/{id}` | ADMIN | `RegisterRequest` | `UserResponse` |
| DELETE | `/{id}` | ADMIN | — | `null` |

> Profile update reuses `RegisterRequest` (name, email, password, phone). Password change is optional on update depending on backend logic.

---

### Fleet — `/api/fleet`

| Method | Path | Auth | Body | Response `data` |
|--------|------|------|------|-----------------|
| GET | `/availability` | ADMIN, FLEET_MANAGER | — | `CarResponse[]` |
| GET | `/maintenance` | ADMIN, FLEET_MANAGER | — | `CarResponse[]` |
| PATCH | `/cars/{id}/status` | ADMIN, FLEET_MANAGER | `{ status }` | `CarResponse` |

---

### Reports — `/api/reports`

| Method | Path | Auth | Query | Response `data` |
|--------|------|------|-------|-----------------|
| GET | `/bookings` | ADMIN | `startDate`, `endDate` (ISO date) | `BookingResponse[]` |
| GET | `/revenue` | ADMIN | `period=DAILY\|WEEKLY\|MONTHLY` | `Record<string, unknown>[]` |
| GET | `/customers` | ADMIN | — | `Record<string, unknown>[]` |
| GET | `/car-utilization` | ADMIN, FLEET_MANAGER | — | `Record<string, unknown>[]` |

**Revenue report item shape:**
```json
{ "period": "2026-06", "revenue": 15000.00, "transactions": 12 }
```

**Customer report item shape:**
```json
{
  "id": 1,
  "name": "John",
  "email": "john@example.com",
  "total_bookings": 5,
  "total_spent": 2500.00,
  "last_booking": "2026-06-10T10:00:00"
}
```

**Car utilization item shape:**
```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Corolla",
  "status": "AVAILABLE",
  "total_bookings": 10,
  "total_revenue": 5000.00,
  "total_days_rented": 45
}
```

---

## 10. Suggested Frontend Structure

```
car-rental-frontend/
├── .env
├── .env.example
├── vite.config.ts
├── package.json
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── types/
    │   └── api.ts
    ├── lib/
    │   ├── api.ts              # axios instance + interceptors
    │   └── auth.ts             # token helpers
    ├── context/
    │   └── AuthContext.tsx
    ├── hooks/
    │   ├── useAuth.ts
    │   ├── useCars.ts
    │   └── useBookings.ts
    ├── components/
    │   ├── layout/
    │   │   ├── Navbar.tsx
    │   │   ├── Sidebar.tsx
    │   │   └── ProtectedRoute.tsx
    │   ├── cars/
    │   │   ├── CarCard.tsx
    │   │   ├── CarFilters.tsx
    │   │   └── CarForm.tsx
    │   ├── bookings/
    │   │   └── BookingForm.tsx
    │   └── ui/
    │       ├── Button.tsx
    │       ├── Modal.tsx
    │       └── StatusBadge.tsx
    ├── pages/
    │   ├── public/
    │   │   ├── HomePage.tsx
    │   │   ├── CarDetailPage.tsx
    │   │   ├── LoginPage.tsx
    │   │   └── RegisterPage.tsx
    │   ├── customer/
    │   │   ├── MyBookingsPage.tsx
    │   │   ├── NewBookingPage.tsx
    │   │   └── MyPaymentsPage.tsx
    │   ├── admin/
    │   │   ├── DashboardPage.tsx
    │   │   ├── UsersPage.tsx
    │   │   ├── CarsManagePage.tsx
    │   │   ├── BookingsManagePage.tsx
    │   │   └── ReportsPage.tsx
    │   ├── fleet/
    │   │   └── FleetDashboardPage.tsx
    │   └── ProfilePage.tsx
    └── routes/
        └── AppRoutes.tsx
```

### Recommended libraries

| Purpose | Library |
|---------|---------|
| Routing | `react-router-dom` v6 |
| HTTP | `axios` |
| Server state | `@tanstack/react-query` |
| Forms | `react-hook-form` + `zod` |
| UI | Tailwind CSS + shadcn/ui (optional) |
| Charts (reports) | `recharts` |
| Google login | `@react-oauth/google` |
| Dates | `date-fns` or `dayjs` |

---

## 11. Pages & Features

### Public

| Page | Features |
|------|----------|
| **Home** | Car grid, filters (brand, fuel, price range), link to detail |
| **Car Detail** | Full car info, "Book now" → login if guest |
| **Login** | Email/password + Google button |
| **Register** | Name, email, password (min 8), phone |

### Customer

| Page | Features |
|------|----------|
| **My Bookings** | List with status badges; cancel pending; view detail |
| **New Booking** | Pick car, date range (future dates), show estimated total |
| **Pay Booking** | Payment form after booking approved |
| **My Payments** | Payment history |
| **Profile** | View/edit name, phone |

### Admin

| Page | Features |
|------|----------|
| **Dashboard** | KPI cards (bookings, revenue, users) |
| **Users** | CRUD table |
| **Cars** | CRUD + status |
| **Bookings** | All bookings; approve/reject/complete |
| **Reports** | Date-range bookings, revenue chart, customer table |

### Fleet Manager

| Page | Features |
|------|----------|
| **Fleet Dashboard** | Available vs maintenance tabs |
| **Update Status** | Set car to AVAILABLE / RENTED / UNDER_MAINTENANCE |

### Payment Manager

| Page | Features |
|------|----------|
| **All Payments** | Filterable payment list |

---

## 12. User Flows

### Customer: Book a car

```
Browse cars (public)
  → Login / Register
  → Select car + dates
  → POST /api/bookings
  → Status: PENDING
  → (Admin approves) Status: APPROVED
  → POST /api/payments { bookingId, paymentMethod }
  → Status: COMPLETED
```

### Admin: Approve booking

```
GET /api/bookings
  → PATCH /api/bookings/{id}/status { "status": "APPROVED" }
```

### Fleet: Mark car under maintenance

```
GET /api/fleet/availability
  → PATCH /api/fleet/cars/{id}/status { "status": "UNDER_MAINTENANCE" }
```

---

## 13. Example API Client

`src/lib/api.ts`:

```ts
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

/** POST multipart/form-data (do not set Content-Type manually — browser adds boundary) */
export async function postFormData<T>(url: string, formData: FormData) {
  const token = localStorage.getItem('token');
  const response = await api.post<T>(url, formData, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
  return response.data;
}
```

`src/services/carService.ts`:

```ts
import { postFormData } from '../lib/api';
import type { ApiResponse, CarResponse } from '../types/api';

export const carService = {
  uploadCarImages: async (carId: number, files: File[]) => {
    const form = new FormData();
    files.forEach((file) => form.append('images', file));
    const res = await postFormData<ApiResponse<CarResponse>>(`/cars/${carId}/images`, form);
    return res.data;
  },
};
```

`src/services/userService.ts`:

```ts
import { postFormData } from '../lib/api';
import type { ApiResponse, UserResponse } from '../types/api';

export const userService = {
  uploadAvatar: async (file: File) => {
    const form = new FormData();
    form.append('file', file);
    const res = await postFormData<ApiResponse<UserResponse>>('/users/me/avatar', form);
    return res.data;
  },
};
```

`src/services/authService.ts`:

```ts
import api from '../lib/api';
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../types/api';

export const authService = {
  register: (body: RegisterRequest) =>
    api.post<ApiResponse<AuthResponse>>('/auth/register', body),

  login: (body: LoginRequest) =>
    api.post<ApiResponse<AuthResponse>>('/auth/login', body),

  google: (idToken: string) =>
    api.post<ApiResponse<AuthResponse>>('/auth/google', { idToken }),
};
```

`src/components/layout/ProtectedRoute.tsx`:

```tsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import type { UserRole } from '../../types/api';

interface Props {
  children: React.ReactNode;
  roles?: UserRole[];
}

export function ProtectedRoute({ children, roles }: Props) {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (roles && user && !roles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}
```

---

## 14. Environment Variables

### Frontend `.env.example`

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
```

### Backend (for reference)

| Property | Value |
|----------|-------|
| `server.port` | `8080` |
| `jwt.expiration` | `86400000` (24h) |
| `google.client-id` | Must match frontend Google Client ID |
| `supabase.url` | Supabase project URL |
| `supabase.service-role-key` | Server-side only — never expose to frontend |
| `supabase.storage.cars` | Bucket for car images (default `car-images`) |
| `supabase.storage.avatars` | Bucket for profile pictures (default `avatars`) |

### Google Cloud Console setup

1. Create OAuth 2.0 Client ID (Web application).
2. **Authorized JavaScript origins:** `http://localhost:5173`
3. Copy Client ID to both frontend `.env` and backend `application.properties`.

---

## 15. Testing Checklist

### Auth
- [ ] Register new customer
- [ ] Login with email/password
- [ ] Google sign-in returns JWT
- [ ] Token attached on protected calls
- [ ] Expired/invalid token → 401 → redirect login

### Customer
- [ ] Browse cars without login (with `description` and `imageUrls`)
- [ ] Upload profile avatar (`POST /api/users/me/avatar`)
- [ ] Filter cars by brand, fuel, price
- [ ] Create booking (future dates only)
- [ ] View/cancel own bookings
- [ ] Pay for approved booking
- [ ] View payment history

### Admin
- [ ] CRUD cars (including `description` field)
- [ ] Upload car images via multipart (`POST /api/cars/{id}/images`)
- [ ] Delete car images (`DELETE /api/cars/{id}/images/{imageId}`)
- [ ] List all users/bookings
- [ ] Approve/reject bookings
- [ ] Revenue & customer reports load

### Fleet Manager
- [ ] View available / maintenance cars
- [ ] Update car status

### Edge cases
- [ ] Book unavailable car → 409
- [ ] Wrong role on admin route → 403
- [ ] Validation errors show per-field messages
- [ ] Date format `YYYY-MM-DD` for bookings and reports

---

## Appendix: Status Badge Colors (UI hint)

| Status | Suggested color |
|--------|-----------------|
| Booking `PENDING` | yellow |
| Booking `APPROVED` | green |
| Booking `REJECTED` / `CANCELLED` | red |
| Booking `COMPLETED` | blue |
| Car `AVAILABLE` | green |
| Car `RENTED` | orange |
| Car `UNDER_MAINTENANCE` | gray |
| Payment `COMPLETED` | green |
| Payment `FAILED` | red |

---

## Next Steps

1. Create the frontend repo and copy this file into it as `README.md` or `docs/API_INTEGRATION.md`.
2. Add **CORS** (or Vite proxy) on the backend before integrating.
3. Implement auth + protected routing first.
4. Build public car catalog, then customer booking flow.
5. Add role-specific admin/fleet dashboards last.

For backend changes or new endpoints, keep this document in sync with the API repo.
