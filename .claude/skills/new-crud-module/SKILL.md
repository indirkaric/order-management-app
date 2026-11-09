---
name: new-crud-module
description: Ordered checklist for scaffolding a brand-new domain module (entity through controller) in order-service/identity-service's layered style. Use when starting a module from zero and you want the checklist injected into the current conversation instead of delegating to the oms-feature-builder subagent.
---

Follow `CLAUDE.md` at the repo root plus `.claude/rules/layering.md`,
`controllers.md` and `validators.md` — those hold the exact
naming/annotation rules, and `CLAUDE.md`'s "Agent behavior" section holds the
language and git policy. Read them before step 1; this checklist is the
ordering only.

Don't skip steps or do them out of order — later steps depend on earlier ones
existing. Applies to `order-service`/`identity-service`; check the existing
code first if you're tempted to apply this to `audit-log-service`,
`oms-ui-service`, or `oms-storage-service`.

1. `entity/<Entity>.java`
2. Liquibase changelog adding the table, in that service's
   `src/main/resources/liquibase/`
3. `modules/<domain>/repository/I<Domain>Repository` +
   `repository/impl/<Domain>Repository` (+ `repository/projection/` if the
   module needs list/detail reads)
4. `service/validator/I<Verb><Domain>ServiceValidator` + impl
5. `service/<domain>/I<Verb><Domain>Service` + impl (one per operation:
   create/modify/delete/read)
6. `modules/<domain>/mapper/I<Domain>Mapper`
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

This is the same ordering `oms-feature-builder` follows when delegated to as
a subagent. Use this skill instead of that agent when you want to do the
scaffolding yourself, in the main conversation, with the checklist as a guide
rather than handing the whole task off. One difference: the subagent stops at
step 10 and hands back a `## KLASE ZA TESTOVE` list, whereas here steps 11–12
are yours to finish.
