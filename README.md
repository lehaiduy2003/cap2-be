# SafeNestly Backend

Backend API for **SafeNestly** — a platform that helps people find rental rooms and compatible roommates. Owners can list properties, renters can search and request viewings or rentals, and an AI service powers roommate matching.

Built with **Spring Boot 3** and **Java 17**.

## Features

- **Authentication & authorization** — JWT-based auth with role-based access (`OWNER`, `RENTER`, `ADMIN`)
- **Room management** — Create, update, search, and filter listings with image uploads and geo coordinates
- **Rent workflow** — Rent requests, rent history, and view (tour) requests with status tracking
- **Roommate finder** — Roommate profiles and AI-powered recommendations via an external AI service
- **Real-time messaging** — WebSocket (STOMP) chat between users
- **Notifications** — In-app notifications for rent, view, and report events
- **Maps integration** — Google Maps for location search, markers, and distance calculations
- **Identity verification** — Citizen ID verification through FPT.AI
- **Reporting** — User reports on listings with admin handling
- **MCP tools** — Public endpoints for AI agents / RAG services to fetch owner and property data
- **API documentation** — OpenAPI/Swagger UI with JWT bearer auth support

## Tech Stack

| Layer      | Technology                        |
| ---------- | --------------------------------- |
| Framework  | Spring Boot 3.4.3                 |
| Language   | Java 17                           |
| Database   | MySQL 8                           |
| Migrations | Flyway                            |
| Security   | Spring Security, JWT (jjwt)       |
| Real-time  | Spring WebSocket (STOMP + SockJS) |
| Mapping    | MapStruct                         |
| Docs       | SpringDoc OpenAPI                 |
| Build      | Gradle                            |
| Testing    | JUnit 5, Spring Boot Test, JaCoCo |

## Prerequisites

- Java 17+
- Docker & Docker Compose (for local MySQL)
- API keys (as needed):
  - Google Maps API key
  - FPT.AI API key
  - External AI service for roommate recommendations (default: `http://localhost:8000`)

## Getting Started

### 1. Clone the repository

```bash
git clone git@github.com:lehaiduy2003/cap2-be.git
cd cap2-be
```

### 2. Start MySQL

```bash
docker compose up -d
```

This starts MySQL 8 on port `3306` with database `SafeNestly` (root password: `root`).

### 3. Configure environment

Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

Create a profile-specific properties file (these are gitignored):

**`src/main/resources/application-dev.properties`**

```properties
spring.datasource.username=root
spring.datasource.password=root
```

Set environment variables (via `.env` or your shell):

| Variable              | Description                     | Example                                  |
| --------------------- | ------------------------------- | ---------------------------------------- |
| `DB_URL`              | MySQL JDBC URL                  | `jdbc:mysql://localhost:3306/SafeNestly` |
| `ENV_PROFILE`         | Active Spring profile           | `dev`                                    |
| `GOOGLE_MAPS_API_KEY` | Google Maps API key             | —                                        |
| `FPT_AI_API_KEY`      | FPT.AI API key                  | —                                        |
| `AI_SERVICE_URL`      | Roommate recommendation service | `http://localhost:8000`                  |

### 4. Run the application

```bash
./gradlew bootRun
```

The server starts on port **8080** by default.

Database schema is applied automatically via **Flyway** migrations in `src/main/resources/db/migration/`.

### 5. Run with Docker

Build and run the full backend image:

```bash
docker build -t SafeNestly-be .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/SafeNestly \
  -e ENV_PROFILE=dev \
  SafeNestly-be
```

## API Documentation

With the app running, open:

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Protected endpoints require a JWT in the `Authorization` header:

```
Authorization: Bearer <token>
```

Obtain a token via `POST /auth/login` or `POST /auth/register`.

## Main API Endpoints

| Area          | Base path                          | Description                                   |
| ------------- | ---------------------------------- | --------------------------------------------- |
| Auth          | `/auth`                            | Register, login, refresh token                |
| Users         | `/users`, `/owner`, `/renterowner` | Profiles, verification, admin user management |
| Rooms         | `/api/rooms`                       | CRUD, search/filter, owner listings           |
| Rent requests | `/api/rent-requests`               | Submit and manage rental requests             |
| Rent history  | `/api/rent-histories`              | Rental history records                        |
| View requests | `/api/view-requests`               | Schedule and respond to room viewings         |
| Roommates     | `/api/roommates`                   | Roommate profiles and AI recommendations      |
| Messages      | `/messages`, `/conversations`      | Chat and conversation summaries               |
| Notifications | `/api/notifications`               | User notifications                            |
| Reports       | `/api/reports`                     | Report listings and admin actions             |
| Maps          | `/maps`                            | Location search and map markers               |
| MCP tools     | `/api/mcp/tools`                   | AI agent integration endpoints                |
| WebSocket     | `/api/socket`, `/ws`               | Real-time messaging (STOMP)                   |
| Health        | `/health`, `/actuator`             | Health and actuator endpoints                 |

Uploaded room images are served at `/images/**`.

## WebSocket

The frontend connects via STOMP over SockJS:

- Endpoint: `/api/socket` (allowed origins include `http://localhost:5173` and `https://cap2-fe.vercel.app`)
- App prefix: `/app`
- Broker topics: `/topic`, `/chatroom`, `/user`

Pass the username in the `username` STOMP header on connect.

## Testing

Run the test suite:

```bash
./gradlew test
```

Generate a JaCoCo coverage report:

```bash
./gradlew test jacocoTestReport
```

Reports are written to `build/reports/jacoco/test/html/index.html`.

Tests use an in-memory H2 database with Flyway disabled.

## Project Structure

```
src/main/java/com/c1se_01/SafeNestly/
├── config/          # Security, WebSocket, CORS, OpenAPI
├── controller/      # REST and WebSocket endpoints
├── dto/             # Request/response objects
├── enums/           # Domain enums (Role, RoomStatus, etc.)
├── exception/       # Custom exceptions and global handler
├── mapper/          # MapStruct mappers
├── model/           # JPA entities
├── repository/      # Spring Data repositories
├── service/         # Business logic
└── utils/           # Helpers and validators
```

## Related Projects

- Frontend: [cap2-fe](https://cap2-fe.vercel.app) (Vite + React)

## License

This project was developed as part of a capstone (C1SE) course project.
