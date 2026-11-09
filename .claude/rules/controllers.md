# Controllers

**Applies to:** `order-service`, `identity-service`. `oms-ui-service` is a
gateway/BFF with a different shape — read its existing controllers before
applying anything here to it.

- `@RestController`, `@RequestMapping(path = "/api/v1/<plural>", produces =
  MediaType.APPLICATION_JSON_VALUE)`, `@Tag(name = "...", description =
  "...")`.
- Every endpoint: full Javadoc, `@Operation` + `@ApiResponses` (+
  `@Parameters` for query params), `@PreAuthorize(Constants.Authority.X_Y_AUTHORITY)`,
  `@Valid @RequestBody` for bodies, `Check.*` on every parameter, delegate to
  exactly one `*ResponseCreatorService` (or a bare operation service for
  delete), wrap the result in `ResponseEntity` with the right status (`201`
  create, `200` read/update, `204` delete or empty list, `404`/`422`
  documented in `@ApiResponses` even though they're thrown, not returned,
  from this method).
- Class members are `final` wherever the language allows it — but controller
  **methods are never `final`**: Spring's method-security proxy (CGLIB) must
  be able to override them to route through `@PreAuthorize` interception; a
  `final` method breaks that and produces an NPE on every injected field the
  moment the endpoint is actually called (not caught by `mvn test` with
  mocks, only on live traffic, since method-security proxies are only built
  for the real Spring context). This was the root cause of a real incident
  across `identity-service`, `order-service` and `audit-log-service`.

A `final` method on a `@RestController` is a **blocking** review finding, not
a style nit — see the review verdict rules in `CLAUDE.md`.
