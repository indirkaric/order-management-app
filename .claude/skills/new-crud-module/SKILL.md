---
name: new-crud-module
description: Ordered checklist for scaffolding a brand-new domain module (entity through controller) in order-service/identity-service's layered style, per CLAUDE.md. Use when starting a module from zero and you want the checklist injected into the current conversation instead of delegating to a separate subagent.
---

Follow `CLAUDE.md` at the repo root in full — both for the exact
naming/annotation rules and for its "Agent behavior" section (language to
respond in, editing/git policy).

This checklist is the ordering only — don't skip steps or do them out of
order, since later steps depend on earlier ones existing. Applies to
`order-service`/`identity-service`; check the existing code first if you're
tempted to apply this to `audit-log-service`, `oms-ui-service`, or
`oms-storage-service`.

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
9. `<Domain>Controller`
10. Reuse the service's existing `DomainEventListener`/
    `DomainChangeEventListener` for the new event type — don't add a second
    one
11. Tests for every class added in steps 3-9, then `mvn test` and
    `mvn checkstyle:check` inside that service

This is the same ordering `oms-feature-builder` follows when delegated to as
a subagent. Use this skill instead of that agent when you want to do the
scaffolding yourself, in the main conversation, with the checklist as a
guide rather than handing the whole task off.
