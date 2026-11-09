# Tests

**Applies to:** all 5 services. The *conventions* are universal; the *shape*
of what you mock is not — `audit-log-service`, `oms-ui-service` and
`oms-storage-service` have no JPA/mapper layer, so open a sibling `*Test` in
the same service before writing a new one.

## Conventions

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock` fields,
  dependencies wired manually in `@BeforeEach void setUp()` — no
  `@InjectMocks`) + AssertJ (`assertThat`, `assertThatThrownBy`,
  `assertThatCode`). Never JUnit's own `assertEquals`/`assertTrue`.
- Test class = `<ClassUnderTest>Test`, same package as the class under test,
  under `src/test/java`.
- Method names read as a sentence: `should<ExpectedBehavior>()`, e.g.
  `shouldIgnoreRequestedCustomerIdWhenCallerIsNotElevated`. No `test_`
  prefixes, no leaning on `@DisplayName` instead of a readable method name.
- Mock only direct collaborators; verify the behavior that matters
  (`verify(x).method(...)`, an `ArgumentCaptor` when the captured value itself
  needs an assertion) — don't assert on incidental implementation calls.
- A new `Service`/`Validator`/`Controller` ships with its test in the same
  change — not "tests later". JaCoCo runs via `mvn jacoco:report` per
  service.

## Coverage to aim for

The happy path, every validator rejection branch, every
`EntityNotFoundException`/`BusinessException` branch, and — for services that
publish events — that `applicationEventPublisher.publishEvent(...)` was called
with the right event type and fields (see [events.md](events.md)).

## Running them

```bash
mvn test                      # whole service
mvn test -Dtest=ClassName     # one test class
mvn test -Dtest=ClassName#method   # one test method, for debugging
```

Always from inside the affected service's directory — there is no aggregator
build at the repo root.

## Who writes them (ownership)

"Ships with its test in the same change" is satisfied at the level of the
**change**, not of a single agent:

1. `oms-feature-builder` writes production code only, and ends its report with
   a `## KLASE ZA TESTOVE` block listing every class that needs a test.
2. The main agent then runs `oms-test-writer` on that list, in the same turn.
3. Only after that does `oms-code-reviewer` run — otherwise it flags a test gap
   that is the workflow's design, not a defect.

A change is not done while that list is non-empty. `/ship` enforces this
ordering (Step 3 before Step 4).
