---
paths: ['**/*.kt', '**/*.kts', '**/*.ts', '**/*.tsx', '**/*.js', '**/*.jsx', 'maestro/**', 'e2e/**']
---

# UI Proof Review

Extends `common.md`. A change that alters what a screen shows is proven on a device (`kotlin-testing.md` → "Proving UI changes on a device", `web-testing.md`). The person who built the screen is the worst judge of its own recording, so a second reader who has not seen the code reviews the recording before the pull request is opened. On 2026-10-25 the owner found these problems by watching the proof videos of pinqponq-chat-kmp pull request #84, and the builder had called the proof good: a flat pure-blue attach menu off the theme, two round buttons without icons, a tiny media thumbnail and a blank black video thumbnail, a bare spinner with no retry, sent media shown as a raw link in a yellow bubble, and sample data full of "Missed call" noise.

---

## Who reviews and what they get

- **MUST:** every pull request that changes what a screen shows gets an independent UI review of its proof before it is opened. The reviewer is the `pin-ui-review` agent (task runner) or another person or session that did not write the change.
- The reviewer gets only the screenshots, contact sheets and frames of the proof recordings, plus the reference screens when the product has a reference (Rindle for chat and media; deveng-core components first). It does not get the code, the task text or the builder's notes, because those explain away what the screen does not show.
- Frames come from `scripts/verification/proof_frames.py`: a frame every one or two seconds as contact sheets, and the full-size frames next to them. The reviewer opens a full-size frame for everything suspicious on a sheet.
- The review looks at the screens only. Tests, logs and code are checked by other steps.

---

## Checklist

The reviewer answers every row with `ok`, `defect` or `not-shown`, and says in one sentence what it looked at. `ok` without that sentence does not count. A reply that lists no checks is rejected, never read as "looks fine".

| Check id | What to look for |
|---|---|
| `ICONS` | Buttons, menu rows and list items with an empty icon, a missing glyph, a blank box or a placeholder shape. |
| `COLOURS` | Colours that are not theme tokens: pure `#0000FF` blue, hard-coded yellow or grey, a surface that matches neither the light nor the dark palette. |
| `TOUCH_TARGETS` | Tappable controls that look smaller than 48dp (a small icon with no padding, chips and text links packed close together). |
| `CONTRAST` | Text or icons that are hard to read on their background (grey on grey, white on yellow). 4.5:1 for body text, 3:1 for large text and icons. |
| `RAW_DATA` | A raw URL, JSON, id, file name, enum name or stack trace shown to the user instead of a designed element. |
| `PLACEHOLDER_DATA` | Test or placeholder data in the proof: "Lorem ipsum", "Test 1", repeated "Missed call" rows, identical avatars, `null` text. |
| `STATES` | A loading, error, empty or retry state that is missing, bare or unreachable: a spinner with no label or cancel, an error with no retry. A state that cannot be reached in the proof is listed as `not-shown`. |
| `PREVIEWS` | Thumbnails and previews that are blank, black, tiny or cropped so the content cannot be recognised. |
| `TEXT_FIT` | Text that is cut off, overlaps another element or runs out of its container. |
| `SPACING` | Spacing, corner radius and elevation that differ from the neighbouring screens or from the rest of the app. |
| `CONSISTENCY` | The screen looks different from the reference product or the deveng-core components it should reuse (menu, dialog, bubble, button shape and weight). |
| `THEMES` | Light and dark mode. When the proof shows one mode only, the check is `not-shown`, and a screen that sets its own colours without tokens is a `COLOURS` defect. |
| `TEXT_SIZE` | Small and large font. When the proof shows one size only, the check is `not-shown`. Mechanical large-font checks are in the section below. |

### Severity

- **major:** a user would notice it at first sight, or it breaks a state the feature promises (blank thumbnail, icon-less button, raw link, missing retry, off-theme colour block).
- **minor:** polish that does not block use (a few dp of spacing, a slightly different corner radius).

Any major defect means no pull request: fix, record again and review again. After three rounds the pull request is opened anyway, with the open defects at the top and the decision marked for the owner. The report goes into the pull request under "UI inceleme" (`ui_review_report.py section`).

---

## Mechanical checks

Where the stack allows it, a test catches the defect before anyone watches a video. These do not replace the review; they keep a fixed defect from coming back. Code examples: `.pinq-doq/references/kotlin/ui-proof-checks.md`.

- **Touch targets:** a Compose UI behavior test asserts that every clickable node of the screen is at least 48dp wide and high.
- **Contrast:** a unit test computes the contrast ratio of the theme token pairs the screen uses and asserts the 4.5:1 and 3:1 limits.
- **Theme tokens:** the colour of a new component comes from `MaterialTheme.colorScheme` or the core theme, never from a colour literal; `pinq_code-review` reports a literal.
- **Golden screenshots** already exist (`kotlin-testing.md`) and stay: they catch changes to a screen, the review catches a screen that was wrong from the start.
