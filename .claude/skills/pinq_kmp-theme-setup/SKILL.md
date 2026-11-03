---
name: pinq_kmp-theme-setup
description: Project-agnostic pattern for setting up colors, typography, and the AppTheme composable in a KMP / Compose Multiplatform project that depends on deveng-core-kmp. Covers Color.kt token definitions, Typography.kt with four TextStyle helpers (RegularTextStyle, MediumTextStyle, SemiBoldTextStyle, BoldTextStyle) wired to a project FontFamily, and Theme.kt with ComponentTheme wiring for every deveng-core sub-theme (ButtonTheme, CustomTextFieldTheme, AlertDialogTheme, HeaderTheme, DatePickerTheme, etc.). Trigger when starting a new KMP project, creating Color.kt / Typography.kt / Theme.kt, wiring ComponentTheme, configuring deveng-core component defaults, deciding how to structure design tokens, or when anyone mentions setting up theming, colors, fonts, or typography in a Compose Multiplatform app that uses deveng-core-kmp.
---

# KMP Theme Setup

General pattern for colors, typography, and theming in a KMP/Compose project that depends on `deveng-core-kmp`. **Project-agnostic** â€” the *values* (specific palette, type scale, design philosophy) live in each project's own docs; this skill is about the *wiring*.

For reference on what deveng-core provides (components, utilities, camera, etc.), see the `deveng-core` rule (`kotlin-deveng-core.md`) and its API map (`references/kotlin/deveng-core-reference.md`).

---

## When to use this skill

- Starting a new KMP/Compose Multiplatform project that depends on deveng-core-kmp.
- Setting up or refactoring `core/presentation/theme/Color.kt`, `Typography.kt`, or `Theme.kt`.
- Wiring a new `ComponentTheme` sub-theme after adding a new deveng-core component.
- Deciding how to name/organize color tokens or text-style helpers.
- Diagnosing why a deveng-core component is rendering with wrong colors, fonts, or shapes (almost always a missing `ComponentTheme` sub-theme).

---

## Table of Contents

1. [File Layout](#1-file-layout)
2. [Color Tokens (`Color.kt`)](#2-color-tokens-colorkt)
3. [Typography (`Typography.kt`)](#3-typography-typographykt)
4. [AppTheme with ComponentTheme (`Theme.kt`)](#4-apptheme-with-componenttheme-themekt)
5. [Component Sub-Themes Reference](#5-component-sub-themes-reference)
6. [Usage Rules](#6-usage-rules)

---

## 1. File Layout

Every project keeps its theme under one package:

```
core/presentation/theme/
â”œâ”€â”€ Color.kt          â† all Color tokens
â”œâ”€â”€ Typography.kt     â† FontFamily + TextStyle helpers + FontWeight constants
â””â”€â”€ Theme.kt          â† AppTheme composable with ComponentTheme wiring
```

`AppTheme` is **defined locally in each project** â€” it is NOT imported from `deveng-core-kmp`. The core library provides the `ComponentTheme` data classes and `LocalComponentTheme` composition local; the project wires them. This separation is intentional: each project picks its own palette, font, and component defaults without fighting the library.

---

## 2. Color Tokens (`Color.kt`)

Define every color as a top-level `val` in `Color.kt`. Use `PascalCase` ending in `Color`. Never hard-code hex values in composables.

```kotlin
import androidx.compose.ui.graphics.Color

// Material-aligned slots
val PrimaryColor = Color(0xFFFFC964)
val OnPrimaryColor = Color(0xFF1A1A1A)
val SecondaryColor = Color(0xFFE6A000)
val OnSecondaryColor = Color.White
val SurfaceColor = Color(0xFFFFFFFF)
val OnSurfaceColor = Color(0xFF1A1A1A)
val BackgroundColor = Color(0xFF0E0E0E)
val OnBackgroundColor = Color(0xFF1A1A1A)
val ErrorColor = Color(0xFFDC2626)

// Project-specific semantic tokens (prefixed with Custom)
val CustomGrayHintColor = Color(0xFF9CA3AF)
val CustomBlackColor = Color(0xFF1A1A1A)
val CustomGrayColor = Color(0xFF6B7280)
val CustomDividerColor = Color(0xFFE5E7EB)
val CustomTextFieldBorderGrayColor = Color(0xFFE5E7EB)
val CustomLightGray = Color(0xFFF3F4F6)
// ...
```

**Rules**

- One token per semantic purpose.
- Material-aligned slots (`Primary`, `OnPrimary`, `Secondary`, `Surface`, `Background`, `Error`) come first.
- Additional project tokens prefixed with `Custom` (`CustomGrayHintColor`, `CustomDividerColor`, etc.).
- All tokens live in a single `Color.kt` file â€” no splitting by feature.

---

## 3. Typography (`Typography.kt`)

Declare font weight constants, the `FontFamily` composable, and **one `TextStyle` helper per weight**. Every piece of text in the app resolves its style through these helpers.

```kotlin
val REGULAR_FONT_WEIGHT = FontWeight(400)
val MEDIUM_FONT_WEIGHT = FontWeight(500)
val SEMI_BOLD_FONT_WEIGHT = FontWeight(600)
val BOLD_FONT_WEIGHT = FontWeight(700)

@Composable
fun UrbanistTextFont() = FontFamily(
    Font(resource = Res.font.urbanistregular, weight = REGULAR_FONT_WEIGHT),
    Font(resource = Res.font.urbanistmedium, weight = MEDIUM_FONT_WEIGHT),
    Font(resource = Res.font.urbanistsemibold, weight = SEMI_BOLD_FONT_WEIGHT),
    Font(resource = Res.font.urbanistbold, weight = BOLD_FONT_WEIGHT)
)

@Composable
fun RegularTextStyle(): TextStyle = TextStyle(
    fontFamily = UrbanistTextFont(),
    fontWeight = REGULAR_FONT_WEIGHT
)

@Composable
fun MediumTextStyle(): TextStyle = TextStyle(
    fontFamily = UrbanistTextFont(),
    fontWeight = MEDIUM_FONT_WEIGHT
)

@Composable
fun SemiBoldTextStyle(): TextStyle = TextStyle(
    fontFamily = UrbanistTextFont(),
    fontWeight = SEMI_BOLD_FONT_WEIGHT
)

@Composable
fun BoldTextStyle(): TextStyle = TextStyle(
    fontFamily = UrbanistTextFont(),
    fontWeight = BOLD_FONT_WEIGHT
)
```

**Rules**

- One font family per project. Name the `FontFamily` composable after the font (`UrbanistTextFont`, `ManropeTextFont`, etc.).
- Font weight constants: `REGULAR_FONT_WEIGHT`, `MEDIUM_FONT_WEIGHT`, `SEMI_BOLD_FONT_WEIGHT`, `BOLD_FONT_WEIGHT`.
- `TextStyle` helpers are `@Composable` functions named `RegularTextStyle()`, `MediumTextStyle()`, `SemiBoldTextStyle()`, `BoldTextStyle()` â€” no project prefix.
- `fontSize`, `color`, `textAlign`, `textDecoration`, etc. are applied at the call site via `.copy(...)`:

  ```kotlin
  Text(
      text = state.uiState.title,
      style = SemiBoldTextStyle().copy(fontSize = 18.sp, color = OnPrimaryColor)
  )
  ```

- **Never** use `MaterialTheme.typography.*`.
- **Never** import `CoreRegularTextStyle` / `CoreMediumTextStyle` / etc. from `deveng-core-kmp` â€” those bypass the project's font family.

---

## 4. AppTheme with ComponentTheme (`Theme.kt`)

`AppTheme` is the project's theme composable. It:

1. Builds Material `colorScheme` from the color tokens (both light and dark).
2. Builds a `ComponentTheme` configuring every deveng-core component (`CustomButton`, `CustomTextField`, `CustomHeader`, dialogs, pickers, etc.).
3. Wraps content in `MaterialTheme { CompositionLocalProvider(LocalComponentTheme provides customTheme) { content() } }`.

Every core component reads its defaults from the current `LocalComponentTheme`, so all component configuration lives in one place. If a sub-theme is missing, that core component falls back to library defaults and the project's styling is silently lost â€” so every core component the project uses must have its sub-theme configured here.

```kotlin
package core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    background = BackgroundColor,
    onBackground = OnBackgroundColor,
    surface = SurfaceColor,
    onSurface = OnSurfaceColor,
    error = ErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    background = BackgroundColor,
    onBackground = OnBackgroundColor,
    surface = SurfaceColor,
    onSurface = OnSurfaceColor,
    error = ErrorColor
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val customTheme = ComponentTheme(
        typography = TypographyTheme(
            fontFamily = UrbanistTextFont()
        ),
        button = ButtonTheme(
            containerColor = colorScheme.secondary,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = colorScheme.secondary.copy(alpha = 0.4f),
            disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.4f),
            defaultTextStyle = SemiBoldTextStyle().copy(
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        ),
        alertDialog = AlertDialogTheme(
            headerColor = Color.White,
            bodyColor = Color.White,
            titleColor = CustomBlackColor,
            descriptionColor = CustomBlackColor,
            dividerColor = CustomDividerColor,
            titleTextStyle = SemiBoldTextStyle().copy(fontSize = 16.sp),
            descriptionTextStyle = MediumTextStyle().copy(fontSize = 16.sp),
            buttonTextStyle = BoldTextStyle().copy(fontSize = 16.sp)
            // ...remaining fields
        ),
        customTextField = CustomTextFieldTheme(
            titleTextStyle = MediumTextStyle().copy(fontSize = 16.sp, color = CustomBlackColor),
            textStyle = MediumTextStyle().copy(fontSize = 16.sp, color = CustomBlackColor),
            hintTextStyle = MediumTextStyle().copy(fontSize = 16.sp, color = CustomGrayHintColor),
            errorTextStyle = MediumTextStyle().copy(fontSize = 12.sp, color = ErrorColor),
            containerShape = RoundedCornerShape(12.dp),
            containerColor = Color.White
            // ...remaining fields
        ),
        // ... every other sub-theme (see reference below)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(LocalComponentTheme provides customTheme) {
                content()
            }
        }
    )
}
```

**Rules**

- Must build `colorScheme` from tokens in `Color.kt`, not hard-coded hex.
- Must provide a `ComponentTheme` via `LocalComponentTheme` â€” otherwise core components fall back to library defaults and the project's styling is lost.
- `MaterialTheme` wraps `CompositionLocalProvider`, not the other way around.
- Every core component used in the project must have its corresponding sub-theme configured.
- The root entry (e.g., `MainScreen.kt`, `App.kt`) wraps everything in `AppTheme { ... }`.
- All `@Preview` composables wrap their content in `AppTheme { ... }`.

---

## 5. Component Sub-Themes Reference

`ComponentTheme` aggregates these sub-themes. Configure each with tokens from `Color.kt` + `TextStyle` helpers from `Typography.kt`:

| Sub-theme | Applies to |
|-----------|-----------|
| `TypographyTheme` | Default font family for all core components |
| `ButtonTheme` | `CustomButton` |
| `IconButtonTheme` | `CustomIconButton` |
| `CustomTextFieldTheme` | `CustomTextField` |
| `AlertDialogTheme` | `CustomAlertDialog` |
| `DialogHeaderTheme` | `CustomDialogHeader` |
| `HeaderTheme` | `CustomHeader` |
| `SurfaceTheme` | `RoundedSurface` |
| `DatePickerTheme` | `CustomDatePicker` |
| `DateRangePickerTheme` | `CustomDateRangePicker` |
| `PickerFieldTheme` | `PickerField` |
| `LabeledSwitchTheme` | `LabeledSwitch` |
| `LabeledSlotTheme` | `LabeledSlot` |
| `SearchFieldTheme` | `SearchField` |
| `CustomDropDownMenuTheme` | `CustomDropDownMenu` |
| `OptionItemTheme` | `OptionItem` |
| `OptionItemListTheme` | `OptionItemList` + option-item dialogs |
| `ChipItemTheme` | `ChipItem` |
| `ProgressIndicatorBarsTheme` | `ProgressIndicatorBars` |
| `OtpViewTheme` | `OtpView`, `OtpDigit` |
| `ScrollbarWithScrollStateTheme` | `Modifier.scrollbarWithScrollState` |
| `ScrollbarWithLazyListStateTheme` | `Modifier.scrollbarWithLazyListState` |

Only include sub-themes for components the project actually uses. If a new core component is introduced later, add its sub-theme to `AppTheme` before using the component.

---

## 6. Usage Rules

### Do

- Wrap the app root and every `@Preview` in `AppTheme { ... }`.
- Import colors directly from tokens: `import core.presentation.theme.PrimaryColor`.
- Use TextStyle helpers for every `Text` / `TextField` / custom text rendering: `SemiBoldTextStyle().copy(fontSize = 16.sp, color = CustomBlackColor)`.
- Add a new token to `Color.kt` when a new color is needed; do not inline hex.
- Configure a sub-theme for every core component the project uses.

### Don't

- Don't use `MaterialTheme.typography.*`.
- Don't import `CoreRegularTextStyle` / `CoreMediumTextStyle` / `CoreSemiBoldTextStyle` / `CoreBoldTextStyle` from `deveng-core-kmp` â€” they do not apply the project's font.
- Don't hard-code `Color(0xFF...)` in composables.
- Don't define a second `AppTheme` or wrap content in `MaterialTheme` without going through `AppTheme`.
- Don't rely on core component defaults â€” always configure the sub-theme in `ComponentTheme`.
- Don't create per-feature `TextStyle` constants; always compose via `.copy(...)` on the four helpers.
