# Agent State Machine

```mermaid
stateDiagram-v2
    [*] --> OFFLINE
    OFFLINE --> AVAILABLE : Login
    AVAILABLE --> RESERVED : Allocator Reserves
    RESERVED --> DIALING : Call Initiated
    RESERVED --> AVAILABLE : Recovery Timeout (Crash)
    DIALING --> CONNECTED : Call Answered
    DIALING --> AVAILABLE : Call Failed / No Answer
    CONNECTED --> WRAP_UP : Call Ended
    WRAP_UP --> AVAILABLE : Wrap Up Complete
    AVAILABLE --> PAUSED : Agent Break
    PAUSED --> AVAILABLE : Break Over
```
