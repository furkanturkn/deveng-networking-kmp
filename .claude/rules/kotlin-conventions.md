---
paths: ['**/*.kt', '**/*.kts', '**/composeResources/**/strings.xml']
---

# Kotlin / KMP — Conventions & Style

Extends `common.md`. Style and quality rules for Android / Kotlin Multiplatform. Structure is in `kotlin-architecture.md`; naming in `kotlin-naming.md`; deveng-core API usage in `kotlin-deveng-core.md`.

---

## deveng-core-kmp

When the project depends on **deveng-core-kmp**, prefer the library's existing APIs over reimplementing functionality. Before adding UI, camera, platform (dial/clipboard/maps/URL/share/location), permissions, media, or pagination features, follow `kotlin-deveng-core.md` (loads automatically alongside this file for Kotlin) and its full API list in `references/kotlin/deveng-core-reference.md` (read on demand from the pinq-doq mount, e.g. `.pinq-doq/references/kotlin/deveng-core-reference.md`), and use the core APIs it lists.

---

## Jetpack Compose

- Top-level composable: `[Feature]Screen(...)`
- Content composable: `[Feature]ScreenContent(...)`
- Composables must be logic-free. Pass UI instructions, not domain state.

```kotlin
// ❌ Wrong — implies domain logic
isMuted

// ✅ Correct — direct UI instruction
isMutedIconVisible
```

- Visibility states must use positive framing:

```kotlin
// ❌ Wrong
if (state.uiState.isProductListEmpty) { ... }

// ✅ Correct
if (state.uiState.isProductListVisible) { ... }
```

### Confirmation Dialogs

- Show a confirmation dialog before every destructive or irreversible action (delete, clear, reset, etc.).
- The question must reference the specific action — not a generic prompt.
- Confirm button label: the action verb ("Delete", "Clear", "Reset"). Cancel button label: always "Cancel" — never "Yes / No".
- Style the confirm button as a destructive/error button when the action deletes or permanently removes data.

### Preview Standards
- Always wrap previews in `AppTheme`.
- Use preview parameter providers for dynamic data.
- Provide named previews for `empty`, `loading`, `error`, and `success` states.
- Include edge cases: long text, special characters.
- File under `preview/` directory.
- Naming: `[Entity]PreviewProvider`, `Fake[Entity]DataProvider`

---

## Compose Multiplatform String Resources

- In `composeResources/**/strings.xml`, write apostrophes and quotes as plain characters: `l'abonnement`, `"photo"`. Compose resources resolve only `\uXXXX`, `\n`, `\t` and `\\`; every other escape, such as `\'` or `\"`, is shown with its backslash. This differs from Android `res/values`.
- Keep a unit test that reads every `strings.xml` under `composeResources` and fails with the file and key when a value contains an escape Compose shows literally, so a new string cannot bring it back.

---

## Class Structure & Member Ordering

Inside a class, maintain this order:
1. Fields
2. Properties
3. Constructors
4. Public methods
5. Private helper methods

Definitions must appear above their usage sites.

---

## Access Modifiers

- Default everything to `private`.
- Do not expose members unless strictly required.
- Public APIs must be intentional and minimal.

---

## Validation Errors — Boolean Flags, Not Strings

- Each distinct validation error is an `is…Visible: Boolean` flag in `State` (e.g. `isEmailEmptyErrorVisible`). The ViewModel never stores a localized error string.
- One flag per distinct condition — do not collapse "empty" and "invalid" into one flag.
- The UI resolves the message at render time via `stringResource(...)`. See `.pinq-doq/references/kotlin/error-handling.md`.
- This is presentation state, not error signalling — domain/data still throw real exceptions (see `common.md`).

---

## Formatting — Blank Lines

- Place a blank line after a closing `}` / `)` block, except when the next line is another closing brace, end of file, or a directly chained call (`.collect()`, `.map()`).

---

## ScreenContent Extraction

- A `[Feature]ScreenContent.kt` file contains only the ScreenContent composable + its `@Preview`. Every other composable lives in the feature's `component/` folder (singular), one composable per file. See `.pinq-doq/references/kotlin/formatting.md`.

---

## Pre-API / Fake Data

- For features without a real API yet, funnel mock data through the UseCase (which returns a `Fake[Entity]DataProvider`), not through the ViewModel directly — so swapping to a repository later needs no ViewModel change. Pattern: `.pinq-doq/references/kotlin/fake-data.md`.

---

## Null Safety

- When using `!!`, add an inline comment explaining why it is safe — or refactor to avoid it.

---

## Dependency Management — Internal Libraries

`deveng-core-kmp` and `deveng-networking-kmp` are internal libraries that always exist locally. Never resolve them from Gradle's build cache — always reference them directly from the local file system.

To see live changes from an internal library in a consumer project, include it as a composite build:

```kotlin
// settings.gradle.kts
includeBuild("../deveng-core-kmp") {
    dependencySubstitution {
        substitute(module("global.deveng:core-kmp")).using(project(":deveng-core"))
    }
}
```
