# SmartDialer — Safety-First Predictive Auto-Dialing Platform

Demo Link : https://smartdailer.vercel.app/

> **Predictive intelligence recommends how aggressively to dial. The Safety Controller decides what is actually safe.**

SmartDialer is a safety-first predictive auto-dialing platform for collections/contact-center workflows. It combines historical call data, predictive pacing, deterministic safety controls, atomic resource reservation, provider-health monitoring, event-driven state management, idempotency, concurrency protection, and worker-crash recovery.

## 1. Problem

Auto-dialers must balance agent utilization with safety. Aggressive dialing can create over-dialing, while conservative dialing leaves agents idle. Provider failures, duplicate webhooks, out-of-order events, concurrent workers, and worker crashes create additional reliability risks.

SmartDialer addresses these through:

```text
Historical Data
      ↓
Predictive Pacing
      ↓
Safety Controller
      ↓
Atomic Resource Reservation
      ↓
Provider Dialing
      ↓
Event Processing
      ↓
State Validation
      ↓
Recovery
```

## 2. Core Principle

```text
Prediction ≠ Permission
```

The Predictive Pacing Engine recommends a dial volume. The Safety Controller has final authority.

Example:

```text
Prediction = 101 calls
        ↓
Safety Controller
        ↓
Safe approved volume = 40 calls
```

This prevents an aggressive prediction from becoming an unsafe action.

## 3. Technology Stack

- **Frontend:** React, TypeScript, Vite, Tailwind CSS
- **Backend:** Java, Spring Boot, Spring Data JPA, REST APIs
- **Database:** PostgreSQL target runtime; H2 PostgreSQL-compatible fallback for local validation
- **Data:** Provided 30K collections dataset / `calls.csv`
- **Build:** Maven, npm
- **Infrastructure:** Docker / Docker Compose
- **Version Control:** Git / GitHub

## 4. Architecture

```text
                    ┌──────────────────────────┐
                    │   30K Historical Dataset │
                    │        calls.csv         │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Data Ingestion Service    │
                    │ Parse & derive metrics    │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Historical Analytics      │
                    │ Answer Rate / Talk Time   │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Predictive Pacing Engine  │
                    │ Recommended Dial Volume   │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ SAFETY CONTROLLER         │
                    │ Final Safety Decision      │
                    └──────┬─────────┬─────────┘
                           │         │
                           ↓         ↓
                 Agent Capacity   Provider Health
                           │         │
                           └────┬────┘
                                ↓
                    ┌──────────────────────────┐
                    │ Atomic Call Allocator     │
                    │ Reserve Resources         │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Provider Adapter          │
                    │ Provider A / B            │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Provider Events           │
                    │ RINGING / ANSWERED        │
                    │ COMPLETED / FAILED        │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Event Processor            │
                    │ Idempotency + Ordering    │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Call State Machine        │
                    │ Valid Transitions         │
                    └────────────┬─────────────┘
                                 ↓
                    ┌──────────────────────────┐
                    │ Persistent Database       │
                    └────────────┬─────────────┘
                                 ↑
                    ┌────────────┴─────────────┐
                    │ Recovery Service          │
                    │ Stale Call Detection      │
                    └──────────────────────────┘
```

## 5. Historical Dataset Pipeline

The supplied collections dataset is used instead of a hardcoded answer rate.

```text
collections_30k_dataset
          ↓
       calls.csv
          ↓
     CSV Parsing
          ↓
 Historical Records
          ↓
 Answer Rate + Talk Time
          ↓
 Historical Analytics
          ↓
 Predictive Pacing
```

`DataIngestionService` parses the dataset during backend startup and derives historical metrics used by the pacing pipeline.

## 6. Predictive Pacing

The engine uses historical behavior and current operating conditions to calculate a recommended call volume.

```text
Historical Answer Rate
          +
Current Agent Capacity
          +
Current Call Conditions
          ↓
Predictive Pacing
          ↓
Recommended Call Volume
```

The recommendation remains advisory because the Safety Controller validates the final safe volume.

## 7. Safety Controller

The Safety Controller is the final gate before dialing.

It considers:

- Available agent capacity
- Active calls
- Maximum safe limits
- Provider health
- Current system conditions
- Predictive recommendation

Possible decisions:

```text
APPROVE
REDUCE
REJECT
```

Provider behavior:

```text
HEALTHY  → normal safe pacing
DEGRADED → conservative pacing
DOWN     → outbound dialing rejected
```

## 8. Progressive Dialing

The core ordering is:

```text
Reserve Agent
      ↓
Reserve Borrower
      ↓
Create Call
      ↓
Initiate Dial
```

The system does not blindly dial before confirming resource availability.

## 9. Atomic Reservations & Concurrency

Agent and borrower reservations are concurrency-sensitive. Atomic database updates ensure that multiple workers cannot successfully reserve the same resource.

Conceptually:

```sql
UPDATE agents
SET status = 'RESERVED'
WHERE id = ?
AND status = 'AVAILABLE';
```

Expected behavior:

```text
Worker 1 ──┐
           ├──> Same Agent
Worker 2 ──┘
              ↓
       Atomic Reservation
              ↓
       One success only
```

This protects against double reservation and unsafe duplicate dialing.

## 10. Call State Machine

Normal lifecycle:

```text
QUEUED
   ↓
RESERVED
   ↓
INITIATED
   ↓
RINGING
   ↓
ANSWERED
   ↓
CONNECTED
   ↓
COMPLETED
```

Failure paths may terminate safely:

```text
INITIATED → FAILED
RINGING   → FAILED
```

Invalid transitions such as `QUEUED → ANSWERED` are rejected. After a terminal state such as `COMPLETED`, stale events cannot resurrect the call.

## 11. Idempotency

Provider webhooks can be delivered more than once.

```text
Event ID = EVT-1001
        ↓
First event  → Processed
Duplicate    → Ignored
```

This prevents duplicate state updates, repeated side effects, double counting, and call-state corruption.

## 12. Out-of-Order Protection

Events are validated against the current state before being applied.

```text
Illegal event
      ↓
Transition validation
      ↓
Rejected
      ↓
Existing state preserved
```

This protects the database from stale or incorrectly ordered provider events.

## 13. Worker Crash Recovery

If a worker crashes while a call is active:

```text
Agent     = RESERVED
Borrower  = RESERVED
Call      = INITIATED
Worker    = CRASHED
```

The scheduled `RecoveryService` detects stale work:

```text
Scheduled Recovery
       ↓
Find stale calls
       ↓
Check timeout
       ↓
Mark call FAILED
       ↓
Release Agent
       ↓
Release Borrower
```

This prevents resources from remaining locked indefinitely.

## 14. Provider Health

Provider health is directly connected to the safety decision.

```text
Provider A = DOWN
        ↓
Safety Controller
        ↓
REJECT
        ↓
Approved calls = 0
```

When the provider becomes healthy again, normal pacing can resume.

## 15. Simulation Lab

The frontend contains controlled scenarios for demonstrating reliability.

### Scenario A — Duplicate Events

```text
Same Event ID
      ↓
Idempotency Check
      ↓
Duplicate ignored
```

### Scenario B — Out-of-Order Events

```text
Invalid event sequence
      ↓
State Machine
      ↓
Transition rejected
```

### Scenario C — Concurrency

```text
Many concurrent workers
          ↓
Same resource
          ↓
Atomic reservation
          ↓
One successful reservation
```

### Scenario D — Worker Crash

```text
Active call
    ↓
Worker failure
    ↓
Recovery Service
    ↓
Call FAILED
    ↓
Resources released
```

### Provider Outage

```text
Provider HEALTHY
       ↓
Provider DOWN
       ↓
Safety Controller
       ↓
Dialing blocked
```

## 16. Frontend

The React application provides six primary pages:

1. **Dashboard** — system health, available agents, active calls, historical answer rate, utilization.
2. **Predictive Pacing** — recommendations and approved safe pacing.
3. **Safety Controller** — limits, provider health, decisions, reasoning.
4. **Providers** — provider state and outage simulation.
5. **Simulation Lab** — Scenarios A–D and provider outage.
6. **Historical Analytics** — dataset-derived answer rate, talk time, and historical trends.

## 17. Three Real-World User Journeys

### Operations/Admin

```text
Dashboard
   ↓
Agents
   ↓
Providers
   ↓
System Health
   ↓
Historical Metrics
```

### Dialer Operations

```text
Run Pacing Cycle
       ↓
Predictive Recommendation
       ↓
Safety Controller
       ↓
Approved Dial Volume
       ↓
Call Allocation
       ↓
Call State Changes
```

Example:

```text
Recommended = 101
Approved    = 40
```

### QA/Safety

The QA journey validates:

```text
Duplicate Events
Out-of-Order Events
Concurrency
Worker Crash
Provider Outage
```

## 18. End-to-End Testing

Testing followed the complete chain:

```text
User Action
    ↓
React Frontend
    ↓
REST API
    ↓
Spring Boot
    ↓
Business Logic
    ↓
Safety Controller
    ↓
Database / State
    ↓
Related UI Impact
```

The application was not considered successful merely because the UI rendered; system-level effects were checked.

## 19. Test Results

### Backend

```text
Tests run: 13
Failures: 0
Errors:   0
Skipped:  0

BUILD SUCCESS
```

Coverage includes:

- Concurrency
- Idempotency
- Load testing
- Predictive pacing
- Recovery
- Safety Controller
- Application context

### Frontend

```text
Vite production build
1859 modules transformed
Build successful
```

## 20. Load Testing

Concurrent workloads were executed for:

```text
100
1,000
10,000
```

The tests verified concurrency behavior and exposed the Spring Data JPA/database connection bottleneck at higher load while preserving the atomic reservation guarantee.

## 21. Final PASS/FAIL Matrix

| Feature | Result | Verification |
|---|---|---|
| Dashboard | PASS | Backend-driven metrics |
| Historical Analytics | PASS | Dataset-derived metrics |
| Predictive Pacing | PASS | Historical metrics drive recommendation |
| Safety Controller | PASS | Recommendation constrained by safe capacity |
| Progressive Dialing | PASS | Resources reserved before dialing |
| Agent Reservation | PASS | Atomic reservation |
| Borrower Reservation | PASS | Concurrency protection |
| Duplicate Events | PASS | Idempotency |
| Out-of-Order Events | PASS | State transition validation |
| Concurrency | PASS | No double reservation |
| Worker Recovery | PASS | Stale calls recovered |
| Provider Outage | PASS | Dialing blocked when provider is DOWN |
| Simulation A–D | PASS | Scenarios executable from UI |
| Frontend Build | PASS | Production build successful |
| Backend Tests | PASS | 13 tests, 0 failures |
| Documentation | PASS | Architecture and state-machine documentation |

## 22. What We Overcame

### Over-Dialing

```text
Prediction
    ↓
Safety Controller
    ↓
Hard Capacity Limits
```

### Double Reservation

```text
Atomic Database Reservation
```

### Duplicate Webhooks

```text
Event ID Idempotency
```

### Invalid Event Ordering

```text
Strict State Machine
```

### Provider Failure

```text
Provider Health
      ↓
Safety Decision
```

### Worker Failure

```text
Scheduled Recovery Service
```

### Lack of Historical Context

```text
30K Dataset
     ↓
Data Ingestion
     ↓
Historical Metrics
```

## 23. Key Design Principles

1. **Safety Before Optimization** — never optimize by violating operational safety.
2. **Prediction Is Advisory** — predictive output recommends; deterministic controls authorize.
3. **Reserve Before Dial** — resources must be safely reserved before initiating a call.
4. **Validate State** — every event must pass transition validation.
5. **Idempotent Events** — repeated events must not create repeated side effects.
6. **Recover Failures** — worker failures must not permanently lock resources.
7. **Provider-Aware Safety** — provider degradation or outage must influence dialing decisions.

## 24. Project Structure

```text
SmartDialer/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── pom.xml
│   └── mvnw
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.*
│
├── docs/
│   ├── architecture-overview.md
│   ├── architecture-decisions.md
│   ├── agent-state-machine.md
│   ├── call-state-machine.md
│   └── audit-report.md
│
├── data/
│   └── collections_30k_dataset/
│
├── docker-compose.yml
└── README.md
```

## 25. Running the Application

### Backend — Windows

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Backend — Linux/macOS

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Open the development URL displayed by Vite.

## 26. Running Tests

### Backend

```powershell
cd backend
.\mvnw.cmd test
```

### Frontend

```bash
cd frontend
npm run build
```

## 27. Recommended Demo Flow

### Step 1 — Dashboard

Show system health, available agents, active calls, historical answer rate, and utilization.

### Step 2 — Historical Analytics

Explain:

```text
Provided 30K Dataset
        ↓
Data Ingestion
        ↓
Historical Answer Rate
        ↓
Predictive Pacing Input
```

### Step 3 — Predictive Pacing

Run a pacing cycle and show the recommendation.

```text
Recommended = 101
```

### Step 4 — Safety Controller

Show:

```text
Recommended = 101
Approved    = 40
```

Key explanation:

> **The model recommends; the Safety Controller decides.**

### Step 5 — Provider Outage

Set:

```text
Provider A → DOWN
```

Run pacing again.

Expected:

```text
Approved = 0
```

### Step 6 — Simulation Lab

Demonstrate:

```text
Duplicate Event
Out-of-Order Event
Concurrency
Worker Crash
```

### Step 7 — Recovery

Show that stale calls are failed safely and reserved resources are released.

## 28. Final System Flow

```text
Historical Dataset
        ↓
Data Ingestion
        ↓
Historical Analytics
        ↓
Predictive Pacing
        ↓
Safety Controller ← Provider Health
        ↓
Atomic Allocation ← Agent / Borrower
        ↓
Provider Dialing
        ↓
Provider Events
        ↓
Idempotency Check
        ↓
State Transition Validation
        ↓
Persistent State
        ↑
Recovery Service
```

## 29. Final Result

SmartDialer combines:

```text
REAL HISTORICAL DATA
        +
PREDICTIVE INTELLIGENCE
        +
DETERMINISTIC SAFETY
        +
ATOMIC CONCURRENCY
        +
EVENT IDEMPOTENCY
        +
STATE VALIDATION
        +
FAILURE RECOVERY
        +
PROVIDER HEALTH
        +
SIMULATION
        +
END-TO-END TESTING
```

The result is a system where optimization is always constrained by safety.

## 30. Final Verdict

**SmartDialer is demo and submission ready.**

The application has been validated across the frontend, backend, database interactions, historical dataset ingestion, predictive pacing, safety controls, agent/borrower reservation, concurrency, duplicate events, out-of-order events, provider outages, worker recovery, simulation scenarios, and automated tests.

The central architectural guarantee is:

> **The Predictive Engine recommends how aggressively to dial, but the Safety Controller decides what is actually safe.**

---

**SmartDialer — Predict intelligently. Dial safely. Recover reliably.**
