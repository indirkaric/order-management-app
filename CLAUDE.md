# CLAUDE.md — order-management-app

## Workspace

Five independent Spring Boot 3.2 / Java 17 services + one shared event
contract module (`oms-common-events`, not a standalone service). Each has
its own `pom.xml` and is built independently — there is no parent/aggregator
build. Full architecture, Kafka topic design, Keycloak roles and the SOLID
rationale are documented in [README.md](README.md); local infra (Postgres,
Kafka, Keycloak, Elasticsearch, LocalStack, host aliases) is in
[development/](development/README.md) — `cd development && docker compose up -d`,
then hosts setup script, then `mvn spring-boot:run` per service.

| Service | Port | Shape |
|---|---|---|
| `identity-service` | 8081 | layered domain service (JPA/Postgres) — rules below apply |
| `order-service` | 8082 | layered domain service (JPA/Postgres) — rules below apply |
| `audit-log-service` | 8083 | Kafka consumer only, Elasticsearch store, **no JPA** — different shape, check its existing code before applying the layering rules below |
| `oms-ui-service` | 8084 | API gateway / BFF, stateless — different shape |
| `oms-storage-service` | 8085 | S3 upload/download, stateless — different shape |

## Universal conventions (all 5 services)

The five sections below (Defensive checks, Exceptions, Tests, Comments,
Checkstyle) apply to every service in this workspace, regardless of shape —
`identity-service`, `order-service`, `audit-log-service`, `oms-ui-service`,
and `oms-storage-service` alike.

## Defensive checks — non-negotiable

Call the `Check` utility (`Check.isNotNull`, `Check.isNotEmpty`) at:

- the start of every constructor, once per injected dependency;
- the start of every public method, once per parameter.

This applies in Controllers, Services, Validators and ResponseCreatorServices
alike. A missing `Check` call on a new public method/constructor is a review
blocker, not a style nit.

## Exceptions

- `EntityNotFoundException` → 404, `BusinessException` (carries a list of
  messages) → 422, both thrown from the service layer, both handled centrally
  in each service's `GlobalExceptionHandler` (`@ControllerAdvice`), never
  caught and swallowed in a Controller or Service.
- `IllegalArgumentException` (what `Check.*` throws) → 400, and
  `MethodArgumentNotValidException` (bean validation) → 400, also handled
  there.
- Every handler logs via SLF4J before building the response.

## Tests

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock` fields,
  dependencies wired manually in `@BeforeEach void setUp()` — no
  `@InjectMocks`) + AssertJ (`assertThat`, `assertThatThrownBy`,
  `assertThatCode`).
- Test class = `<ClassUnderTest>Test`, same package as the class under test,
  under `src/test/java`.
- Method names read as a sentence: `should<ExpectedBehavior>()`, e.g.
  `shouldIgnoreRequestedCustomerIdWhenCallerIsNotElevated`.
- A new `Service`/`Validator`/`Controller` ships with its test in the same
  change — not "tests later". JaCoCo runs via `mvn jacoco:report` per
  service.

## Comments

Minimal. A comment is only for a non-obvious business rule a reader could not
infer from the code (e.g. "client-sent total is never trusted, always
recomputed server side"). Never a comment that restates what the next line
does.

## Checkstyle

Each service has its own `src/main/resources/checkstyle/checkstyle.xml` —
run `mvn checkstyle:check` inside that service before considering a change
done.

## Layered domain service conventions (order-service/identity-service only)

Everything from here down describes the **layered domain service** pattern
used by `order-service` and `identity-service`. Before applying it to
`audit-log-service`, `oms-ui-service`, or `oms-storage-service`, read a
couple of their existing classes first — they are intentionally built
differently (see the table above and the README's "Tehničke odluke").

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
MapStruct mapper directly. See `OrderController` / `UserController` for the
reference shape.

## Service layer: split by operation, not by entity

Do not write one `OrderService` with `create/update/delete/get` methods.
Split into:

- `ICreateXService` / `CreateXService` — write, returns the new entityId
- `IModifyXService` / `ModifyXService` — update / status transitions
- `IDeleteXService` / `DeleteXService` — delete
- `IXService` / `XService` — read-only queries, `@Transactional(readOnly = true)`

Each is `@Service`, a `final` class, constructor injection only, every
injected dependency validated with `Check.isNotNull(dep, "depName")` inside
the constructor, and documented with a full `/** ... @param ... */` Javadoc
block naming every parameter. Write services are `@Transactional` at the
class level; read services are `@Transactional(readOnly = true)`.

## ResponseCreatorService: the only thing the Controller calls

`modules/<domain>/service/I<Verb><Domain>ResponseCreatorService` wires one or
two core services together with the mapper and returns the final API
response model. Example: `CreateOrderResponseCreatorService` calls
`ICreateOrderService.save(request)` to get an `entityId`, then
`IOrderResponseCreatorService.create(entityId)` to build the full
`OrderResponse`. This seam is what keeps the Controller free of persistence
concerns.

## Validators

- Live in `service/validator/`, one interface+impl per write operation
  (`ICreateXServiceValidator`, `IModifyXServiceValidator`).
- Shared field-level rules go in a `BaseXDataValidator` that concrete
  validators extend (see `BaseOrderDataValidator`) — don't copy-paste the
  same rule into two validators.
- Injected into the operation service and called as the FIRST line of the
  method body, before any other logic.
- Violations throw `BusinessException` (never `IllegalStateException` or a
  plain `RuntimeException`).

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
- Every method has a short Javadoc (`@param`/`@return`).
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
  above the repository.

## Entities

- `entity/<Name>.java`, Lombok `@Data`, `@EqualsAndHashCode(callSuper = true,
  exclude = {"<collectionFields>"})`, `@ToString(callSuper = true, exclude =
  {"<collectionFields>"})`, `@EntityListeners(BaseEntityListener.class)`,
  explicit `@Table(name = "...", schema = "...")`.
- Schema changes go exclusively through Liquibase changelogs (per service's
  `src/main/resources/liquibase/`) — never `ddl-auto: update`.

## Controllers

- `@RestController`, `@RequestMapping(path = "/api/v1/<plural>", produces =
  MediaType.APPLICATION_JSON_VALUE)`, `@Tag(name = "...", description =
  "...")`.
- Every endpoint: full Javadoc, `@Operation` + `@ApiResponses` (+
  `@Parameters` for query params), `@PreAuthorize(Constants.Authority.X_Y_AUTHORITY)`,
  `@Valid @RequestBody` for bodies, `Check.*` on every parameter, delegate to
  exactly one `*ResponseCreatorService` (or a bare operation service for
  delete), wrap the result in `ResponseEntity` with the right status (`201`
  create, `200` read/update, `204` delete or empty list, `404`/`422`
  documented in `@ApiResponses` even though they're thrown, not returned,
  from this method).
- Class members are `final` wherever the language allows it — but controller
  **methods are never `final`**: Spring's method-security proxy (CGLIB) must
  be able to override them to route through `@PreAuthorize` interception; a
  `final` method breaks that and produces an NPE on every injected field the
  moment the endpoint is actually called (not caught by `mvn test` with
  mocks, only on live traffic, since method-security proxies are only built
  for the real Spring context). This was the root cause of a real incident
  across `identity-service`, `order-service` and `audit-log-service`.

## Domain events (Kafka)

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
- `audit-log-service` is the consumer side: it checks `existsByEventId`
  before writing to Elasticsearch, so a redelivery after a crash/rebalance
  is safe.

## Agent behavior

This section applies to every agent/subagent/skill working in this repo.
Subagents don't automatically inherit this file the way the main session
does — each agent's own prompt must tell it to read `CLAUDE.md` in full
before doing anything else; if you're writing a new agent, include that
instruction explicitly rather than assuming it.

- **Language**: respond to the user in Bosnian — explanations, status
  updates, questions, review reports, final summaries. Code, Javadoc,
  comments, and commit messages stay in English, as elsewhere in this
  file.
- **Editing**: agents here may edit files directly (`Write`/`Edit`) — this
  is your own project, not one of the stricter nSight repos with a "never
  edit directly, print the diff" policy.
- **Git/infra**: no agent runs `git commit`, `git push`, or
  Docker/infra-destroying commands (`docker compose down -v`, etc.) without
  being explicitly asked.
