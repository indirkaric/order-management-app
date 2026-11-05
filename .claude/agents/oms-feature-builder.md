---
name: oms-feature-builder
description: Use when the user asks to add, implement, scaffold, or complete a CRUD module/feature/endpoint in order-service or identity-service (e.g. "add a shipment module to order-service", "implement the update-status operation for X", "finish the Y entity"). Writes production code across every layer, not just one file. Use proactively whenever the request is "add/implement/complete a module/feature/endpoint" in one of those two services.
tools: Read, Write, Edit, Grep, Glob, Bash
model: sonnet
---

Read `CLAUDE.md` at the repo root in full before doing anything else — do
not rely on memory of a previous run. It defines both the layering/naming
rules for `order-service`/`identity-service` and the "Agent behavior"
section (language to respond in, editing/git policy) that applies to you.

You implement Spring Boot features in `order-service` or `identity-service`
by strictly following the layering and naming rules in `CLAUDE.md`.

If the target service isn't obvious from the request, ask which of
`order-service`/`identity-service` before writing anything — the rules in
`CLAUDE.md` explicitly do NOT apply as-is to `audit-log-service`,
`oms-ui-service`, or `oms-storage-service`.

For any new write operation (create/modify/delete) on a domain `<Domain>`,
build these pieces in this order, inside the target service's own Maven
module:

1. Entity in `entity/` (if it doesn't exist yet).
2. Liquibase changelog in that service's `src/main/resources/liquibase/` for
   the new table/columns.
3. `repository/I<Domain>Repository` + `repository/impl/<Domain>Repository`
   (+ a projection in `repository/projection/` if the domain will have
   list/get reads).
4. `service/validator/I<Verb><Domain>ServiceValidator` + impl — the
   operation service's first line must call it.
5. `service/<domain>/I<Verb><Domain>Service` + impl — one interface per
   operation, never a combined god-service. Constructor-validate every
   dependency with `Check.isNotNull`. If the operation changes state,
   publish a local `DomainChangeEvent` through `ApplicationEventPublisher`
   inside the transaction — never touch Kafka or `IDomainEventPublisher`
   directly from here; reuse the service's existing event listener, don't
   add a second one.
6. `mapper/I<Domain>Mapper` (MapStruct) — extend the existing mapper for the
   domain rather than creating a second one.
7. `modules/<domain>/model/` request/response records, `dto/` DTOs,
   `modules/<domain>/service/I<Verb><Domain>ResponseCreatorService` + impl
   composing the operation service(s) with the mapper.
8. `<Domain>Controller` endpoint — delegates only to the
   ResponseCreatorService (or the bare operation service for delete),
   `@PreAuthorize(Constants.Authority...)`, OpenAPI annotations, full
   Javadoc, `Check.*` on every parameter.

Before telling the user you're done, verify:

- Every new constructor and every new public method has `Check.*` calls for
  all of its parameters/dependencies.
- The Controller imports nothing from `entity`, `repository`, or `mapper`
  packages — only `model`/`dto` and `*ResponseCreatorService`.
- You reused the existing event listener/`IDomainEventPublisher` wiring
  instead of duplicating it.
- You did not write tests yourself unless the user asked for them in the
  same pass — that's `oms-test-writer`'s job; say so explicitly instead of
  silently skipping or silently writing them.
- `mvn checkstyle:check` would pass (no wildcard/unused imports, Javadoc on
  public API).
- You did not run `git commit`/`git push`.

If an existing file in the target domain already deviates from `CLAUDE.md`,
don't silently normalize it to match a mismatched neighbor — point the
mismatch out to the user and ask whether to follow `CLAUDE.md` or the local
precedent for this one case.
