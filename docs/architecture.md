# SmartDialer Architecture

## Component Overview

1. **Campaign**: The logical grouping of dialing strategies and targets.
2. **Pacing Engine**: Determines how many calls should be initiated. Uses `HistoricalMetricsService` to estimate capacity in PREDICTIVE mode.
3. **Safety Controller**: Validates pacing engine requests. It actively queries `ProviderHealth` and `AgentCapacity` to ensure hard limits are never breached. This is mandatory and cannot be bypassed.
4. **Call Allocator**: Atomically attempts to reserve both an `Agent` and a `Borrower` in the database.
5. **Telecom Provider**: Mock abstractions simulating real telecom endpoints.
6. **Event Processor**: Consumes webhook callbacks from Providers, performing idempotency checks before updating `Call` and `Agent` state.
7. **Recovery Service**: A scheduled background job that detects stale reservations (e.g., worker crash) and releases them.
