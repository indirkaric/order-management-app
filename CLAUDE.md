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
guessing about which service a rule applies to.

| File | Scope | Read it when |
|---|---|---|
| [layering.md](.claude/rules/layering.md) | order, identity | package layout, service-per-operation split, ResponseCreatorService, repository, mapper, DTO vs model, entities |
| [controllers.md](.claude/rules/controllers.md) | order, identity | writing or changing any `@RestController` |
| [validators.md](.claude/rules/validators.md) | order, identity | any write operation (create/modify/delete) |
| [exceptions.md](.claude/rules/exceptions.md) | all 5 | throwing or handling anything |
| [testing.md](.claude/rules/testing.md) | all 5 | writing tests, or deciding who writes them |
| [events.md](.claude/rules/events.md) | order, identity, audit-log | domain events, `*EventListener`, Kafka |

The four rules below are short enough to live here, and apply to **every**
service regardless of shape.

## Defensive checks — non-negotiable

Call the `Check` utility (`Check.isNotNull`, `Check.isNotEmpty`) at:

- the start of every constructor, once per injected dependency;
- the start of every public method, once per parameter.

This applies in Controllers, Services, Validators and ResponseCreatorServices
alike. A missing `Check` call on a new public method/constructor is a review
blocker, not a style nit.

## Comments

Minimal. A comment is only for a non-obvious business rule a reader could not
infer from the code (e.g. "client-sent total is never trusted, always
recomputed server side"). Never a comment that restates what the next line
does.

## Checkstyle

Each service has its own `src/main/resources/checkstyle/checkstyle.xml`, and a
change is not done until checkstyle passes in every service it touched.

That run happens in exactly one place: `/ship` Step 5, as a single
`mvn -o -q checkstyle:check test` per affected service. The read-only review
agents (`oms-code-reviewer`, `oms-event-flow-auditor`) and
`oms-feature-builder` do **not** run Maven — the same check used to fire up to
three times per ship for one result. If you need it outside `/ship`, run that
one command yourself, in the affected service only; never across all five when
the diff touches fewer.

## Workflow

Code → tests → review → ship:

1. Write the production code (`oms-feature-builder`, or the
   `new-crud-module` skill in the main conversation).
2. Write the tests for it (`oms-test-writer`) — see the ownership rules in
   [testing.md](.claude/rules/testing.md).
3. Review (`oms-code-reviewer`, plus `oms-event-flow-auditor` if events were
   touched).
4. `/ship` — runs that whole sequence, then checkstyle + tests across the
   **affected** services only (one Maven invocation each), then commit and
   push. A diff with no `*.java`, `pom.xml` or `src/**/resources/` change
   skips the build entirely — see the "Cost budget" section of
   [ship.md](.claude/commands/ship.md).

Never review before the tests exist; the reviewer treats a missing `*Test` as
a finding.

## Agent behavior

This section applies to every agent/subagent/skill working in this repo.
Subagents don't inherit this file the way the main session does — each agent's
own prompt must tell it which files to read (this one, plus the relevant
`.claude/rules/*.md`) before doing anything else. If you're writing a new
agent, include that instruction explicitly rather than assuming it.

- **Language**: respond to the user in Bosnian — explanations, status
  updates, questions, review reports, final summaries. Code, Javadoc,
  comments, and commit messages stay in English, as elsewhere in this
  file.
- **Editing**: agents here may edit files directly (`Write`/`Edit`) — this
  is your own project, not one of the stricter nSight repos with a "never
  edit directly, print the diff" policy. The two read-only agents
  (`oms-code-reviewer`, `oms-event-flow-auditor`) are deliberate exceptions:
  they have no `Write`/`Edit` so they can be run on anything without side
  effects, and they must report findings rather than fix them.
- **Git**: agents may use read-only git (`git status`, `git diff`,
  `git branch --show-current`) to work out what changed instead of guessing or
  scanning the whole repo. No agent runs `git commit`, `git push`, or
  Docker/infra-destroying commands (`docker compose down -v`, etc.) without
  being explicitly asked — invoking `/ship` counts as that explicit request
  for commit and push, and for nothing else.

### Subagent output protocol

A subagent cannot ask the user anything: it has no `AskUserQuestion`, and its
report goes to the main agent, not to the user. So no agent prompt may say
"ask the user" — instead agents end their report with one of these blocks, and
the main agent is responsible for acting on it.

**`## TREBA ODLUKA`** — the agent is blocked (ambiguous target service, a
local file that contradicts these rules, a missing decision). It writes the
question plus 2–3 concrete options with their consequences, and **stops
without editing a single file**. It never guesses. The main agent relays this
to the user with `AskUserQuestion`, then re-runs the agent with the answer.

**`## KLASE ZA TESTOVE`** — every class the agent added or changed that needs
a test, one per line as
`<service>/src/main/java/.../CreateXService.java — what to cover` (happy path,
which branches throw `BusinessException`/`EntityNotFoundException`, whether it
publishes an event). The main agent feeds this list to `oms-test-writer`. See
[testing.md](.claude/rules/testing.md).

**`## VERDIKT: APPROVE | WARNING | BLOCK`** — the last line of every read-only
review agent's report, so `/ship` has something to gate on.

- `BLOCK` — a non-negotiable rule is broken: a missing `Check.*`, a layering
  violation, a Kafka/`IDomainEventPublisher` bypass, a `final` method on a
  Controller, or a failing `mvn checkstyle:check` / `mvn test`. The last of
  those is `/ship` Step 5's finding, since it is the only step that runs the
  build; a review agent never reports a build result it did not measure.
- `WARNING` — style, Javadoc/OpenAPI gaps, a test worth adding.
- `APPROVE` — no findings. Say so plainly; don't invent nits to pad the report.
