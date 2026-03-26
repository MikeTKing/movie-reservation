# 🎬 Movie Reservation System

A production-ready REST API built with **Java 21 + Spring Boot 3** for browsing movies, scheduling showtimes, and reserving seats — with role-based access control, overbooking prevention, and admin reporting.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Data Model](#data-model)
- [API Reference](#api-reference)
- [Overbooking Prevention](#overbooking-prevention)
- [Quick Start (Docker)](#quick-start-docker)
- [Running Locally](#running-locally)
- [Running Tests](#running-tests)
- [Seed Credentials](#seed-credentials)
- [Project Structure](#project-structure)

---

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Language       | Java 21                             |
| Framework      | Spring Boot 3.2                     |
| Security       | Spring Security + JWT (jjwt 0.12)   |
| Persistence    | Spring Data JPA + Hibernate         |
| Database       | PostgreSQL 16                       |
| Migrations     | Flyway                              |
| Validation     | Jakarta Bean Validation             |
| Documentation  | SpringDoc OpenAPI (Swagger UI)      |
| Build          | Maven 3.9                           |
| Containerisation | Docker + Docker Compose           |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                      HTTP Clients                       │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│              JwtAuthenticationFilter                    │
│   Extracts & validates Bearer token on every request   │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                     Controllers                         │
│  AuthController  MovieController  ShowtimeController   │
│  ReservationController  AdminController  HallController│
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                      Services                           │
│  AuthService  MovieService  ShowtimeService            │
│  ReservationService  ReportingService  GenreService    │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                    Repositories                         │
│  (Spring Data JPA — custom JPQL queries for complex    │
│   reporting, seat availability, overlap detection)     │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                  PostgreSQL 16                          │
│  Flyway migrations  •  UNIQUE constraints              │
│  EXCLUDE USING gist (hall overlap prevention)          │
└─────────────────────────────────────────────────────────┘
```

---

## Data Model

```
users
  id, name, email, password, role (USER|ADMIN)

genres
  id, name

movies
  id, title, description, poster_url, duration_min, rating

movie_genres  (M:N join)
  movie_id → movies, genre_id → genres

halls
  id, name, total_rows, total_cols

seats
  id, hall_id → halls, row_num, col_num, label

showtimes
  id, movie_id → movies, hall_id → halls
  start_time, end_time, price
  [EXCLUDE USING gist prevents hall double-booking]

reservations
  id, user_id → users, showtime_id → showtimes
  status (CONFIRMED|CANCELLED), total_price

reservation_seats
  id, reservation_id, seat_id, showtime_id
  [UNIQUE (seat_id, showtime_id) prevents overbooking]
```

---

## API Reference

Interactive Swagger UI is available at `http://localhost:8080/swagger-ui.html` once the app is running.

### Authentication — `/api/auth`

| Method | Path                        | Auth      | Description                  |
|--------|-----------------------------|-----------|------------------------------|
| POST   | `/api/auth/signup`          | Public    | Register a new user          |
| POST   | `/api/auth/login`           | Public    | Login and receive JWT token  |
| PATCH  | `/api/auth/users/{id}/promote` | Admin  | Promote user to ADMIN        |

### Movies — `/api/movies`

| Method | Path                     | Auth      | Description                          |
|--------|--------------------------|-----------|--------------------------------------|
| GET    | `/api/movies`            | Public    | List all movies (paginated, filterable by title) |
| GET    | `/api/movies/{id}`       | Public    | Get movie details                    |
| GET    | `/api/movies/genre/{genre}` | Public | Filter movies by genre              |
| GET    | `/api/movies/playing?date=YYYY-MM-DD` | Public | Movies with showtimes on a date |
| POST   | `/api/movies`            | Admin     | Create a movie                       |
| PATCH  | `/api/movies/{id}`       | Admin     | Update a movie                       |
| DELETE | `/api/movies/{id}`       | Admin     | Delete a movie                       |

### Showtimes — `/api/showtimes`

| Method | Path                              | Auth   | Description                    |
|--------|-----------------------------------|--------|--------------------------------|
| GET    | `/api/showtimes?date=YYYY-MM-DD`  | Public | All showtimes on a date        |
| GET    | `/api/showtimes/{id}`             | Public | Showtime details + capacity    |
| GET    | `/api/showtimes/movie/{movieId}?date=` | Public | Showtimes for a movie    |
| GET    | `/api/showtimes/{id}/seat-map`    | Public | Full seat availability map     |
| POST   | `/api/showtimes`                  | Admin  | Schedule a new showtime        |
| PATCH  | `/api/showtimes/{id}`             | Admin  | Update time / price            |
| DELETE | `/api/showtimes/{id}`             | Admin  | Delete a showtime              |

### Reservations — `/api/reservations`

| Method | Path                           | Auth | Description                  |
|--------|--------------------------------|------|------------------------------|
| POST   | `/api/reservations`            | User | Reserve seats                |
| GET    | `/api/reservations/my`         | User | My reservations (paginated)  |
| GET    | `/api/reservations/my/{id}`    | User | Get one of my reservations   |
| PATCH  | `/api/reservations/my/{id}/cancel` | User | Cancel upcoming reservation |

### Admin — `/api/admin`

| Method | Path                          | Auth  | Description                         |
|--------|-------------------------------|-------|-------------------------------------|
| GET    | `/api/admin/reservations`     | Admin | All reservations (paginated)        |
| GET    | `/api/admin/dashboard`        | Admin | Totals: revenue, capacity, counts   |
| GET    | `/api/admin/reports/revenue`  | Admin | Revenue breakdown per movie         |
| GET    | `/api/admin/reports/capacity` | Admin | Occupancy report per showtime       |
| GET    | `/api/admin/users`            | Admin | List all users                      |

### Halls & Genres — `/api/halls`, `/api/genres`

| Method | Path              | Auth   | Description           |
|--------|-------------------|--------|-----------------------|
| GET    | `/api/halls`      | Public | List all halls        |
| GET    | `/api/halls/{id}` | Public | Hall details          |
| GET    | `/api/genres`     | Public | List all genres       |
| POST   | `/api/genres`     | Admin  | Create a genre        |
| DELETE | `/api/genres/{id}` | Admin | Delete a genre       |

---

## Overbooking Prevention

The system uses a **two-layer defence** strategy:

### Layer 1 — Application-level check (fast path)
Before inserting any `reservation_seats` rows, `ReservationService` calls:
```java
boolean hasConflict = reservationSeatRepository.existsConflict(showtimeId, seatIds);
```
This JPQL query counts CONFIRMED `reservation_seats` rows matching the requested seat IDs.
If any conflict is found, a `ConflictException` is thrown immediately — no DB write needed.

### Layer 2 — Database UNIQUE constraint (safety net)
The `reservation_seats` table has:
```sql
UNIQUE (seat_id, showtime_id)
```
Even if two concurrent requests pass the application check simultaneously (race condition),
only one INSERT batch will succeed. The other triggers a `DataIntegrityViolationException`,
which Spring translates into a `ConflictException` via the global exception handler.

### Layer 3 — Hall scheduling (showtime overlap)
The `showtimes` table has a PostgreSQL exclusion constraint:
```sql
EXCLUDE USING gist (
    hall_id WITH =,
    tsrange(start_time, end_time) WITH &&
)
```
This prevents scheduling two movies in the same hall with overlapping time ranges, even under concurrency.

### Transaction isolation
All reservation writes run inside a `@Transactional` method using Spring's default
READ_COMMITTED isolation. The DB-level UNIQUE constraint is the final concurrency guard.

---

## Quick Start (Docker)

```bash
# Clone and start
git clone <repo-url>
cd movie-reservation
docker compose up --build

# API is now live at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 16 running locally

### Steps

```bash
# 1. Create the database
psql -U postgres -c "CREATE DATABASE moviedb;"

# 2. Configure environment (or edit application.yml)
export DB_URL=jdbc:postgresql://localhost:5432/moviedb
export DB_USERNAME=postgres
export DB_PASSWORD=password
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# 3. Run — Flyway migrations and seed data run automatically
mvn spring-boot:run
```

---

## Running Tests

Tests use an **in-memory H2 database** — no PostgreSQL needed.

```bash
mvn test
```

Test coverage includes:
- `AuthControllerTest` — signup, login, duplicate email, invalid input
- `ReservationServiceTest` — booking success, seat conflicts, partial overlap, cancellation, duplicate seat IDs
- `ShowtimeServiceTest` — scheduling, overlap detection, back-to-back showtimes

---

## Seed Credentials

Flyway migration `V2__Seed_Data.sql` inserts these on first startup:

| Role  | Email                  | Password   |
|-------|------------------------|------------|
| Admin | admin@movieapp.com     | Admin@123  |
| User  | john@example.com       | User@123   |

Three halls (Hall A: 120 seats, Hall B: 80 seats, Hall C: 48 seats), three sample movies, and four upcoming showtimes are also seeded.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/moviereservation/
│   │   ├── MovieReservationApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java        # JWT filter chain, role-based rules
│   │   │   └── OpenApiConfig.java         # Swagger/OpenAPI setup
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── MovieController.java
│   │   │   ├── ShowtimeController.java
│   │   │   ├── ReservationController.java
│   │   │   ├── AdminController.java
│   │   │   ├── HallController.java
│   │   │   └── GenreController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── MovieService.java
│   │   │   ├── ShowtimeService.java
│   │   │   ├── ReservationService.java    # Core booking logic
│   │   │   ├── GenreService.java
│   │   │   ├── ReportingService.java
│   │   │   └── impl/
│   │   │       └── UserDetailsServiceImpl.java
│   │   ├── entity/
│   │   │   ├── User.java                  # implements UserDetails
│   │   │   ├── Role.java
│   │   │   ├── Movie.java
│   │   │   ├── Genre.java
│   │   │   ├── Hall.java
│   │   │   ├── Seat.java
│   │   │   ├── Showtime.java
│   │   │   ├── Reservation.java
│   │   │   ├── ReservationSeat.java
│   │   │   └── ReservationStatus.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── MovieRepository.java
│   │   │   ├── GenreRepository.java
│   │   │   ├── HallRepository.java
│   │   │   ├── SeatRepository.java
│   │   │   ├── ShowtimeRepository.java
│   │   │   ├── ReservationRepository.java
│   │   │   └── ReservationSeatRepository.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── AuthRequests.java
│   │   │   │   ├── MovieRequests.java
│   │   │   │   ├── ShowtimeRequests.java
│   │   │   │   └── ReservationRequests.java
│   │   │   └── response/
│   │   │       ├── Responses.java
│   │   │       ├── AdminDashboardResponse.java
│   │   │       ├── MovieRevenueReport.java
│   │   │       └── ShowtimeCapacityReport.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── exception/
│   │       ├── Exceptions.java
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__Initial_Schema.sql     # Tables + constraints
│           └── V2__Seed_Data.sql          # Genres, halls, seats, users, movies
└── test/
    ├── java/com/moviereservation/
    │   ├── controller/
    │   │   └── AuthControllerTest.java
    │   └── service/
    │       ├── ReservationServiceTest.java
    │       └── ShowtimeServiceTest.java
    └── resources/
        └── application.yml                # H2 in-memory config
```
