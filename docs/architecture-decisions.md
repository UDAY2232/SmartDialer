# Architecture Decisions

### Decision: Relational Database (PostgreSQL / H2) instead of NoSQL
**Why**: We need ACID guarantees for atomic reservations (e.g., `UPDATE agents SET status='RESERVED' WHERE status='AVAILABLE'`).
**Trade-off**: Requires scaling up vertically or complex sharding at extreme scale, but guarantees zero double-dials.

### Decision: Safety Controller as a Strict Boundary
**Why**: Predictive algorithms are statistical and can be wrong (e.g., sudden answer rate spikes). A deterministic rules-engine (Safety Controller) prevents abandoning connected calls.
**Trade-off**: Adds a hop in the allocation pipeline.

### Decision: Statistical Pacing instead of Deep Learning
**Why**: Explainability. The business needs to know *why* a number was dialed. A statistical model (`agents / answer_rate`) is easily auditable.
**Trade-off**: May not capture extremely complex hidden variables, but is safe and reliable.
