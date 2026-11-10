# Layered domain service conventions

**Applies to:** `order-service`, `identity-service` only.

Everything in this file describes the **layered domain service** pattern used
by `order-service` and `identity-service`. Before applying it to
`audit-log-service`, `oms-ui-service`, or `oms-storage-service`, read a couple
of their existing classes first — they are intentionally built differently
(see the service table in `CLAUDE.md` and the README's "Technical decisions
and alternatives").

Controller rules live in [controllers.md](controllers.md), validators in
[validators.md](validators.md), the event bridge in [events.md](events.md),
the pipeline and handoffs in [workflow.md](workflow.md).

## Package layout (per domain module, e.g. "order", "user", "product")

```
entity/<Entity>.java                          JPA entity

modules/<domain>/
  <Domain>Controller.java                     REST layer
  model/                                       API request/response records
  dto/                                         internal DTOs built off projections
  mapper/I<Domain>Mapper.java                  MapStruct
  repository/I<Domain>Repository.java          domain-facing repository contract
  repository/impl/<Domain>Repository.java      Spring Data JPA backed impl
  repository/projection/<Domain>Projection.java read-path projections
  service/I<Verb><Domain>ResponseCreatorService.java + impl/
                                                composes core service(s) + mapper
                                                into the final API response

service/<domain>/I<Verb><Domain>Service.java + impl/
                                                ONE interface PER OPERATION —
                                                never one fat "<Domain>Service"
                                                god class for everything

service/validator/I<Verb><Domain>ServiceValidator.java + impl/
                                                one validator per write
                                                operation, sharing rules via a
                                                Base<Domain>DataValidator

event/<Domain>ChangeEvent + <Domain>EventListener (or DomainChangeEvent /
DomainEventListener if the service already has one — there is exactly one
per service, reuse it)
                                                local Spring event -> single
                                                AFTER_COMMIT bridge to Kafka
```

Rule of thumb: the Controller depends ONLY on `*ResponseCreatorService`
interfaces (plus a bare operation service for operations with no response
body, like delete). It never imports a `Repository`, an `Entity`, or a
MapStruct mapper directly `[BLOCK]`. See `OrderController` / `UserController`
for the reference shape.

## Build order

The canonical order for a new domain module or a new write operation on an
existing one. This is the **only** copy of this ordering in the repo — agents,
skills and commands point here rather than restating it. Later steps depend on
earlier ones existing, so don't reorder them.

1. `entity/<Entity>.java` — if it doesn't exist yet.
2. Liquibase changelog for the new table/columns, in that service's
   `src/main/resources/liquibase/`. Never `ddl-auto: update` `[BLOCK]`.
3. `repository/I<Domain>Repository` + `repository/impl/<Domain>Repository`,
   plus a `repository/projection/` projection if the domain will have
   list/detail reads.
4. `service/validator/I<Verb><Domain>ServiceValidator` + impl — per
   [validators.md](validators.md).
5. `service/<domain>/I<Verb><Domain>Service` + impl — one interface per
   operation. If the operation changes state, publish the local
   `DomainChangeEvent` from here, inside the transaction, per
   [events.md](events.md) — this is where the event belongs, not a later step.
6. `mapper/I<Domain>Mapper` — extend the domain's existing mapper rather than
   adding a second one.
7. `modules/<domain>/model/` request/response records and `dto/` DTOs.
8. `modules/<domain>/service/I<Verb><Domain>ResponseCreatorService` + impl.
9. `<Domain>Controller` endpoint — per [controllers.md](controllers.md).
10. Tests for every class added in steps 3–9, per [testing.md](testing.md).
    Who writes them, and when relative to review, is in
    [workflow.md](workflow.md).

## Service layer: split by operation, not by entity

Do not write one `OrderService` with `create/update/delete/get` methods
`[BLOCK]`. Split into:

- `ICreateXService` / `CreateXService` — write, returns the new entityId
- `IModifyXService` / `ModifyXService` — update / status transitions
- `IDeleteXService` / `DeleteXService` — delete
- `IXService` / `XService` — read-only queries, `@Transactional(readOnly = true)`

Each is `@Service`, constructor injection only, every injected dependency
validated with `Check.isNotNull(dep, "depName")` inside the constructor
`[BLOCK]`, and documented with a full `/** ... @param ... */` Javadoc block
naming every parameter — on the constructor **and** on every public method
`[WARN]`. Write services are `@Transactional` at the class level; read services
are `@Transactional(readOnly = true)`.

### `final` and Spring proxies

Make the class `final` only when Spring does not proxy it. Concretely:

- `final` — repository impls, ResponseCreatorServices, validators,
  `CallerContext`: nothing proxies them, so `final` is correct and checkstyle's
  `DesignForExtension` is satisfied for free.
- **not `final`** — any class carrying `@Transactional` `[BLOCK]`. Spring
  Boot's `TransactionAutoConfiguration` activates
  `@EnableTransactionManagement(proxyTargetClass = true)` whenever
  `spring.aop.proxy-target-class` is unset (`matchIfMissing = true`), so these
  beans are proxied with CGLIB *even though they implement an interface*, and
  CGLIB cannot subclass a `final` class — the context fails to start. Marking
  the individual methods `final` is worse, not better: the proxy is then
  created but silently cannot intercept them, so `@Transactional` stops
  applying with no error at all.

Neither failure is caught by `mvn test`, because the tests mock collaborators
and never build a real Spring context — the same blind spot as the `final`
controller-method incident in [controllers.md](controllers.md). For
`@Transactional` classes, satisfy `DesignForExtension` with the method Javadoc
that this file already requires.

## ResponseCreatorService: the only thing the Controller calls

`modules/<domain>/service/I<Verb><Domain>ResponseCreatorService` wires one or
two core services together with the mapper and returns the final API
response model. Example: `CreateOrderResponseCreatorService` calls
`ICreateOrderService.save(request)` to get an `entityId`, then
`IOrderResponseCreatorService.create(entityId)` to build the full
`OrderResponse`. This seam is what keeps the Controller free of persistence
concerns.

## Repository layer

- `modules/<domain>/repository/I<Domain>Repository` is a hand-written
  interface with domain-named methods (`findByEntityId`,
  `findPagedList(customerId, status, pageable)`, `existsByEntityId`) — it
  does NOT extend `JpaRepository` directly, so callers never see Spring
  Data's generic surface.
- `repository/impl/<Domain>Repository` implements it, wrapping the actual
  Spring Data JPA repository.
- List/detail reads go through `repository/projection/` projections, not the
  full entity graph, to avoid over-fetching.

## Mapper (MapStruct)

- One interface per domain: `@Mapper(componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.IGNORE)`.
- Every method has a short Javadoc (`@param`/`@return`) `[WARN]`.
- Derived/computed fields use `@Mapping(target = "...", expression =
  "java(...)")`, delegating to a small static helper (e.g.
  `OrderAmountCalculator`) — never duplicate the computation inline in both
  the service and the mapper.
- The mapper only ever touches `model`/`dto` types on the way out and
  `entity`/`projection` types on the way in — it is the one place allowed to
  know about both.

## DTO vs model

- `model/` = records exposed over the API (`CreateOrderRequest`,
  `OrderResponse`) — validated with `jakarta.validation` annotations, bound
  with `@Valid @RequestBody`.
- `dto/` = internal objects built off projections (`OrderDTO`), used only
  between the repository/mapper and the ResponseCreatorService. Never return
  a `dto` type from a Controller, and never return an `entity` from anywhere
  above the repository `[BLOCK]`.

## Entities

- `entity/<Name>.java`, Lombok `@Data`, `@EqualsAndHashCode(callSuper = true,
  exclude = {"<collectionFields>"})`, `@ToString(callSuper = true, exclude =
  {"<collectionFields>"})`, `@EntityListeners(BaseEntityListener.class)`,
  explicit `@Table(name = "...", schema = "...")`.
- Schema changes go exclusively through Liquibase changelogs (per service's
  `src/main/resources/liquibase/`) — never `ddl-auto: update` `[BLOCK]`.
