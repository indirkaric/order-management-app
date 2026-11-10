---
name: new-crud-module
description: Ordered checklist for scaffolding a brand-new domain module (entity through controller) in order-service/identity-service's layered style. Use when starting a module from zero and you want the checklist injected into the current conversation instead of delegating to the oms-feature-builder subagent.
---

This file is the **single source of the build order** for a layered module —
`oms-feature-builder` reads it too, so the ordering lives here and nowhere
else. It is the ordering only: the exact naming and annotation rules are in
`CLAUDE.md` plus `.claude/rules/layering.md`, `controllers.md` and
`validators.md`. Read those before step 1.

Don't skip steps or reorder them — later steps depend on earlier ones
existing. Applies to `order-service`/`identity-service`; for
`audit-log-service`, `oms-ui-service` or `oms-storage-service`, read their
existing code first.

1. `entity/<Entity>.java`
2. Liquibase changelog adding the table, in that service's
   `src/main/resources/liquibase/` — never `ddl-auto: update`
3. `modules/<domain>/repository/I<Domain>Repository` +
   `repository/impl/<Domain>Repository` (+ `repository/projection/` if the
   module needs list/detail reads)
4. `service/validator/I<Verb><Domain>ServiceValidator` + impl — the operation
   service's first line calls it
5. `service/<domain>/I<Verb><Domain>Service` + impl (one per operation:
   create/modify/delete/read, never a combined god-service)
6. `modules/<domain>/mapper/I<Domain>Mapper` — extend the domain's existing
   mapper rather than adding a second one
7. `modules/<domain>/model/` (request/response records) and `dto/`
   (internal DTOs)
8. `modules/<domain>/service/I<Verb><Domain>ResponseCreatorService` + impl
9. `<Domain>Controller` — no `final` methods, per `controllers.md`
10. Reuse the service's existing `DomainEventListener`/
    `DomainChangeEventListener` for the new event type — don't add a second
    one (`.claude/rules/events.md`)
11. **Tests for every class added in steps 3–9**, per
    `.claude/rules/testing.md`. Since you're in the main conversation here,
    either write them yourself or hand the class list to `oms-test-writer` —
    either way they land in this same change, before any review.
12. Review, then ship: `oms-code-reviewer` (plus `oms-event-flow-auditor`
    if step 10 applied), then `/ship`. Never review before step 11 — the
    reviewer counts a missing `*Test` as a finding, so reviewing first just
    produces noise.

Use this skill when you want to scaffold in the main conversation with the
checklist as a guide; use `oms-feature-builder` to hand the whole task off. The
subagent stops at step 10 and returns a `## KLASE ZA TESTOVE` list, so there
steps 11–12 are the main agent's; here they are yours.
