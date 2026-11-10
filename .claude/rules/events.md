# Domain events (Kafka)

**Applies to:** `order-service` and `identity-service` (producer side),
`audit-log-service` (consumer side). The shared contract types live in
`oms-common-events`.

## Producer side

- A service that changes state publishes a **local** Spring event —
  `applicationEventPublisher.publishEvent(new DomainChangeEvent(entityType,
  action, entityId, performedBy, payload))` — inside the `@Transactional`
  method, never a direct Kafka call `[BLOCK]`.
- Exactly one `@Component` per service (`DomainEventListener` in
  `order-service`, `DomainChangeEventListener` in `identity-service`) has the
  single `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
  method that turns the local event into
  `IDomainEventPublisher.publish(OmsEvent.of(...))`. This is the ONLY class
  per service allowed to depend on `IDomainEventPublisher`/Kafka `[BLOCK]`.
  Reuse the existing one — do not add a second listener `[BLOCK]`.
- That listener swallows and logs `RuntimeException` `[BLOCK]` — a Kafka outage
  must never surface as an error to the original caller, whose transaction
  already committed.

## Consumer side

- `audit-log-service` is the consumer side: it checks `existsByEventId`
  before writing to Elasticsearch `[BLOCK]`, so a redelivery after a
  crash/rebalance is safe.

## The five checks

Any change to `event/`, a `*EventListener`, or anything implementing
`IDomainEventPublisher` is audited against exactly these five points, and
nothing else — each one produces a silent correctness bug (lost events, events
for rolled-back writes, double publishes, duplicated audit rows), never a
cosmetic one. Which agent runs them, and how it is invoked, is in
[workflow.md](workflow.md).

1. No class outside the service's one `DomainEventListener` /
   `DomainChangeEventListener` imports `IDomainEventPublisher` or
   `KafkaTemplate`.
2. Every `applicationEventPublisher.publishEvent(new DomainChangeEvent(...))`
   call sits inside a method annotated `@Transactional` (class- or
   method-level), and is not placed after an early return that would skip the
   write it is supposed to describe.
3. The listener method is annotated
   `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
4. The listener catches and logs `RuntimeException` around the publish call
   instead of letting it propagate.
5. In `audit-log-service`: an `existsByEventId` (or equivalent) idempotency
   check runs before persisting to Elasticsearch.

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
