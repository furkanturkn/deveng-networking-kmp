# Release Notes — Reference

Classification and writing rules for [SKILL.md](SKILL.md). Read this file before classifying any commit.

## Change Filter

**User-visible — include:**
- `feat` that adds or changes something a user can see, tap, or notice.
- `fix` for behaviour a user experienced as broken (crash, wrong content, stuck state, missing feedback).
- Copy, layout, animation and empty-state changes that alter what the screen looks like.
- Permission, onboarding, sharing and notification behaviour changes.

**Internal — exclude:**
- Analytics, telemetry, conversion events, attribution SDK wiring.
- Dependency bumps, library upgrades, build config, signing, CI.
- Compile errors, warnings, lint, formatting, refactors with no behaviour change.
- Standards/docs chores (for example pinq-doq updates), log removal, diagnostics.
- Version bump commits themselves.

**Judgment pass.** After the type filter, ask of each surviving commit: *would a user notice this without being told?* If the honest answer is no, move it to the excluded list. A `feat` that only sends an event, and a `fix` that only repairs a build, are both internal.

**Platform scoping.** A commit scoped `ios`/`android`, or whose diffstat touches only that platform's source set, is platform-only. Platform-only items go to that platform's store notes only.

The filter produces the first proposal only; the user's edits at the review gate override it.

## Voice
- Second person, present tense, plain language. "Your video collages now play in a full-screen feed", not "Implemented reels-style playback for video collages".
- Lead with the benefit, not the mechanism.
- One headline change first; everything else in a single following sentence or short paragraph.
- No marketing filler ("we're excited to announce"), no version numbers, no "bug fixes and performance improvements" unless that is genuinely all there is.
- Match the product's existing store voice when earlier release notes are available in the conversation.
