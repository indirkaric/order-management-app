# Controllers

**Applies to:** `order-service`, `identity-service`. `oms-ui-service` is a
gateway/BFF with a different shape — read its existing controllers before
applying anything here to it.

- `@RestController`, `@RequestMapping(path = "/api/v1/<plural>", produces =
  MediaType.APPLICATION_JSON_VALUE)`, `@Tag(name = "...", description =
  "...")`.
- Every endpoint: full Javadoc, `@Operation` + `@ApiResponses` (+
  `@Parameters` for query params) `[WARN]`,
  `@PreAuthorize(Constants.Authority.X_Y_AUTHORITY)` `[BLOCK]`,
  `@Valid @RequestBody` for bodies `[BLOCK]`, `Check.*` on every parameter
  `[BLOCK]`, delegate to exactly one `*ResponseCreatorService` (or a bare
  operation service for delete) `[BLOCK]`, wrap the result in `ResponseEntity`
  with the right status (`201` create, `200` read/update, `204` delete or empty
  list, `404`/`422` documented in `@ApiResponses` even though they're thrown,
  not returned, from this method).
- Class members are `final` wherever the language allows it — but controller
  **methods are never `final`** `[BLOCK]`: Spring's method-security proxy
  (CGLIB) must be able to override them to route through `@PreAuthorize`
  interception; a `final` method breaks that and produces an NPE on every
  injected field the moment the endpoint is actually called (not caught by
  `mvn test` with mocks, only on live traffic, since method-security proxies
  are only built for the real Spring context). This was the root cause of a
  real incident across `identity-service`, `order-service` and
  `audit-log-service`.
