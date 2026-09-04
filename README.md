<<<<<<< HEAD
# SmartDialer Platform

A complete full-stack web application for a collections company that maximizes agent utilization while maintaining the deterministic safety characteristics of progressive dialing.

## Core Objective

The system uses predictive intelligence to recommend how aggressively to dial, but relies on a deterministic **Safety Controller** to make the final decision. The predictive algorithm never directly places a telecom call.

## Architecture

```
Campaign -> Pacing Engine -> Safety Controller -> Call Allocator -> Telecom Provider
```

1. **Pacing Engine:** Recommends call volume (Predictive or Progressive).
2. **Safety Controller:** Validates recommendations against agent capacity, hard limits, and provider health.
3. **Call Allocator:** Handles transaction-safe allocation using DB-level atomic updates.
4. **Telecom Provider:** Mock interfaces simulating reliable and flaky external systems.
5. **Event Processor:** Idempotent event handler for provider webhooks.

## Tech Stack
* **Frontend:** React, TypeScript, Tailwind CSS, Recharts
* **Backend:** Java 21, Spring Boot 3, Spring Data JPA
* **Database:** PostgreSQL
* **Containerization:** Docker Compose

## Concurrency & Idempotency
- **Atomic Reservation:** `UPDATE agents SET status = 'RESERVED' WHERE id = ? AND status = 'AVAILABLE'` prevents duplicate agent assignments.
- **Idempotency Check:** Provider events are logged in `provider_events` with unique constraints on `(providerEventId, callId)`.

## How to Run
1. Ensure Docker Desktop is running.
2. Run `docker-compose up -d` to start PostgreSQL.
3. Start Backend: `cd backend && ./mvnw spring-boot:run`
4. Start Frontend: `cd frontend && npm run dev`
5. Visit `http://localhost:5173`

*(Note: Data is seeded automatically on startup if the database is empty).*
=======
# SmartDialer
>>>>>>> 6c203f8cbb22023c8679d3db0cdbd24807a42ca3
