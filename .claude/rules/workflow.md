# Process: pipeline, handoffs, blocked agents, verdicts

**Applies to:** every agent, skill and command in this repo, for all 5 services.

This file owns the *process*. The `rules/*.md` siblings own the *code rules*;
`CLAUDE.md` owns the service map, the precedence order and the universal rules.
An agent/skill/command prompt orchestrates — it never restates what is written
here.

## Pipeline

```
1 build      production code only          oms-feature-builder, or the
                                           new-crud-module skill in the main
                                           conversation
2 test       one test per class from the    oms-test-writer
             handoff list
3 review     read-only, reports findings    oms-code-reviewer
             (+ event flow, if triggered)   (+ oms-event-flow-auditor)
4 ship       checkstyle + tests across all  /ship
             5 services, then commit+push
```

The order is a gate, not a suggestion: **review never runs before the tests
exist**, because a reviewer counts a missing `*Test` as a finding, so reviewing
first reports the pipeline's own design as a defect. A change is not done while
the handoff list from step 1 is non-empty.

### Who writes the tests

Step 1 writes production code and hands back a `## KLASE ZA TESTOVE` list.
Step 2 empties that list. "A new class ships with its test in the same change"
([testing.md](testing.md)) is satisfied at the level of the **change**, not of a
single agent — which is why the author of the code is never the author of its
coverage.

### Which agent a change triggers

| The change touches | Run |
|---|---|
| any Java code in any of the 5 services | `oms-code-reviewer` |
| `event/`, a `*EventListener`, or anything implementing `IDomainEventPublisher` | `oms-event-flow-auditor` as well, in parallel |

`oms-event-flow-auditor` deliberately has no `Bash`, so it cannot read the git
diff. Whoever launches it **must name the affected service and files in its
prompt**; without that it audits the whole flow and says so.

## Handoff blocks

A subagent cannot ask the user anything: it has no `AskUserQuestion`, and its
report goes to the main agent. So no agent prompt may say "ask the user" —
agents end their report with one of these blocks instead, and the main agent
acts on it.

### `## KLASE ZA TESTOVE`

Every class the agent added or changed that needs a test, one per line:

```
## KLASE ZA TESTOVE
order-service/src/main/java/.../service/order/impl/CreateOrderService.java — happy path, BusinessException from validator, publishes DomainChangeEvent
order-service/src/main/java/.../service/validator/impl/CreateOrderServiceValidator.java — one case per rejection rule
```

The main agent passes it straight to step 2. An empty list is valid only if the
agent genuinely added no `Service`/`Validator`/`Mapper`/`Controller`/consumer,
and it must then say so explicitly rather than omitting the block.

### `## TREBA ODLUKA` — two classes, pick the right one

The agent is blocked on a decision that is not its to make (ambiguous target
service, a local file that contradicts the rules, a missing business decision).
Both forms carry the question plus 2–3 concrete options with their
consequences, and neither ever guesses.

**`## TREBA ODLUKA (HARD STOP)`** — the blocker invalidates the *whole* task:
the target service cannot be derived, or the requested behaviour itself is
unclear. Write **zero files** and stop. There is nothing safe to build when you
don't know where it goes — the layering rules apply to `order-service` and
`identity-service` only, and the other three services are shaped differently.

**`## TREBA ODLUKA (PARTIAL)`** — the blocker is local to *one* class or
decision: an existing file in the target domain deviates from the rules, so
following the rules and following local precedent produce different code.
Then:

- build every part of the feature that does **not** depend on the answer;
- leave the disputed class unwritten — do not silently normalise the neighbour
  to the rules, and do not silently follow the deviation either;
- cite the conflicting `file:line` in the block;
- still end with `## KLASE ZA TESTOVE` for what you did write.

These two are the only reasons to stop early, and they are mutually exclusive.
Do not emit a `HARD STOP` for something that only affects one class, and never
treat a `PARTIAL` as licence to leave the rest of the feature unbuilt.

### Continue the blocked agent — do not restart it

The main agent relays the question with `AskUserQuestion`, then **continues the
same agent with `SendMessage`**, passing the answer. The agent keeps its
context and the files it already wrote. A fresh `Agent` call starts cold,
re-derives everything, and may make different choices for the parts that were
already finished — spawn one only if the original agent is gone.

### `## VERDIKT: APPROVE | WARNING | BLOCK`

The last line of every read-only review agent's report, so `/ship` has
something to gate on.

- **`BLOCK`** — at least one finding tagged `[BLOCK]` in `rules/*.md` or
  `CLAUDE.md`, or a failing `mvn checkstyle:check` / `mvn test`.
- **`WARNING`** — findings tagged `[WARN]` only.
- **`APPROVE`** — no findings. Say so plainly; don't invent nits to pad the
  report.

The severity of an individual finding is **not** a judgement call and is not
restated in any agent prompt: it is the `[BLOCK]`/`[WARN]` tag written next to
that rule in `CLAUDE.md` or the relevant `rules/*.md`. A rule with no tag is
`[WARN]`.

## Git and infra in the pipeline

Only `/ship` may `git commit`/`git push`; invoking it is the user's explicit
authorisation for those two actions and for nothing else. Every other agent is
limited to read-only git (`git status`, `git diff`, `git branch --show-current`)
to work out what changed. No agent runs `docker compose down -v` or anything
else that wipes local infra unless the user asks for exactly that.
