---
paths: ['**/*.kt', '**/*.kts']
---

# Deveng-Core Library Guide (Vibecoding)

## Purpose

This guide ensures that when building or vibecoding an app that depends on **deveng-core-kmp**, the agent uses the library’s existing APIs instead of reimplementing or duplicating functionality. The agent should know what the core provides and choose the right API for each situation.

## Non-Goals

- Does not document parameters or teach “how to call” each API in detail.
- Does not define a strict I/O contract for the agent’s reply; the output is correct app code that uses core.
- Does not replace reading the codebase when in doubt; it directs *what to prefer* and *when*.

## Scope

### In-scope

- Before implementing a feature, check the **reference** for a matching situation and use the listed core API.
- When the user asks for dial, clipboard, maps, URL open, share, location, or platform info → use **MultiPlatformUtils**.
- When the app needs to **download remote media by URL** and **share as files** or **save to the device gallery** (photos/videos from URLs) → use **RemoteMediaExportManager** (`core.util.media`) and **`RemoteMediaFile`** for bulk lists—not `MultiPlatformUtils.shareText` (that API is for plain text).
- When the app needs to save captured photo bytes or add location EXIF → use **PhotoSaveUtils**.
- When the app needs **simple markdown lines** in Compose (headings, lists, bold/italic) with a **caller-controlled text color** → use **MarkdownContentParser** (`core.util.markdown`).
- When the app needs a **fullscreen image viewer** with zoom and swipe-to-dismiss → use **MediaViewer** (`core.presentation.component.mediaviewer`).
- When the app needs camera capture, preview, or recording → use **CameraController** / **CameraKScreen** / **rememberCameraKState** and related compose/domain APIs.
- When the app needs to stack or persist camera photos in temp storage (e.g. before upload, swipe-to-delete flow) → use core **CameraTempPhotoRepository** and **TempPhotoItem**; register **CameraTempDirProvider** in DI (or use default implementations from core).
- When the app needs permission request/check or app settings → use **PermissionsController** (and **rememberPermissionsControllerFactory** / **BindEffect**) or camera-domain **Permissions** where applicable.
- When the app needs paginated lists (load-more, pull-to-retry, empty/error UI) → use **PaginatedFlowLoader** + **PaginatedListView** and core pagination models.
- When the app needs shared UI (buttons, headers, dialogs, text fields, date pickers, lists, navigation, etc.) → use the core **presentation** composables and theme listed in the reference.
- When the app needs a **swipeable card stack** (e.g. approve/reject, gallery stack) → use **SwipeCards** + **rememberSwipeCardsState**; configure overlay buttons and **swipe-direction highlight colors** via **ComponentTheme** → **SwipeCardsTheme** (not ad-hoc colors on the composable).
- When the app needs date/time formatting or selection rules → use core **datetime** and **CustomSelectableDates**.
- When the app needs string/email validation, modifier helpers, or logging → use core **util** extensions and **CustomLogger**.

### Out-of-scope

- Inventing new core APIs or changing the library’s public contracts.
- Explaining every parameter of every function in the guide text.
- Using this guide for projects that do not depend on deveng-core-kmp (then the guide does not apply).

### Stop conditions

- **Prefer core:** If a feature clearly maps to a reference situation, use the core API; do not reimplement.
- **Assume:** If the reference lists an area (e.g. “dial / clipboard / share”) and the app needs that behavior, assume core’s API is intended unless the user explicitly asks for a different implementation.
- **Refuse:** Do not generate code that duplicates core behavior (e.g. custom “save photo to file” or “open URL” logic when PhotoSaveUtils / MultiPlatformUtils exist).

## Procedure

1. **Validate context:** Confirm the project depends on deveng-core-kmp (e.g. `deveng-core` in dependencies).
2. **Match situation:** For the requested feature, check the reference: “What exists and when to use it.”
3. **Plan:** Prefer the core module/API listed for that situation; only add app-specific wiring or UI that is not in core.
4. **Execute:** Write code that calls core APIs (MultiPlatformUtils, PhotoSaveUtils, camera, permissions, pagination, presentation components, SwipeCards where applicable, utils) instead of reimplementing their behavior.
5. **Verify:** Ensure no duplicate logic exists for dial, clipboard, maps, URL open, share text, remote URL media export (share/save gallery), location, platform config, photo save/EXIF, camera lifecycle, permissions, or pagination.

## Rules

### MUST

- Use **MultiPlatformUtils** for: dial, copy to clipboard, open maps with location, open URL, share text, get current location, get platform config. Do not implement these with platform-specific or custom code in the app.
- Use **RemoteMediaExportManager** for: download media from **HTTPS URLs** and invoke the system share sheet for **files** or persist to the **gallery** (bulk: `List<RemoteMediaFile>`). Do not reimplement URL fetch + FileProvider/MediaStore/Photos for that flow when this API fits. Do not confuse with **MultiPlatformUtils.shareText** (text-only).
- Use **PhotoSaveUtils** for saving photo bytes to a path and for adding location EXIF to image bytes (e.g. after camera capture). Do not reimplement file write or EXIF handling.
- Use core **camera** APIs (CameraController, builder, rememberCameraKState, CameraKScreen, DefaultCameraPreview, etc.) for capture, preview, and recording. Do not introduce a separate camera stack unless the user explicitly requests it.
- Use core **CameraTempPhotoRepository** (and **TempPhotoItem**, **CameraTempDirProvider** / **CameraTempFileOps**) for persisting captured photos in temp storage; do not reimplement temp photo storage in the app.
- Use **PermissionsController** (and **rememberPermissionsControllerFactory** / **BindEffect**) or camera **Permissions** for permission checks and requests; do not reimplement permission flows.
- Use **PaginatedFlowLoader** and **PaginatedListView** (and **PageResult** / **PaginatedListState**) for paginated lists with load-more and retry; do not reimplement pagination state or list UI that core already provides.
- Use core **presentation** components and **theme** (e.g. CustomButton, CustomHeader, CustomDialog, CustomTextField, CustomDatePicker, PaginatedListView, AppTheme, typography) when the UI need matches what they provide.
- For **SwipeCards** overlay actions and **left/right swipe button tint while dragging**, use **SwipeCardsTheme** inside **ComponentTheme** (`negativeSwipeHighlightColor`, `positiveSwipeHighlightColor`, `swipeHighlightIconTintBlend`, plus base `buttonBackgroundColor` / `buttonIconTint`). Do not fork that styling in the app unless the user explicitly wants a one-off.

### SHOULD

- When adding a new screen or flow, quickly scan the reference for “situation → use this” and align with it.
- Prefer core **util** (String extensions, Modifier extensions, datetime formatting, CustomLogger) over custom helpers for the same purpose.
- When **editing deveng-core** public APIs, match existing **KDoc** style: block comment above `@Composable` with a short summary and `@param` lines for each parameter (see `SearchField`, `LabeledSwitch`, `SwipeCards`); theme `data class` types use a **one-line class-level** description (see `SwipeCardsTheme`, `IconButtonTheme`).

### MUST NOT

- Implement “dial number,” “copy to clipboard,” “open map,” “open URL,” or “share text” without using MultiPlatformUtils.
- Implement “download URL → share file or save to gallery” without using **RemoteMediaExportManager** when the task is exactly that (plain text share remains **MultiPlatformUtils.shareText**).
- Implement “save image bytes to file” or “add GPS EXIF to image” without using PhotoSaveUtils.
- Implement camera capture/preview/recording with a different stack when core camera APIs are available.
- Implement temp photo stacking/storage (e.g. for pre-upload or swipe flows) without using core CameraTempPhotoRepository and related types.
- Implement permission request/check/settings navigation without using PermissionsController or core Permissions.
- Implement infinite-scroll paginated lists with custom state and list UI when PaginatedFlowLoader + PaginatedListView fit.
- Duplicate core presentation components (e.g. custom “theme” or “generic button” that mirrors AppTheme/CustomButton) without reason.
- Build a new dialog on Compose `androidx.compose.ui.window.Dialog` / `AlertDialog`; wrap it in core **CustomDialog** (or **CustomAlertDialog** for title + buttons). An existing dialog in the app that uses the raw Compose `Dialog` is legacy, not a pattern to copy.

## Reference

See **`references/kotlin/deveng-core-reference.md`** (in the pinq-doq mount, e.g. `.pinq-doq/references/kotlin/deveng-core-reference.md`) for: what exists in the library and which API to use in which situation. Use it to decide “use this from core” before writing app code.

## Architecture note (core library)

Core uses a **domain + data** split for camera temp storage: repository interface in **core.domain.camera.temp** (`CameraTempPhotoRepository`, `TempPhotoItem`), implementation and file/dir abstractions in **core.data.camera.temp** (`CameraTempPhotoRepositoryImpl`, `CameraTempDirProvider`, `CameraTempFileOps`). Apps depend on the interface and register platform-specific `CameraTempDirProvider` in DI.

**Sample app:** The `sample:composeApp` **ThemingDemo** screen includes a **SwipeCards** example at the top of the main lazy column for manual testing.

## Changing the core library (definition of done)

A change to the public surface of deveng-core-kmp (new component, new parameter, new API, new server-driven capability) is complete only when it also contains:

1. **A sample:** a working example in `sample/composeApp` showing each new public piece in its main states (new demo screen reachable from the main list, or a section in an existing demo). The sample must build on Android and desktop.
2. **Documentation:** the `README.MD` section added or updated, with a link to deeper docs when they exist.
3. **The PR says where:** a "Sample" line in the PR body naming the demo screen.

Internal refactors and bug fixes without public surface may skip the sample; say so in the PR. Do not defer the sample to a later task.

## Moving UI into a shared library

A product's look stays in the product. This section adds the UI parts to the "Shared libraries" rule in `common.md` (Separation of Concerns), which keeps a product's conventions and names out of shared code. It applies when a UI component moves from an app into deveng-core-kmp or another shared library, and when a new shared component is written from an app's screen.

- Colors, text styles, shapes, sizes and paddings come from the library's theme (`ComponentTheme` and its sub-themes) or from parameters. Each of these style parameters has a neutral default: a theme value, not the product's value.
- Text comes from parameters. The component holds no fixed text, not even as a default.
- No product name, product logo or product text stays inside the component. These name the product, so a shared component that contains one is wrong for every other product.
- No product layout constant or product color stays inside the component (a private `val HeroHeight = 312.dp`, a fixed `Color(0xFFFFC964)`). Move each one to the theme or to a parameter.
- A look that only one product wants is built by that product: through its theme values, or through a slot parameter that takes the product's own composable.

Example: an app's paywall header has a fixed yellow gradient, a 312 dp height and the text "<App> Premium". In the shared component the colors come from the theme, the height is a parameter whose default comes from the theme, the title is a required `title: String` parameter, and the app passes its own artwork through a `heroContent: @Composable () -> Unit` slot.

Before opening the pull request, search the moved files for private layout constants, hardcoded colors (`Color(0x`, `Color.Red`) and fixed text, and list in the pull request what you found and where each value went (theme, parameter or slot).

## Versions of our libraries that depend on core

pinqponq-chat-kmp, deveng-networking-kmp and other libraries of ours are published already compiled against one core-kmp (or networking-kmp) version. A theme or config `data class` that gains a parameter changes its default-argument constructor, so a library compiled against the older version crashes in the app with `NoSuchMethodError` (Android) or SIGABRT (iOS) the first time that code runs. The build stays green.

- When you add or bump one of these libraries, or bump core-kmp or networking-kmp in an app that uses them, check the core-kmp and networking-kmp versions in the library's published `.pom`. They must be the versions the app uses. If they differ, rebuild the library against the app's versions and release it first (one version line and a patch bump in the library's `gradle/libs.versions.toml`). Do not downgrade the app to the older core.
- Every page or component the app embeds from such a library needs a UI test that draws it, so a mismatch fails a test instead of the app.
