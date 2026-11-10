# Exceptions

**Applies to:** all 5 services.

- `EntityNotFoundException` → 404, `BusinessException` (carries a list of
  messages) → 422, both thrown from the service layer, both handled centrally
  in each service's `GlobalExceptionHandler` (`@ControllerAdvice`), never
  caught and swallowed in a Controller or Service `[BLOCK]`.
- `IllegalArgumentException` (what `Check.*` throws) → 400, and
  `MethodArgumentNotValidException` (bean validation) → 400, also handled
  there.
- Every handler logs via SLF4J before building the response `[WARN]`.

| Thrown | HTTP | Thrown from |
|---|---|---|
| `EntityNotFoundException` | 404 | service layer, on a missing lookup |
| `BusinessException` | 422 | service layer / validators, business rule violated |
| `IllegalArgumentException` | 400 | `Check.*`, i.e. a programming/contract error |
| `MethodArgumentNotValidException` | 400 | bean validation on `@Valid @RequestBody` |

Throwing a bare `RuntimeException` or an `IllegalStateException` for a
business rule is `[BLOCK]`, as is a missing lookup that returns `null`/empty
instead of throwing `EntityNotFoundException`. The one place a
`RuntimeException` is deliberately caught and logged rather than propagated is
the Kafka bridge in the event listener — see [events.md](events.md).
