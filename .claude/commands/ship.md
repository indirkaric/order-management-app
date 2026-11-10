# /ship — Full Ship Workflow

Orchestrate all steps directly. Do not hand the entire workflow to a subagent.
Individual steps spawn focused agents.

**User input**: $ARGUMENTS

Arguments may add scope notes, but they never replace the branch preflight and
never change which branch you are on.

Read `CLAUDE.md` and `.claude/rules/testing.md` before Step 1. Answer the user
in Bosnian throughout.

At the start of each step output a progress line:
`▶ <Step Name>...`
At the end of each step output:
`✓ <outcome in one line>`

**Before you start, read the "Cost budget" section at the bottom of this file.**
It is not advice — it is the rule this command is built around, because an
unscoped run burns ~10 Maven JVMs (≈49 s of saturated CPU) for work that fits
in one.

---

## Step 1 — Preflight And Analyze Changes

1. Run `git branch --show-current`, `git status --short`,
   `git diff --name-only main`, and `git diff --stat main`.
   Do **not** pull the full `git diff main` into this conversation — that is
   `oms-code-reviewer`'s job in Step 4, and fetching it here just means paying
   for the same diff twice.
2. Require the current branch to match
   `^(feature|fix|chore)/[a-z0-9]+(?:-[a-z0-9]+)*$`. If it does not — including
   when you are on `main` — **stop**. `/ship` never creates a branch around an
   existing diff and never auto-stashes changes; tell the user the command to
   run and let them decide.
3. If there are no changes to commit, stop and inform the user.
4. Check for unrelated changes. If any exist: **[TREBA INPUT]** — list them and
   ask whether they should be removed from this scope. Do not continue until
   the diff belongs entirely to one piece of work.
5. Never stage or commit `development/.env`, `.mcp.json`, or anything under
   `target/`. If one of them shows up in the diff, flag it before going on —
   `.mcp.json` holds a GitHub PAT.

---

## Step 2 — Lock Scope

This step decides how much CPU the rest of the run is allowed to spend. Get it
right and Steps 3–5 are cheap; skip it and they are not.

1. Derive the changed-file list once via `git diff --name-only main` and reuse
   it for the rest of the run, refreshing it only if the diff actually changes.

2. **Classify the diff.** A path is *build-relevant* if it is a `*.java` file,
   a `pom.xml`, or anything under `src/main/resources/` or
   `src/test/resources/`.

3. **If nothing in the diff is build-relevant** — a docs-only change
   (`README.md`, `CLAUDE.md`), `.claude/**`, `postman/**`, `development/**`
   docs — then there is nothing to compile and nothing whose behaviour could
   have changed:

   - **skip Step 3 entirely** (no production class changed, so no class needs a
     test);
   - run Step 4 on the changed files only;
   - **skip Step 5 entirely** — zero Maven invocations;
   - say so explicitly in the Step 2 `✓` line and again in the Step 8 report,
     naming which steps were skipped and why. Never silently skip them.

4. **Otherwise, derive the affected services** from the build-relevant paths
   (the top-level directory of each path). Only those services are in scope for
   Steps 3 and 5. Treat user arguments as additional scope notes only — they
   can widen the scope, never silently narrow it below what the diff touches.

5. `oms-common-events/` is a contract module, **not** one of the five services,
   so it never enters the Step 5 set — it has no `checkstyle.xml`, no
   checkstyle plugin in its `pom.xml` and no tests, and `checkstyle:check`
   there fails outright. If the diff touches it:

   - run `mvn -o -q install -DskipTests` inside it first;
   - then add `order-service`, `identity-service` and `audit-log-service` to
     the affected set. They resolve it as `0.0.1-SNAPSHOT` from `~/.m2`, so a
     stale install makes the later test runs lie, and they are also where its
     contract is actually exercised.

---

## Step 3 — Tests Before Review

Skip this step entirely if Step 2 classified the diff as not build-relevant.

1. From the changed-file list, find every new or changed `*Service`,
   `*Validator`, `*Mapper`, `*Controller` or Kafka-consumer class with no
   matching `*Test` under `src/test/java`.
2. If that list is non-empty, launch **oms-test-writer** with it. If a previous
   step produced a `## KLASE ZA TESTOVE` block, pass that through verbatim.
   State in its prompt that it must verify with **one** batched run —
   `mvn -o -q test -Dtest=A,B,C -DfailIfNoTests=false` in the affected service —
   not one Maven invocation per test class.
3. This runs *before* the review on purpose: `oms-code-reviewer` counts a
   missing `*Test` as a finding, so reviewing first would just report the
   workflow's own design as a defect.
4. If the agent returns `## TREBA ODLUKA`, relay it to the user with
   `AskUserQuestion` and re-run it with the answer. Do not answer on the
   user's behalf.

---

## Step 4 — Code Review

1. Launch **oms-code-reviewer**, instructing it to review against `main` so
   staged and unstaged working-tree changes are both included. Name the
   affected services from Step 2 in its prompt, so it reads only the rules
   files that actually apply.
2. If the diff touches `event/`, a `*EventListener`, or anything implementing
   `IDomainEventPublisher`, launch **oms-event-flow-auditor** in parallel and
   name the affected service and files in its prompt (it has no `Bash`, so it
   cannot read the diff itself, and without named files it will stop and ask
   rather than scan every service).

Neither agent runs Maven — both are read-only by design, and Step 5 is the
single authoritative checkstyle/test gate. If a report claims a checkstyle or
test result, treat it as unverified and rely on Step 5.

Then, on the `## VERDIKT` line of each report:

- **`BLOCK`** — **[TREBA INPUT]** — stop, report every finding, ask how to
  proceed. Do NOT continue.
- **`WARNING`** — report the findings and ask whether to continue. Continuing
  means explicitly accepting that known risk for this ship run. Do NOT
  continue automatically.
- **`APPROVE`** — continue.
- **No verdict line at all, or the agent failed to run** — **[TREBA INPUT]** —
  report that clearly and ask whether to stop or proceed without the review.
  Do NOT assume `APPROVE`.
- Any `## TREBA ODLUKA` from either agent — relay it to the user, same as in
  Step 3.

If you fix findings here, refresh the changed-file list before Step 5 — and
re-run Step 2's classification, since a fix can pull a Java file into a diff
that had none.

---

## Step 5 — Hygiene

The final gate before committing, and the only place in this workflow allowed
to run Maven.

Skip this step entirely if Step 2 classified the diff as not build-relevant,
and say so in the report.

Otherwise, for **each affected service from Step 2** — not all five — from
inside that service's directory, run exactly one command:

```bash
mvn -o -q checkstyle:check test
```

Why this one line and not the previous two:

- **One invocation, not `checkstyle:check` then `test`** — one Maven boot, one
  dependency resolution, one compile pass instead of two. The checkstyle
  execution in these poms has no `<phase>`, so its default is `verify`, which
  `test` never reaches — the goal has to stay explicit, it does not come for
  free with `test`.
- **`-o` (offline)** — skips remote `maven-metadata` lookups on every
  dependency. Verified to pass in all five services here. If it fails with a
  *resolution* error (a genuinely missing artifact, not a test failure), re-run
  the same command without `-o` and note that in the report.
- **`-q`** — keeps the output to failures, which also keeps this conversation
  small.
- **No `mvn clean`** — incremental compilation is half the saving. Only add
  `clean` if a build result is actually suspect, and say why.

Run the affected services **one after another, never in parallel and never
with `-T`**. Parallel Maven would finish sooner but saturates every core at
once; this command deliberately trades wall-clock for a cooler machine.

If it fails: fix it inline, then re-run that one service's command. Never skip,
`@Disabled`, or weaken a failing test to get green. Run `mvn jacoco:report`
only if the user asked for coverage.

Report the per-service outcome as a short list, not one blanket "all green",
and name the services that were out of scope so the user knows what was not
run.

---

## Step 6 — Commit

1. Stage only the relevant files — specific paths, never `git add -A`, and
   never the excluded files from Step 1.5.
2. Commit with a clear imperative message summarising the **why**, not a
   restatement of the diff. No `Co-Authored-By` lines.

---

## Step 7 — Push

1. `git push -u origin <branch-name>`

---

## Step 8 — Report

Output a final summary:

- Branch pushed
- Affected services, and which of the five were out of scope
- Any step skipped by Step 2's classification, with the reason
- Review verdict (and the event-flow verdict, if that agent ran)
- Test classes written in Step 3, and the per-service Step 5 outcome
- How many Maven invocations the run actually cost
- Commit SHA
- The URL to open the PR manually:
  `https://github.com/indirkaric/order-management-app/compare/main...<branch>`

There is no `gh` CLI in this environment, so `/ship` stops here. Do not claim a
PR was opened, a review was requested, or anything was merged — hand the user
the link and let them do it.

---

## Cost budget

A `/ship` run has a CPU budget, and these are its hard limits:

- **At most one Maven invocation per affected service per pass**, plus the one
  batched run `oms-test-writer` makes in Step 3 when it actually wrote tests.
  That Step 3 run is the only exemption, and it exists because a test the agent
  never executed is not a verified test. Everything else combines its goals
  into one command — never a separate command per goal, never a command per
  test class.
- **Never Maven in a service the diff does not touch.** Five services × two
  goals costs ≈49 s of saturated CPU; one affected service costs ≈9 s.
- **Never Maven at all when Step 2 found nothing build-relevant.**
- **Never Maven in `oms-common-events/`** beyond the `install -DskipTests` in
  Step 2.5 — it has no checkstyle config and no tests.
- **Never Maven from a read-only review agent.** Step 5 owns the
  checkstyle/test gate; `oms-code-reviewer`, `oms-event-flow-auditor` and
  `oms-feature-builder` do not run Maven at all.
- **Never Maven in parallel**, across services or with `-T`.
- **Never the full `git diff main` in this conversation.** `--stat` here, full
  diff only inside the reviewer.

If a step seems to need more than this, stop and tell the user what it wants
and why, instead of spending it.

---

## Rules

- Never force-push or use destructive git commands.
- Never run `docker compose down -v` or anything else that would wipe local
  infra.
- Only this command may `git commit`/`git push` — invoking it is the user's
  explicit authorisation for those two actions, and for nothing else
  (`CLAUDE.md`, "Agent behavior").
- Follow `CLAUDE.md` and `.claude/rules/`.
- At any unexpected error, report clearly and stop.
