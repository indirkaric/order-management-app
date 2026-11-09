# Domain events (Kafka)

**Applies to:** `order-service` and `identity-service` (producer side),
`audit-log-service` (consumer side). The shared contract types live in
`oms-common-events`.

## Producer side

- A service that changes state publishes a **local** Spring event —
  `applicationEventPublisher.publishEvent(new DomainChangeEvent(entityType,
  action, entityId, performedBy, payload))` — inside the `@Transactional`
  method, never a direct Kafka call.
- Exactly one `@Component` per service (`DomainEventListener` in
  `order-service`, `DomainChangeEventListener` in `identity-service`) has the
  single `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
  method that turns the local event into
  `IDomainEventPublisher.publish(OmsEvent.of(...))`. This is the ONLY class
  per service allowed to depend on `IDomainEventPublisher`/Kafka. Reuse the
  existing one — do not add a second listener.
- That listener swallows and logs `RuntimeException` — a Kafka outage must
  never surface as an error to the original caller, whose transaction already
  committed.

## Consumer side

- `audit-log-service` is the consumer side: it checks `existsByEventId`
  before writing to Elasticsearch, so a redelivery after a crash/rebalance
  is safe.

## Why each rule exists

- **Local event, not a direct Kafka call** — keeps the write path free of
  broker concerns and testable with a mocked `ApplicationEventPublisher`.
- **`AFTER_COMMIT`, not plain `@EventListener`** — a plain `@EventListener`
  fires even when the transaction rolls back, publishing an event for a write
  that never happened. `BEFORE_COMMIT` has the same problem.
- **One listener per service** — a second one silently double-publishes.
- **Swallow + log in the listener** — the caller's transaction has already
  committed, so a broker failure must not become their 500.
- **`existsByEventId` on the consumer** — at-least-once delivery means
  redelivery is normal, not exceptional.

`oms-event-flow-auditor` checks exactly these five points, read-only, and is
worth running on any change that touches `event/`, a `*EventListener`, or
anything implementing `IDomainEventPublisher`.
