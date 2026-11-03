---
paths: ['**/*.kt', '**/*.kts']
---

# Kotlin / KMP — Naming

Extends `common.md` (variables, constants, packages, callbacks). This file adds the Kotlin/KMP-specific naming. Structure is in `kotlin-architecture.md`; style and quality in `kotlin-conventions.md`. Full delta and examples: `.pinq-doq/references/kotlin/naming.md`.

---

## File Naming

| Type | Pattern | Example |
|---|---|---|
| Screen | `[Feature]Screen.kt` | `DashboardScreen.kt` |
| ViewModel | `[Feature]ViewModel.kt` | `DashboardViewModel.kt` |
| Contract | `[Feature]Contract.kt` | `DashboardContract.kt` |
| UseCase | `[Action][Entity]UseCase.kt` | `FetchUserStatusTypesUseCase.kt` |

---

## Resource Naming (Android)

- `string.xml` IDs must include a module prefix.

```xml
<!-- ✅ Correct -->
<string name="onboarding_feat_too_much_connection">

<!-- ❌ Wrong -->
<string name="too_much_connection">
```

---

## MVI — State, Event & Effect

### State
- Use descriptive, state-representative names. Never generic names.
  - ✅ `branchDescription`, `userList`, `isRegistrationFormValid`
  - ❌ `description`, `user`, `isFormValid`
- Boolean fields: prefix with `is`, `has`, `should`, `can`.
- Lists: plural form or suffix with `List`.

### Event
- Use action-based, imperative verbs.
  - ✅ `ClickCompanyInfoButton`, `ClickLoginButton`, `EnterPhoneNumber`, `ToggleReceiptOption`
  - ❌ `ClickCompanyInfo`, `PhoneNumber`
- Navigation events: `NavigateToProfileScreen`, `NavigateBack`

### Effect
- Use outcome-based names.
  - Navigation: `NavigateToOtpScreen`
  - UI: `ShowError`, `ShowSuccess`
  - System: `RequestPermission`, `OpenExternalApp`

---

## Domain Layer Naming

- ✅ `saveBranchInfo()` — create or update
- ✅ `updateBranchInfo()` — update only
- ✅ `fetchBranchInfo()` — fetch a list or item
- ❌ Avoid `put`, `post` — these expose HTTP implementation details.

---

## Use Case Naming — Verb Prefixes

- Pattern: `[Verb][Entity]UseCase`. One `operator fun invoke()` per use case.
- Use **`Fetch`** (not `Get`) for all reads — local cache, fake data, or remote.
- Verbs: `Fetch` (read), `Add`/`Create` (new), `Update`/`Change`, `Delete`, `Send`/`Submit`/`Post`, `Upload`, `Verify`/`Authenticate`, `Handle` (orchestrate), `Initialize`/`End` (session).

---

## String Resources — Triad + Symbols

- Resource names use the triad `{feature}_{context}_{purpose}` (snake_case), extending the module-prefix rule above.
- Symbolic characters (arrows, checks, close icons) are `DrawableResource` vector drawables — never unicode text literals.
