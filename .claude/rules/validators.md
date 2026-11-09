# Validators

**Applies to:** `order-service`, `identity-service`.

- Live in `service/validator/`, one interface+impl per write operation
  (`ICreateXServiceValidator`, `IModifyXServiceValidator`).
- Shared field-level rules go in a `BaseXDataValidator` that concrete
  validators extend (see `BaseOrderDataValidator`) — don't copy-paste the
  same rule into two validators.
- Injected into the operation service and called as the FIRST line of the
  method body, before any other logic.
- Violations throw `BusinessException` (never `IllegalStateException` or a
  plain `RuntimeException`) — see [exceptions.md](exceptions.md).

Like every other class in these services, a validator validates its injected
dependencies with `Check.isNotNull` in the constructor and its parameters with
`Check.*` at the start of every public method (see "Defensive checks" in
`CLAUDE.md`).

Each new validator ships with its test in the same change — one test method
per rejection branch, per [testing.md](testing.md).
