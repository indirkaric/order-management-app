# Tests

**Applies to:** all 5 services. The *conventions* are universal; the *shape*
of what you mock is not — `audit-log-service`, `oms-ui-service` and
`oms-storage-service` have no JPA/mapper layer, so open a sibling `*Test` in
the same service before writing a new one.

## Conventions

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock` fields,
  dependencies wired manually in `@BeforeEach void setUp()` — no
  `@InjectMocks`) + AssertJ (`assertThat`, `assertThatThrownBy`,
  `assertThatCode`). Never JUnit's own `assertEquals`/`assertTrue` `[WARN]`.
- Test class = `<ClassUnderTest>Test`, same package as the class under test,
  under `src/test/java`.
- Method names read as a sentence: `should<ExpectedBehavior>()`, e.g.
  `shouldIgnoreRequestedCustomerIdWhenCallerIsNotElevated`. No `test_`
  prefixes, no leaning on `@DisplayName` instead of a readable method name.
- Mock only direct collaborators; verify the behavior that matters
  (`verify(x).method(...)`, an `ArgumentCaptor` when the captured value itself
  needs an assertion) — don't assert on incidental implementation calls.
- A new `Service`/`Validator`/`Controller` ships with its test in the same
  change — not "tests later" `[WARN]`. A failing `mvn test` is `[BLOCK]`.
  JaCoCo runs via `mvn jacoco:report` per service.

## Coverage to aim for

The happy path, every validator rejection branch, every
`EntityNotFoundException`/`BusinessException` branch, and — for services that
publish events — that `applicationEventPublisher.publishEvent(...)` was called
with the right event type and fields (see [events.md](events.md)).

Never skip, `@Disabled`, or weaken an assertion to get a green run `[BLOCK]`.
If a test fails because the production code is wrong, fix the production code
or report the defect — don't make the test agree with the bug.

## Running them

```bash
mvn test                      # whole service
mvn test -Dtest=ClassName     # one test class
mvn test -Dtest=ClassName#method   # one test method, for debugging
```

Always from inside the affected service's directory — there is no aggregator
build at the repo root.

## Who writes them

"Ships with its test in the same change" is satisfied at the level of the
**change**, not of a single agent: the author of a class is never the author of
its coverage. The handoff, and the rule that review runs only once the test
list is empty, are in [workflow.md](workflow.md).
