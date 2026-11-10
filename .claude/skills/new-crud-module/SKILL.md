---
name: new-crud-module
description: Ordered checklist for scaffolding a brand-new domain module (entity through controller) in order-service/identity-service's layered style. Use when starting a module from zero and you want the checklist injected into the current conversation instead of delegating to the oms-feature-builder subagent.
---

Read `CLAUDE.md` (precedence, service map, universal rules) and
`.claude/rules/workflow.md` (pipeline, handoff blocks), then build by the
numbered **"Build order"** section of `.claude/rules/layering.md` — steps 1–10,
in order, with `controllers.md`, `validators.md`, `exceptions.md` and
`events.md` open for the layer you're on. That list is the single canonical
ordering in this repo; this skill does not keep a second copy of it.

Applies to `order-service` / `identity-service`. The other three services are
shaped differently — read their existing code first if you're tempted to apply
this there.

The only difference from delegating to the `oms-feature-builder` subagent:
you're in the main conversation, so **step 10 is yours**. Either write the
tests yourself or hand the class list to `oms-test-writer` — either way they
land in this same change, before any review. Then `oms-code-reviewer` (plus
`oms-event-flow-auditor` if the module publishes events), then `/ship`.
