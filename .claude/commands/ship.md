# /ship — Full Ship Workflow

Orchestrate all steps directly. Do not hand the entire workflow to a subagent.
Individual steps spawn focused agents.

**User input**: $ARGUMENTS

Arguments may add scope notes, but they never replace the branch preflight and
never change which branch you are on.

Read `CLAUDE.md` (precedence, service table) and `.claude/rules/workflow.md`
(pipeline, handoff blocks, verdict semantics) before Step 1. This command is
the executable form of that pipeline — it sequences and gates, it does not
redefine any of it. Answer the user in Bosnian throughout.

At the start of each step output a progress line:
`▶ <Step Name>...`
At the end of each step output:
`✓ <outcome in one line>`

---

## Step 1 — Preflight And Analyze Changes

1. Run `git branch --show-current`, `git status --short`,
   `git diff --name-only main`, and `git diff main`.
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

1. Derive the changed-file list once via `git diff --name-only main` and reuse
   it for the rest of the run, refreshing it only if the diff actually changes.
2. Derive the affected services from those paths. Treat user arguments as
   additional scope notes only.
3. If `oms-common-events/` was touched, run `mvn clean install` inside it
   first — `order-service`, `identity-service` and `audit-log-service` resolve
   it as `0.0.1-SNAPSHOT` from `~/.m2`, so a stale install makes the later test
   runs lie.

---

## Step 3 — Tests Before Review

1. Launch **oms-test-writer** with the changed-file list from Step 2. It
   derives the untested classes itself — don't re-derive them here. If a
   previous step produced a `## KLASE ZA TESTOVE` block, pass it through
   verbatim as well.
2. If it reports that nothing needs a test, continue.
3. This runs *before* the review on purpose — the reason is in
   `.claude/rules/workflow.md` ("Pipeline"). Don't reorder these two steps.
4. If the agent returns a `## TREBA ODLUKA` block, relay it to the user with
   `AskUserQuestion`, then **continue that same agent via `SendMessage`** with
   the answer — do not spawn a fresh one, which would throw away the tests it
   already wrote. Never answer on the user's behalf.

---

## Step 4 — Code Review

1. Launch **oms-code-reviewer**, instructing it to review against `main` so
   staged and unstaged working-tree changes are both included.
2. If the diff touches `event/`, a `*EventListener`, or anything implementing
   `IDomainEventPublisher`, launch **oms-event-flow-auditor** in parallel and
   name the affected service and files in its prompt (it has no `Bash`, so it
   cannot read the diff itself).

Then gate on the `## VERDIKT` line of each report. The levels mean what
`.claude/rules/workflow.md` says they mean; what this command does with each:

- **`BLOCK`** — **[TREBA INPUT]** — stop, report every finding, ask how to
  proceed. Do NOT continue.
- **`WARNING`** — report the findings and ask whether to continue. Continuing
  means explicitly accepting that known risk for this ship run. Do NOT
  continue automatically.
- **`APPROVE`** — continue.
- **No verdict line at all, or the agent failed to run** — **[TREBA INPUT]** —
  report that clearly and ask whether to stop or proceed without the review.
  Do NOT assume `APPROVE`.
- Any `## TREBA ODLUKA` from either agent — relay it to the user and continue
  that agent with `SendMessage`, same as in Step 3.

If you fix findings here, refresh the changed-file list before Step 5.

---

## Step 5 — Hygiene

The final gate before committing. In **each of the five services**
(`identity-service`, `order-service`, `audit-log-service`, `oms-ui-service`,
`oms-storage-service`), from inside that service's directory:

1. `mvn checkstyle:check`
2. `mvn test`

If either fails: fix it inline, then re-run that service. Never skip,
`@Disabled`, or weaken a failing test to get green (`.claude/rules/testing.md`
— that is `[BLOCK]`). Run `mvn jacoco:report` only if the user asked for
coverage.

Report the per-service outcome as a short list, not one blanket "all green".

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
- Affected services
- Review verdict (and the event-flow verdict, if that agent ran)
- Test classes written in Step 3, and the per-service `mvn test` outcome
- Commit SHA
- The URL to open the PR manually:
  `https://github.com/indirkaric/order-management-app/compare/main...<branch>`

There is no `gh` CLI in this environment, so `/ship` stops here. Do not claim a
PR was opened, a review was requested, or anything was merged — hand the user
the link and let them do it.

---

## Rules

- Never force-push or use destructive git commands.
- Never run `docker compose down -v` or anything else that would wipe local
  infra.
- Only this command may `git commit`/`git push` — invoking it is the user's
  explicit authorisation for those two actions, and for nothing else
  (`.claude/rules/workflow.md`, "Git and infra in the pipeline").
- Follow `CLAUDE.md` and `.claude/rules/`; where this file and a rule differ,
  the rule wins (`CLAUDE.md`, "Precedence").
- At any unexpected error, report clearly and stop.
