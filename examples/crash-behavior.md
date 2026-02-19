# Crash behavior

- SDK installs an uncaught exception handler wrapper.
- On fatal crash:
  - event is enqueued synchronously (best effort),
  - worker flush is requested,
  - previous uncaught exception handler is invoked.

This preserves default process termination behavior while attempting to persist crash telemetry.
