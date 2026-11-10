# CLAUDE.md — order-management-app

## Precedence

When two instructions collide, the higher one wins. This order is the whole
answer — never resolve a conflict by picking the nearest file.

1. **The user's explicit instruction in chat.**
2. **`.claude/rules/*.md`** — the normative spec: code rules per layer, plus
   [workflow.md](.claude/rules/workflow.md) for the process.
3. **This file** — the service map, this precedence order, and the universal
   rules below.
4. **Agent / skill / command prompts** (`.claude/agents`, `.claude/skills`,
   `.claude/commands`) — orchestration only: who does what, in which order,
   with which tools. A prompt is never a source of code rules; where a prompt
   and a rule differ, the rule wins.
5. **Existing code, as precedent** — read it before writing anything, but if it
   contradicts 2 or 3, that is a `## TREBA ODLUKA (PARTIAL)`, not a licence to
   copy the deviation (see [workflow.md](.claude/rules/workflow.md)).

**This file supersedes `/home/indir/Desktop/projects/CLAUDE.md` for everything
inside `order-management-app/`.** That parent workspace file describes the
nSight platform; its "Code Change Policy" (never edit directly, print the diff,
do not commit) applies only to the nSight repos it names there — not here. In
this repo, agents edit files directly; see "Agent behavior" below.

## Workspace

Five independent Spring Boot 3.2 / Java 17 services + one shared event
contract module (`oms-common-events`, not a standalone service). Each has
its own `pom.xml` and is built independently — there is no parent/aggregator
build. Full architecture, Kafka topic design, Keycloak roles and the SOLID
rationale are documented in [README.md](README.md); local infra (Postgres,
Kafka, Keycloak, Elasticsearch, LocalStack, host aliases) is in
[development/](development/README.md) — `cd development && docker compose up -d`,
then hosts setup script, then `mvn spring-boot:run` per service.

The whole workspace is **one git repo** (root `.git`, remote `origin` →
`github.com/indirkaric/order-management-app`), not a repo per service.

| Service | Port | Shape |
|---|---|---|
| `identity-service` | 8081 | layered domain service (JPA/Postgres) — layering rules apply |
| `order-service` | 8082 | layered domain service (JPA/Postgres) — layering rules apply |
| `audit-log-service` | 8083 | Kafka consumer only, Elasticsearch store, **no JPA** — different shape, check its existing code before applying the layering rules |
| `oms-ui-service` | 8084 | API gateway / BFF, stateless — different shape |
| `oms-storage-service` | 8085 | S3 upload/download, stateless — different shape |

## Rules index

The detail lives in `.claude/rules/`. Read the file for the layer you're about
to touch — each one states its own scope on the first line, so there is no
guessing about which service a rule applies to. Normative items carry a
`[BLOCK]` or `[WARN]` tag; that tag *is* the review severity.

| File | Scope | Read it when |
|---|---|---|
| [workflow.md](.claude/rules/workflow.md) | all 5, every agent | the pipeline order, who writes tests, handoff blocks (`TREBA ODLUKA`, `KLASE ZA TESTOVE`, `VERDIKT`), git policy |
| [layering.md](.claude/rules/layering.md) | order, identity | package layout, canonical build order, service-per-operation split, ResponseCreatorService, repository, mapper, DTO vs model, entities |
| [controllers.md](.claude/rules/controllers.md) | order, identity | writing or changing any `@RestController` |
| [validators.md](.claude/rules/validators.md) | order, identity | any write operation (create/modify/delete) |
| [exceptions.md](.claude/rules/exceptions.md) | all 5 | throwing or handling anything |
| [testing.md](.claude/rules/testing.md) | all 5 | writing tests |
| [events.md](.claude/rules/events.md) | order, identity, audit-log | domain events, `*EventListener`, Kafka |

The three rules below are short enough to live here, and apply to **every**
service regardless of shape.

## Defensive checks — non-negotiable `[BLOCK]`

Call the `Check` utility (`Check.isNotNull`, `Check.isNotEmpty`) at:

- the start of every constructor, once per injected dependency;
- the start of every public method, once per parameter.

This applies in Controllers, Services, Validators and ResponseCreatorServices
alike. A missing `Check` call on a new public method/constructor is a review
blocker, not a style nit.

## Comments `[WARN]`

Minimal. A comment is only for a non-obvious business rule a reader could not
infer from the code (e.g. "client-sent total is never trusted, always
recomputed server side"). Never a comment that restates what the next line
does.

## Checkstyle `[BLOCK]`

Each service has its own `src/main/resources/checkstyle/checkstyle.xml` —
run `mvn checkstyle:check` inside that service before considering a change
done. Tests run per service too (`mvn test`); there is no aggregator build at
the repo root.

## Agent behavior

This section applies to every agent/subagent/skill working in this repo.
Subagents don't inherit this file the way the main session does — each agent's
own prompt must tell it which files to read (this one,
[workflow.md](.claude/rules/workflow.md), plus the relevant
`.claude/rules/*.md`) before doing anything else. If you're writing a new
agent, include that instruction explicitly rather than assuming it.

- **Language**: respond to the user in Bosnian — explanations, status
  updates, questions, review reports, final summaries. Code, Javadoc,
  comments, and commit messages stay in English, as elsewhere in this
  file.
- **Editing**: agents here may edit files directly (`Write`/`Edit`) — this is
  your own project, not one of the stricter nSight repos (see "Precedence").
  The two read-only agents (`oms-code-reviewer`, `oms-event-flow-auditor`) are
  deliberate exceptions: they have no `Write`/`Edit` so they can be run on
  anything without side effects, and they must report findings rather than fix
  them.
- **Process**: the pipeline, the handoff blocks, how a blocked agent reports,
  and the git/infra policy are all in
  [workflow.md](.claude/rules/workflow.md). It is normative — this list does
  not repeat it.
