# /ship — Full Ship Workflow

Orchestrate all steps directly; do not hand the whole workflow to a subagent.
Individual steps spawn focused agents.

**User input**: $ARGUMENTS — scope notes only. Arguments never replace the
branch preflight, never change which branch you are on, and can only widen
Step 2's scope, never narrow it below what the diff touches.

Read `CLAUDE.md` and `.claude/rules/testing.md` before Step 1. Answer in
Bosnian throughout. Open each step with `▶ <Step Name>...`, close it with
`✓ <outcome in one line>`.

## Budget — hard limits

- At most **one** Maven invocation per affected service per pass, plus the one
  batched run `oms-test-writer` makes in Step 3 if it wrote tests. Never one
  invocation per goal, one per test class, in parallel, or with `-T`.
- Never Maven in a service the diff does not touch, when Step 2 found nothing
  build-relevant, in `oms-common-events/` beyond Step 2.5's `install`, or from
  a review agent (`CLAUDE.md`, "Checkstyle").
- Never the full `git diff main` in this conversation — `--stat` here, the full
  diff only inside `oms-code-reviewer`.

If a step seems to need more, stop and tell the user what it wants and why.

---

## Step 1 — Preflight And Analyze Changes

1. Run `git branch --show-current`, `git status --short`,
   `git diff --name-only main`, `git diff --stat main`.
2. The branch must match `^(feature|fix|chore)/[a-z0-9]+(?:-[a-z0-9]+)*$`. If
   it does not — `main` included — **stop**: `/ship` never creates a branch
   around an existing diff and never auto-stashes. Give the user the command
   and let them decide.
3. No changes to commit → stop and say so.
4. Unrelated changes in the diff → **[TREBA INPUT]**: list them, ask whether
   they leave this scope, and do not continue until the diff is one piece of
   work.
5. Never stage `development/.env`, `.mcp.json` (holds a GitHub PAT), or
   anything under `target/`. If one appears in the diff, flag it first.

---

## Step 2 — Lock Scope

This step decides what the rest of the run is allowed to cost.

1. Derive the changed-file list once with `git diff --name-only main`; reuse it
   for the whole run, refreshing only if the diff actually changes.
2. A path is *build-relevant* if it is a `*.java` file, a `pom.xml`, or under
   `src/main/resources/` or `src/test/resources/`.
3. **Nothing build-relevant** (docs, `.claude/**`, `postman/**`) → nothing
   compiles and no behaviour changed: **skip Steps 3 and 5 entirely** (zero
   Maven invocations), run Step 4 on the changed files only, and name the
   skipped steps plus the reason in the Step 2 `✓` line and in Step 8. Never
   skip silently.
4. **Otherwise** the affected services are the top-level directories of the
   build-relevant paths — only those are in scope for Steps 3 and 5.
5. `oms-common-events/` is a contract module, not one of the five services: no
   `checkstyle.xml`, no tests, so `checkstyle:check` fails there outright and
   it never enters the Step 5 set. If the diff touches it, run
   `mvn -o -q install -DskipTests` inside it, then add `order-service`,
   `identity-service` and `audit-log-service` to the affected set — they
   resolve it from `~/.m2`, so a stale install makes their test runs lie.

---

## Step 3 — Tests Before Review

Skip entirely if Step 2 found nothing build-relevant.

1. From the changed-file list, find every new or changed `*Service`,
   `*Validator`, `*Mapper`, `*Controller` or Kafka-consumer class with no
   matching `*Test` under `src/test/java`.
2. If that list is non-empty, launch **oms-test-writer** with it, passing any
   earlier `## KLASE ZA TESTOVE` block through verbatim.
3. Tests come before the review on purpose: `oms-code-reviewer` counts a
   missing `*Test` as a finding, so reviewing first reports the workflow's own
   design as a defect.
4. `## TREBA ODLUKA` back from the agent → relay it with `AskUserQuestion` and
   re-run the agent with the answer. Never answer on the user's behalf.

---

## Step 4 — Code Review

1. Launch **oms-code-reviewer** against `main`, so staged and unstaged changes
   are both included. Name Step 2's affected services in its prompt, so it
   reads only the rules files that apply.
2. If the diff touches `event/`, a `*EventListener`, or anything implementing
   `IDomainEventPublisher`, launch **oms-event-flow-auditor** in parallel and
   name the service and files — it has no `Bash`, so without them it stops and
   asks rather than scanning every service.

Neither agent runs Maven; a checkstyle or test result in either report is
unverified, so rely on Step 5.

Then, on each report's `## VERDIKT` line:

- **`BLOCK`** — **[TREBA INPUT]**: stop, report every finding, ask how to
  proceed. Do NOT continue.
- **`WARNING`** — report the findings and ask whether to continue; continuing
  is explicitly accepting that risk for this run. Do NOT continue
  automatically.
- **`APPROVE`** — continue.
- **No verdict line, or the agent failed to run** — **[TREBA INPUT]**: report
  that and ask whether to stop or proceed without the review. Do NOT assume
  `APPROVE`.
- Any `## TREBA ODLUKA` — relay it, as in Step 3.

If you fix findings here, refresh the changed-file list and re-run Step 2's
classification — a fix can pull a Java file into a diff that had none.

---

## Step 5 — Hygiene

The last gate before committing, and the only place in this workflow allowed
to run Maven. Skip entirely if Step 2 found nothing build-relevant, and say so
in the report.

Otherwise, for **each affected service from Step 2** — not all five — from
inside that service's directory, one command, services one after another:

```bash
mvn -o -q checkstyle:check test
```

- **One invocation, not `checkstyle:check` then `test`**: one Maven boot, one
  resolution, one compile. The checkstyle execution in these poms has no
  `<phase>`, so it defaults to `verify`, which `test` never reaches — the goal
  must stay explicit, it does not come free with `test`.
- **`-o`** passes in all five services. On a *resolution* error (a genuinely
  missing artifact, not a test failure), re-run without it and note that.
- **No `mvn clean`** — add it only if a result is actually suspect, and say
  why.

On failure: fix inline, re-run that one service's command. Never skip,
`@Disabled` or weaken a failing test to get green. `mvn jacoco:report` only if
the user asked for coverage.

Report the outcome per service, not one blanket "all green", and name the
services that were out of scope.

---

## Step 6 — Commit

1. Stage only the relevant files — explicit paths, never `git add -A`, never
   Step 1.5's exclusions.
2. Commit with a clear imperative message giving the **why**, not a
   restatement of the diff. No `Co-Authored-By` lines.

## Step 7 — Push

`git push -u origin <branch-name>`

## Step 8 — Report

- Branch pushed; commit SHA
- Affected services, and which of the five were out of scope
- Any step skipped by Step 2's classification, with the reason
- Review verdict (and the event-flow verdict, if that agent ran)
- Test classes written in Step 3, and the per-service Step 5 outcome
- How many Maven invocations the run actually cost
- PR link: `https://github.com/indirkaric/order-management-app/compare/main...<branch>`

There is no `gh` CLI here, so `/ship` stops at the link. Never claim a PR was
opened, a review requested, or anything merged.

---

## Rules

- Never force-push or use destructive git commands.
- Never `docker compose down -v` or anything else that wipes local infra.
- Only this command may `git commit`/`git push` — invoking it authorises those
  two actions and nothing else (`CLAUDE.md`, "Agent behavior").
- Follow `CLAUDE.md` and `.claude/rules/`.
- At any unexpected error, report clearly and stop.
