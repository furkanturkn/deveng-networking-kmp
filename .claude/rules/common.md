# Common Coding Standards

These rules apply to all projects and all languages unless a language-specific file overrides them.

**What belongs in this file:** standards for the code and text a developer writes (naming, structure, errors, comments, tests). How a task, pull request or automated runner works (branching, review steps, which devices to run, which issues to open) does not belong here; it goes into the instructions of the tool that does the work, for example `runner/skill/references/implementation.md`.

---

## Stack-specific rules

This file always loads. Stack-specific rules load automatically only when you touch a matching file, so they may not be in context yet. Before writing stack code, make sure the relevant rule is loaded:

- **Kotlin / KMP / Compose** (`*.kt`, `*.kts`) → `kotlin-architecture.md` (structure, MVI, shared module), `kotlin-naming.md`, `kotlin-conventions.md` (style), `kotlin-testing.md` (test stack and what every change must test), plus `kotlin-deveng-core.md` when the project depends on deveng-core-kmp. Deep references live in `.pinq-doq/references/kotlin/` (architecture, data-layer, mvi-pattern, naming, shared-module, testing, …) and `.pinq-doq/references/kotlin/deveng-core-reference.md`, read on demand.
- **C# / .NET** (`*.cs`, `*.csproj`, `*.sln`) → `dotnet-conventions.md`.
- **Web** (`*.ts`, `*.tsx`, `*.js`, `*.jsx`) → `web-testing.md` (proving a web change in a browser with Playwright). Setup and examples: `.pinq-doq/references/web/e2e-playwright.md`.

---

## Naming Conventions

- Use English strictly. No Turkish identifiers, comments, or abbreviations.
- String literals in code are English too: exception, validation and log messages, tool/API descriptions, and the test assertions that check them. Follow this even where older code in the repo is Turkish. Exceptions: localized user-facing copy (it lives in the string resources) and quoted examples of what a user might type.
- No abbreviations anywhere. Write it out.
- Variables are nouns. Methods are verbs. Names must reflect context, not just type.

| Identifier | Convention | Example |
|---|---|---|
| Classes & Interfaces | PascalCase | `DashboardRepository` |
| Functions / Methods | camelCase, verb-based | `loadUserData()` |
| Variables | camelCase, noun-based | `userName`, `isLoading` |
| Constants | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase, dot-separated | `com.protein.android.domain.user.usecase` |
| Lambda / Callback Parameters | camelCase, contextual prefix | `onRegistrationItemClick` |

- Do not prefix interfaces with 'I' in Kotlin.
- Do not add an `Async` suffix to async methods — the return type already signals it.
- If a method only checks → use `validate`, `check`. If it creates or saves → use action verbs.

---

## Separation of Concerns & Single Responsibility

- Each code unit must have exactly one responsibility.
- A function name that joins two actions with "And" (`saveAndClose`, `saveToPhotosAndDeleteTempFile`) does two jobs: split it into one function per action and call them in sequence from the caller. "And" between two conditions in a test name (`…_withRotatedPhotoAndLowFreeHeap_…`) is fine.
- Extract large logical blocks (retry logic, setup, try-catch) into well-named sub-functions.
- Apply SRP without over-engineering. Do not create abstractions without real value.
- Shared libraries (pinqnuqets, deveng-core-kmp, deveng-networking-kmp, …) hold only what any product would need on its own. When moving product code into one, keep the product's conventions (header and route names, error codes and messages, defaults, policies such as which errors count as client errors) in the product. A setting counts as a convention when only one product would change it: do not add it to keep that product's behavior; the product rebuilds the behavior on the library's extension points (its own middleware, resolver, endpoint). Technology-specific parts go to separate optional packages. Check the moved code, its comments and its docs for product names before opening the pull request.
- Layer dependency rules:
  - Application depends only on Domain.
  - Domain must not depend on any other layer; Domain may only depend on other Domain modules.

---

## Error Handling

- Never hide stack traces — always pass the original exception, not just `ex.Message`.
- Never swallow errors silently. Every error must be logged.
- Do not use sentinel values (`-1`, `0`, `null`, empty string) to signal errors.
  - Value required but missing → throw an exception.
  - Value optional → model it as optional; do not fake validity.

---

## Null & Optional Safety

- If something can be optional, make it optional — do not fake non-nullability.
- If a method cannot return null, its return type must not be nullable.
- When using force-unwrap (`!!` in Kotlin, `!` in Swift), add an inline comment explaining why it is safe — or avoid it entirely.

---

## Magic Values & Configuration

- Never hardcode tunable or performance-sensitive values. Use named constants or config.
- Never use inline strings or numeric codes for domain-meaningful values. Use enums or constants.
- Never assign hardcoded numeric values to variables ending with `Id`. If a static value is needed, it should be an enum or well-named constant.

---

## Code Style & Readability

- Use guard clauses to fail fast. Avoid deeply nested `if` blocks.
- Extract long or complex conditions into well-named variables or methods.
- Do not place logic inside object initializers. Compute values into named variables first.
- State fields always assigned the same value must be set outside conditional branches.
- Variable names must clearly describe what the data represents, not its technical type.
- Do not create variables that simply mirror a parameter — unless they add validation or semantic meaning.
- Attributes/annotations must be on a separate line above the target member, never inline.

---

## Comment Standards

- Do not add comments by default. Prefer readable code, meaningful naming, and small functions.
- Do not write comments that only restate what the code already says.
- Only comment information that cannot be inferred from the code and can be verified from the existing context:
  - business rules and deliberate exceptions,
  - technical constraints and temporary workarounds,
  - security or performance trade-offs,
  - unexpected behavior of external systems,
  - deliberate implementations that look like a mistake at first glance.
- Do not guess a decision's rationale if it was not given by the user or is not explicit in the project. Ask the user for clarification when it is needed.
- If the need for a comment can be resolved with better naming or a refactor, improve the code first instead.
- When changing code, also check the comments tied to it: update or remove any that are no longer valid, but never delete a comment without understanding the context and rationale it carries.
- In `TODO` comments, state the rationale, a tracking reference, and the removal condition where possible.
- Do not auto-generate line-by-line comments just to explain generated code.
- Keep comments concrete and in plain words. Clarity comes before brevity: do not shorten a comment if that removes meaning or makes it harder to follow. How to word a comment is in "Writing Style" below.

**Core test:** if removing a comment would not lose an important, verifiable rationale or warning, the comment was unnecessary.

---

## Writing Style (comments, XML docs, KDoc, README, PR text)

Text you write must be easy to understand on the first read. Clear wording matters more than short or clever wording, and shortening must never remove meaning. The company guide is `meta/document-writing-guide.md` (clear, understandable, plain); these are the rules that apply to code and pull requests.

- One idea per sentence. Say who does what with plain verbs. Write "because", "so", "if", "when" out in full instead of arrows (`→`), slashes or semicolon chains.
- Define a term the first time it appears, or use the plain description instead. Decision codes (`S238`), internal names ("runner", "shipped panel", "pre-contract") and jargon ("sparse", "floor", "snapshot") mean nothing to a reader outside the discussion. For a decision, write the question and the chosen option in words.
- Use one word for one thing and keep it everywhere: the word the code or the API uses (`rowCount`, not "total" in one place and "count" in another; "log", not "entry", "record" and "match" mixed). Do not use a word both as a noun and as a verb ("page" the query).
- Comments, XML docs and KDoc describe how the code works today. History, incident stories and "this used to be" go into the pull request or commit message.
- A claim must be true as written. State its limit next to it ("exact", "all", "never" need a reason or an exception). When behavior changes, search the docs, README files, tool descriptions and PR text for the old statement and fix it.
- Shared packages never name a product in their text ("rindle-backend's contract").
- Before you finish, read each sentence you wrote as someone who has not seen the task. If you had to know something outside the text to follow it, add that or remove the sentence.

---

## Constructor & Dependency Naming

Constructor parameters must clearly express what they configure, not just repeat the type name.

```
// ❌ Wrong
RabbitMQService(options)

// ✅ Correct
RabbitMQService(rabbitOptions)
```

---

## Health & Connectivity

- Services must expose connectivity state via `isConnected`.
- Do not rely solely on exceptions for system health checks.
- Prepare for HealthCheck integration from the start.

---

## Configuration Validation

- Required config fields must be validated at startup.
- Fail fast during application boot, not at runtime.

---

## Integrating Shared Services

- Before integrating a shared service (chat, logging, push, identity) into a product, read how a product that already uses it does so: the client setup, the backend endpoint that issues its tokens, deep links and notifications. Follow the same flow.
- Check the service's response shapes in the service's own source, not only in a consumer: a consumer can read a shape the service no longer sends.
- Add to the shared library only what the new product needs and no existing product has.

---

## Moving or Replacing Existing Code

When a change moves code to a shared library or replaces a whole layer (error handling, logging, paging, data access), prove with tests written against the old code that the behavior stays the same.

- A characterization test records what the old code does today. Test through public behavior (HTTP responses, logs sent, values a service returns), not through the classes that will be removed, so the same tests still compile after the move. Keep the code that names the old classes in one test helper.
- These tests must pass on the old code without any of the new code, and they must still pass after the move. A test that passed before and fails after is a regression, so fix the code.
- Before the old code is removed, list the open pull requests that change it (`gh pr list --state open` in every repo that holds it). Port what they add to the new code, or say in both pull requests which one must follow the other. Example: a bridge rewritten in Kotlin while a .NET bridge pull request added a tool; the tool was lost and nothing could publish the list.
- A move changes no behavior, and the move does not change any expected result of these tests. If the new library behaves differently, keep the old behavior in the code that uses it (a resolver, writer, middleware or small wrapper). A behavior change that the new library makes possible is made separately after the move, with a note on what changes, why it helps and which clients it can affect.
