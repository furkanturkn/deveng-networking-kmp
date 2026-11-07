---
paths: ['**/*.ts', '**/*.tsx', '**/*.js', '**/*.jsx', '**/playwright.config.*', 'e2e/**']
---

# Web — End-to-End Proof with Playwright

Extends `common.md`. How a change in a web project (React, Vite, TypeScript and similar) is proven in a browser. Playwright plays the role Maestro plays for Android and iOS: a scripted flow runs the user's steps in a real browser, takes screenshots and fails when the screen does not show what it should. Setup recipe, config and script examples: `.pinq-doq/references/web/e2e-playwright.md`. The same file sets up mutation testing with StrykerJS (the last section of this rule).

---

## When web proof is required

- **MUST:** every change that alters what a web page shows or what a user action on it does is run as a Playwright flow before the pull request is opened. Unit and component tests are still required; the flow does not replace them.
- Chromium is the default browser. Add WebKit or Firefox only when the change uses a browser-specific API or there is a concrete reason to suspect another browser behaves differently, and say which case applies in the pull request.
- A change with no visible effect (build config, types only, a refactor with unchanged output) needs no flow; the pull request says so.

---

## Folder layout

| Path | Holds |
|---|---|
| `playwright.config.ts` | Project config at the repo root. Output goes to `build/e2e/web/`. |
| `e2e/flows/<name>.spec.ts` | One flow per user scenario (the counterpart of a Maestro flow). |
| `e2e/subflows/<name>.ts` | Shared steps as plain async functions that take a `Page` (open a menu, pick a date, wait for a table). |
| `e2e/auth.setup.ts` | The only place that signs in. It saves the signed-in browser state to `e2e/.auth/state.json`. |
| `e2e/scripts/run-flow.sh` | Runs one flow: `e2e/scripts/run-flow.sh [--proof] <flow>`. |
| `e2e/scripts/login.sh` | Lets a person sign in once by hand and saves the state, for a test environment without a fixed one-time code. |
| `build/e2e/web/` | Screenshots, videos, traces, the HTML report and `junit.xml`. Never committed. |

`e2e/.auth/`, `build/` and `.env` are in `.gitignore`.

---

## Writing flows

- **Reuse before writing.** Read the flows and subflows near the changed feature first. Extend an existing flow when it covers the scenario, add a subflow when a shared step is missing, and write a new flow only for a scenario no flow covers. The pull request says which flows were reused, extended or added.
- **Select what the user sees.** Use `getByRole` with the accessible name, `getByLabel`, `getByPlaceholder` or `getByText`. Use `getByTestId` only when one of those would need more than three conditions to find a single element. Never select by CSS class, DOM structure or index.
- **Wait for state, not time.** Use web-first assertions (`await expect(locator).toBeVisible()`, `toHaveText`, `toHaveURL`). Never use `page.waitForTimeout` to wait for the app.
- **Start signed in.** Every flow starts from the stored state that `auth.setup.ts` saved (`storageState` in the config). A flow never fills the sign-in form itself. A flow that tests signing in or signing out uses a fresh context (`test.use({ storageState: { cookies: [], origins: [] } })`) and stays the only flow that does so.
- **Take a screenshot at each state the pull request shows** (`await page.screenshot({ path: … })` under `build/e2e/web/screenshots/<flow>/`), named after the state (`orders-filter-applied.png`). `run-flow.sh` empties that folder before the run, so old screenshots never pass as new evidence.
- **Same actions, same order as the issue.** A flow that proves a fix first fails on the old code, or shows the problem's footprint, and then passes on the fixed code.

---

## Proof runs

- **MUST:** the run whose media goes into the pull request is a proof run: `e2e/scripts/run-flow.sh --proof <flow>`, or `E2E_PROOF=1` in the environment. A proof run records a video of every flow. The screenshots a flow takes are kept in every run.
- In every run, the trace and a failure screenshot are kept only for a failing test (`retain-on-failure`). A normal run also keeps the video only for a failing test, so everyday runs stay fast and small.
- The sign-in step (`auth.setup.ts`) never records a video, a trace or a screenshot, even in a proof run: its screen shows the test account.
- `run-flow.sh` copies the proof videos to `build/e2e/web/videos/<flow>/` after the run and empties that folder before it, the same way it handles `screenshots/<flow>/`.

---

## Test credentials

- A web project defines these variables in a local `.env` file at the repo root. `.env` is never committed; the repo commits `.env.example` with the names and empty values.

| Variable | Meaning |
|---|---|
| `E2E_BASE_URL` | Address the flows open, for example the local dev server (`http://localhost:5173`). |
| `E2E_USERNAME` | Test account user name or email for the test environment. |
| `E2E_PASSWORD` | Test account password. |
| `E2E_OTP` | Optional. A fixed one-time code the test environment accepts for the test account. When it is empty, a person signs in once by hand (`e2e/scripts/login.sh`) and the saved state is reused until it expires. |

- Credentials, one-time codes and the saved browser state (`e2e/.auth/state.json` holds session cookies) never go into code, flow files, commit messages, issues, pull requests, chat or logs. Flows read them only through `process.env`.
- Use test-environment accounts only. A flow never runs against production.

---

## What the pull request contains

- The video and the screenshots of the passing proof run, for each state the change shows.
- For a bug fix, the proof run on the old code that shows the problem, and its trace when it failed (`trace.zip`, opened with `npx playwright show-trace`). For any other failing run the reviewer must see, its trace too. A trace holds the test environment's session cookies, so it is attached only in private repos; in a public repo the pull request shows the failing step and its screenshot instead.
- The exact command that ran each flow and its result.

---

## Mutation testing

StrykerJS with the Vitest runner plays the role PIT plays for Kotlin and Stryker.NET plays for .NET: it changes the code in small ways (mutants) and checks that a test fails for each change.

- **MUST:** every change to TypeScript or JavaScript production code proves that its tests can fail. Run StrykerJS on the changed files only (`--mutate` with the files of the diff, never the whole project), and count only the mutants on lines changed against the default branch.
- **MUST:** the score on changed lines is at least **85%**, the same threshold as PIT. Below it, add or sharpen tests and run again. A mutant that still survives goes into the pull request with a one-line reason (an end-to-end flow covers it, unreachable defensive code, an equivalent change). A survivor on the main condition or branch the change adds is never acceptable.
- Mutants the TypeScript checker rejects (`CompileError`), that crash the test run before any test starts (`RuntimeError`) or that the config ignores (`Ignored`) are invalid code or excluded on purpose, not missed tests: they are left out of the score and reported as a count.
- Test files, type declarations (`.d.ts`), stories, `e2e/` and config files are never mutated.
- The report goes to `build/stryker/` (`mutation.json`, `mutation.html`, the incremental file), never committed.
- Hand-written mutants do not count as mutation testing.
