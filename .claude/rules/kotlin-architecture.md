---
paths: ['**/*.kt', '**/*.kts']
---

# Kotlin / KMP — Architecture

Extends `common.md`. The structural rules for Android and Kotlin Multiplatform features: Clean Architecture, MVI, the uniform data layer, and when to use a shared module. Naming is in `kotlin-naming.md`; style and quality in `kotlin-conventions.md`; deveng-core API usage in `kotlin-deveng-core.md`. Deep dives live in `.pinq-doq/references/kotlin/`.

---

## Clean Architecture + MVI

- Features split into `data/` + `domain/` + `presentation/`. Application depends only on Domain; Domain depends on nothing outward (see `common.md`).
- Presentation is MVI: one `[Feature]Contract.kt` per feature holding `Event` / `State` / `Effect`; the ViewModel is the only writer of state.
- The Screen holds no decisions. It collects effects and makes the platform call an effect asks for (navigate, open a URL, copy to the clipboard, show a snackbar). When such a call fails, the Screen sends the failure as an `Event` (e.g. `HandleExternalUrlOpenFailure(url)`), and the ViewModel decides what happens next through new effects, so the decision has a ViewModel test.
- Full package skeleton, data-layer recipe, and Screen/ScreenContent/ViewModel split: `.pinq-doq/references/kotlin/architecture.md`, `.pinq-doq/references/kotlin/data-layer.md`, `.pinq-doq/references/kotlin/mvi-pattern.md`, `.pinq-doq/references/kotlin/viewmodel-patterns.md`.

---

## Data Layer (uniform)

Every feature's `data/` has the same shape — no improvisation:
- `[Feature]Service` — concrete class (no interface), inline endpoint strings, returns the response DTO directly. No `Result<T>`/`Flow<T>`/custom wrappers; let exceptions propagate.
- DTOs — `@Serializable` `data class`, suffix `Request` / `Response` (never `Dto`). Nullable fields default to `null`, list fields to `emptyList()`; never a sentinel string. `@SerialName` only on backend-name mismatch.
- A `Request` field the server needs (an id, a type such as `messageType`) never has a default value: `kotlinx.serialization` leaves default values out of the JSON unless `encodeDefaults` is on, so the field silently disappears. Pass it at the call site. A test of what is sent reads the raw JSON, not a decode back through the same `Request` class, which would fill the default in again.
- Mappers — a **class** with `mapToDomain(response): Domain` (not a top-level extension); compose by injecting the inner mapper. Convert nullable DTO fields to non-null domain defaults here.
- Repository — `[Feature]RepositoryImpl` implements a `domain/` interface; takes service + mappers only (no Dispatcher/Scope/Settings); returns domain types. No retries/caching/fallbacks.
- No `local/` datasource in a feature — persisted state lives in a shared `core` key-value store.
- Details and DI wiring: `.pinq-doq/references/kotlin/data-layer.md`.

---

## Shared Module

Most features are self-contained. Put code in a **shared** module when the same concrete artifact — the same service method, request/response or domain model, or use case — **is used by two or more feature modules, or will be (in the work at hand) by a second feature you can name.**

- **Reuse before create:** check `shared/` (then the relevant feature) for an existing equivalent before adding a service, model, mapper, or use case. If it's already in `shared/`, depend on it — creating a duplicate is a defect.
- The trigger is concrete reuse you can point to — **not** that endpoints share a backend service/API, and **not** "might be reused someday" (speculative → keep it feature-local).
- Default to feature-local; **promote to `shared/` when a second feature actually needs it** (moving it is the normal path, not a mistake).
- The shared module is **not** feature-packaged: no feature name in the path. It holds only `shared/data`, `shared/domain`, `shared/presentation` (packages `shared.data`, `shared.domain`, `shared.presentation`).
- Name artifacts by concept (`AuthRepository`, `AuthService`, `ProductsRepository`) — never `SharedRepository` / `SharedService`. Only DI modules carry the `Shared` prefix (`SharedDataModule`, `SharedDomainModule`, `SharedPresentationModule`).
- Decision checklist, package structure, and examples: `.pinq-doq/references/kotlin/shared-module.md`.

---

## Code-generation scripts

A suite of standalone KMP code generators lives at `.pinq-doq/scripts/` — feature `data`/`domain`/`presentation` scaffolds, request/response and domain models, mappers, use cases, DI + navigation registration, and string resources. **Prefer them over hand-writing this boilerplate.**

- **Catalog:** `.pinq-doq/scripts/README.md` lists every script with its purpose and how to invoke it (configuration, target-project flags, `--help`).
- **Each runs individually — you do not need a workflow to use one.** Reach for a single script when you've built the rest by hand and just need one piece (e.g. `register_di_modules.py` to wire a feature you already wrote, or `add_string_resource.py` for a lone string).
- Three skills orchestrate these scripts end-to-end: `api-endpoint-integration` (backend — data → domain → use case → DI), `presentation-scaffold` (presentation — screen/MVI, navigation, components, strings), and `add-feature` (runs both and wires the ViewModel to the use case). Reach for `add-feature` when building a whole feature; the catalog is for à-la-carte use outside those flows.
