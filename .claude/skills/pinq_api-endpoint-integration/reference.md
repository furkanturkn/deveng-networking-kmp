# API Endpoint Integration — Reference

Use this when you need details on script outputs, config, or naming. The main workflow is in [SKILL.md](SKILL.md).

## Discovering scripts

List the scripts with `ls .pinq-doq/scripts/` and read each script's `--help` for current parameters. The integration scripts are:

- **Layer scaffolds:** `generate_data_layer.py`, `generate_domain_layer.py` (presentation scaffolding lives in the `presentation-scaffold` skill)
- **Models and code:** `generate_data_model.py`, `generate_domain_model.py`, `generate_mapper.py`, `generate_use_case.py`, `add_service_method.py`, `add_repository_method.py`
- **Registration / impl:** `register_mapper.py`, `register_use_case.py`, `add_repository_impl.py`, `register_di_modules.py`

## Script types and behaviour

Scripts either **write files** into the project (file-generation) or **print instructions** (Target Path + Code) that you must apply in the workspace (instruction-based). Paths and packages are determined by **config.json** only (see [Configuration](#configuration)).

### project_root (target path)

When you run the scripts against a project **other than** the one the scripts live in, generated files and instruction-based patches would otherwise resolve under the wrong root. To target your app workspace, pass the project location on every relevant call:

- **Scripts that accept `--project-root`** (absolute path to the target project root): `add_repository_method.py`, `add_repository_impl.py`, `register_mapper.py`, `register_use_case.py`, `register_di_modules.py`. Example: `--project-root /Users/you/StudioProjects/my-app`.
- **`generate_use_case.py`** takes neither `--config` nor `--project-root`: it reads the target root from the `AI_SCRIPTS_PROJECT_ROOT` env var (falling back to the current directory) and its config from `config.json` relative to that root. Set `AI_SCRIPTS_PROJECT_ROOT=<path>` to target another project.
- **Layer scaffolds** (`generate_data_layer.py`, `generate_domain_layer.py`) and the model/mapper generators accept **`--config`**: point them at the target project's config with `--config <path-to-your-config.json>` so `base_path` resolves under that project.
- **Behaviour:** Output paths (e.g. `composeApp/src/commonMain/kotlin/...`) are resolved relative to the resolved root. `add_repository_impl.py` reads the repository interface and service files from disk, so the resolved root must be the target app or it will report "file not found" / "method not found".
- **Usage:** Use the same target for the whole integration so all generated files and patches land in one project.

### File-generation scripts (write files)

| Script | Description |
|--------|-------------|
| `generate_data_layer.py` | Creates data layer scaffold (folders + `{Feature}DataModule`, or `SharedDataModule` with `--shared`). For shared, creates only `shared/data/...` and empty DI module. |
| `generate_domain_layer.py` | Creates domain layer scaffold (folders + `{Feature}DomainModule`, or `SharedDomainModule` with `--shared`). |
| `generate_data_model.py` | Creates request/response data class in `.../model/request/` or `.../model/response/`. Naming: `{Entity}Request`, `{Entity}Response`. |
| `generate_domain_model.py` | Creates domain model data class in `.../domain/model/`. No Request/Response suffix. |
| `generate_mapper.py` | Creates mapper (e.g. `toDomain()`) in `.../model/mapper/`. Naming: `{Entity}Mapper`. |
| `generate_use_case.py` | Creates use case class in `.../domain/usecase/`. Naming: `{Action}UseCase`. |

- Use `--output-json` to print file contents as JSON instead of writing to disk (where supported).
- They write files **relative to the resolved root** (config `base_path`, or `--project-root` where supported). When targeting another project, point `--config`/`--project-root` at it so generated files appear in that workspace. Otherwise create or update those files from the printed paths and contents.

### Instruction-based scripts (you apply Target Path + Code)

| Script | Description |
|--------|-------------|
| `add_service_method.py` | Prints **Target Path** + **Code** for adding a suspend function to the service class. Endpoint must start with `/api/`. Create or update the service file at the given path with the code. |
| `add_repository_method.py` | Prints **Target Path** + **Code** for adding a method to the repository interface. Create or update the repository file at the given path. |
| `register_mapper.py` | Prints Target Path (data DI module) + import and `singleOf(::Mapper)`. Apply to the data module file. |
| `register_use_case.py` | Prints Target Path (domain DI module) + import and `singleOf(::UseCase)`. Apply to the domain module file. |
| `add_repository_impl.py` | Prints Target Path + Code for the repository implementation method. **Requires** the repository interface and service to exist at paths the script resolves (pass `--project-root` so it reads the right files). Apply to the repository impl file. |
| `register_di_modules.py` | Prints Target Path (initKoin.kt) + imports and module names. Apply to `initKoin.kt` (or path from config `initKoin_path`). |

- They do **not** write files; they output **Target Path:** and **Code:**.
- **You must apply** that code to the indicated file (create the file if it does not exist, with the necessary package/class wrapper so the snippet compiles).

## Script output formats

### File-generation scripts

- They write files into the project and print a short summary (or print file contents as JSON with `--output-json`).
- **If files are written**, confirm paths from the printed summary. **If not** (e.g. `--output-json`), create or update the files in your workspace from the printed paths and contents.

Example JSON shape (`--output-json`):

```json
{
  "files": [
    {
      "path": "feature/userprofile/domain/model/Profile.kt",
      "content": "package ...\ndata class Profile(...)"
    }
  ],
  "message": "Domain model Profile generated successfully"
}
```

### Instruction-based scripts

- They output **structured text**, not file writes:
  - **Target Path:** <relative_path_to_file>
  - **Code:** <code_to_add>
- **You must apply** the Target Path + Code to the indicated local file (create the file if it does not exist, with the necessary package/class wrapper so the code compiles).
- `add_repository_impl.py` **reads repository/service files from disk**. The resolved root (`--project-root` or cwd) must be the target app project so those files are visible.
- All scripts use **config.json** for paths; instruction-based scripts do not read your local files except `add_repository_impl.py`, which reads the repository/service files.

Example:

```
Target Path:
  feature/userprofile/data/datasource/remote/UserProfileService.kt

Code:
  // Add these imports to the import section:
  import io.ktor.client.call.body
  import io.ktor.client.request.get

  // Method code to add
  suspend fun getUserProfile(userId: String): UserProfileResponse {
      return client.get("/api/users/{userId}/profile").body()
  }
```

## Configuration

### config.json — single source for path behaviour

All path and package behaviour is driven **only** from a `config.json` file. Scripts do not hardcode or override path logic. A sample config ships at `.pinq-doq/scripts/config.json` — copy it and edit it for your project, then point the scripts at your copy with `--config <path-to-your-config.json>`.

- **Config path:** pass `--config <path-to-your-config.json>` to point a script at your project's config.
- **Keys:**
  - `base_path` — root under which feature/shared code lives (e.g. `composeApp/src/commonMain/kotlin`).
  - `base_package` — optional package prefix (often `""`).
  - `feature_root` — first path segment for **feature** modules (e.g. `feature`).
  - `shared_root` — **single** path segment for the shared layer (e.g. `shared`).
  - `initKoin_path` — path to initKoin.kt (optional).

When `--shared` is passed (or `feature_name` resolves to shared), paths and packages use `shared_root` (e.g. `shared/...`, `shared.*`). For other features they use `feature_root` + feature name (e.g. `feature/login/...`, `feature.login.*`). You control layout entirely by editing `config.json`; do not rely on script-side overrides.

### Custom initKoin.kt path

`register_di_modules.py` outputs instructions for registering DI modules. To use a different `initKoin.kt` path, set `initKoin_path` in your `config.json`:

```json
{
  "base_path": "composeApp/src/commonMain/kotlin",
  "base_package": "",
  "feature_root": "feature",
  "shared_root": "shared",
  "initKoin_path": "core/data/di/initKoin.kt"
}
```

- Path can be **relative to project root** or **absolute**.
- If not set, the default `core/data/di/initKoin.kt` is used in the output.
- You still **apply** the Target Path + Code to your local `initKoin.kt` (at this path).

Example output from `register_di_modules.py`:

```
Target Path:
  core/data/di/initKoin.kt

Code:
  // Add these imports to the import section:
  import feature.{feature_name}.domain.di.{Feature}DomainModule
  ...

Target Path:
  core/data/di/initKoin.kt

Code:
  // Add these modules to the modules() call:
  {feature}DomainModule,
  ...
```

Apply both blocks to the same file at the given path.

## Shared module

The decision rule (when to use shared), package structure, and naming live in `.pinq-doq/references/kotlin/shared-module.md` and the `kotlin-architecture.md` rule — they are project-wide, not API-specific.

Operationally for this skill: when shared applies, run the scripts with `--shared` (and `--entity <Concept>` where needed) so script outputs (Target Path + Code) use `shared/data/...`, `shared/domain/...` paths. Register the shared DI modules (`SharedDataModule`, `SharedDomainModule`) in `initKoin.kt` if not already present. (`SharedPresentationModule` is the `presentation-scaffold` skill's concern, not this one.)

## Naming conventions

| Layer        | Correct examples                    | Wrong / avoid                          |
|-------------|-------------------------------------|----------------------------------------|
| Data (API)  | `LoginRequest`, `LoginResponse`, `ProfilePhotoResponse` | `LoginRequestDto`, `LoginReq`, `LoginRes` |
| Domain      | `Login`, `ProfilePhoto`, `Section`  | `LoginResponse`, `LoginData`, `LoginModel` |
| Use cases   | `LoginUseCase`, `GetUserProfileUseCase` | — (always end with `UseCase`)      |
| Mappers     | `LoginMapper`, `UserProfileMapper`  | — (always end with `Mapper`)           |
| **Shared (repos/services)** | `AuthRepository`, `AuthService`, `ProductsRepositoryImpl` | `SharedRepository`, `SharedService` (use concept names; only DI modules use "Shared" prefix) |

- Data: full descriptive name + `Request` or `Response`; no DTO suffix.
- Domain: clean name, no Request/Response suffix.

## Scripts quick reference

| Script | Type | Use for |
|--------|------|---------|
| `generate_data_layer.py` | File | Data layer scaffold (folders + DI module). New feature or `--shared`. Paths from config. |
| `generate_domain_layer.py` | File | Domain layer scaffold (folders + DI module). New feature or `--shared`. |
| `generate_data_model.py` | File | Request/response data classes. Naming: `{Entity}Request`, `{Entity}Response`. |
| `generate_domain_model.py` | File | Domain entities. No Request/Response suffix. |
| `generate_mapper.py` | File | API → domain mapper in `.../model/mapper/`. |
| `generate_use_case.py` | File | Use case class in `.../domain/usecase/`. |
| `add_service_method.py` | Instr. | **Target Path + Code** for a suspend fun on the service. Endpoint must start with `/api/`. Apply to service file (create if missing). |
| `add_repository_method.py` | Instr. | **Target Path + Code** for a method on the repository interface. Apply to repository file (create if missing). |
| `add_repository_impl.py` | Instr. | **Target Path + Code** for the repository impl method. Reads repo/service files from disk; pass `--project-root`. |
| `register_mapper.py` | Instr. | **Target Path + Code** for the data DI module (`singleOf(::Mapper)`). |
| `register_use_case.py` | Instr. | **Target Path + Code** for the domain DI module (`singleOf(::UseCase)`). |
| `register_di_modules.py` | Instr. | **Target Path + Code** for initKoin.kt (or `initKoin_path` from config). Apply imports and module list. |

Run each script with `--help` to confirm its current parameters.
