# 0003 · Repo setup choices

- **Status:** accepted
- **Date:** 2026-10-28
- **Source:** setup of the repo skeleton (P0-A01), CLAUDE.md and the CI/CD workflows; rows 12 to 18 from the backend scaffold (P1-B01, 2026-10-29); rows 19 to 22 from the shared backend library (P1-B02, 2026-10-29); rows 23 to 25 from the schemas (P1-B03, 2026-10-29); rows 27 to 30 from pricing and geo (P1-B04, 2026-10-29)

## Context

The repo was set up from [docs/BUILD_PLAN.md](../BUILD_PLAN.md) on 2026-10-28. A few choices differ from the plan's text or fill a gap in it. They are recorded here so later tasks know why.

## Decisions

| # | Decision | Plan says | Reason |
| --- | --- | --- | --- |
| 1 | `CLAUDE.md` is Appendix A plus rule 9 (no invented design) and rule 10 (small Conventional Commits), numbered like section 1. The `[FIGMA]` marker reads as in section 0.3. | Copy Appendix A into `CLAUDE.md` (0.1) | Appendix A said "[FIGMA] plain Material 3 UI" and left out rules 9 and 10, which conflicts with sections 0.3 and 1. |
| 2 | The CI/CD workflows exist from Phase 0. Each one first checks that its project exists (`backend/functions/package.json`, `android/gradlew`, `admin-web/package.json`) and skips its other jobs until then. | The workflows come with P1-B11, P2-A14 and P5-W01 | CI is ready from the first push. Skipped jobs count as passing, so pull requests are not blocked before the code exists. Those tasks can remove the check. |
| 3 | The Android `check` job writes both `google-services.json` files (dev and prod). | Decode `GOOGLE_SERVICES_DEV` (P2-A14) | `./gradlew check` also builds the prod variants for unit tests, and they need the prod file. |
| 4 | CI uses `gradle/actions/setup-gradle` with `cache-provider: basic`. | "setup-gradle cache" | The default cache ("enhanced") is proprietary and only a free preview for private repos. The basic one is open source (MIT) and free (rule 8). |
| 5 | GitHub Actions are pinned to a commit, with the version in a comment. | Pin every version exactly (4.4) | A commit cannot be moved the way a tag can. Dependabot updates them. |
| 6 | Deploy jobs run firebase-tools from `backend/functions` (its pinned dev dependency), run only after the test or check job passes, and never run twice at once for the same environment. | `npx firebase-tools deploy …` | One pinned CLI version, no untested deploys, and deploys stay in order. |
| 7 | Prod Android builds take `versionName` from the tag (`android-v1.2.0` → `1.2.0`). | `versionName` from `hitchy.versionName`, default `0.1.0` (P2-A03) | Each pilot build shows its real version. |
| 8 | The admin web CI uses the backend's `.nvmrc`. | No Node version for the admin web | One Node version for the whole repo. |
| 9 | `admin-web/` is in CODEOWNERS (Islam) and Dependabot (weekly npm). Dependabot commits use Conventional Commit prefixes such as `chore(android)`. | P0-A01 lists only `/android`, `/backend/functions` and GitHub Actions | The admin web came later in the plan (Phase 5) and needs the same review and updates. The prefixes keep rule 10. |
| 10 | `.gitignore` also ignores `.kotlin/`, `admin-web/dist/`, `coverage/`, `playwright-report/` and `test-results/`. | The list in P0-A01 | These are build and test outputs of tools in section 4.4. |
| 11 | No `ios.yml` for now. | Listed in 4.1, with no spec | The iOS teammate owns it. |
| 12 | ESLint uses `eslint.config.mjs` (flat config) in the backend, and later in the admin web. | `.eslintrc.cjs` (4.2, 4.5) | ESLint 10 no longer reads `.eslintrc` files (ADR 0002). |
| 13 | The Hosting emulator runs on port 5002. | Port 5000 (P1-B01) | macOS AirPlay Receiver holds port 5000. No app or admin code uses the Hosting emulator port. |
| 14 | `firestore.rules` and `storage.rules` deny everything, and `firestore.indexes.json` is empty, until P1-B05 to P1-B07. | Not in the P1-B01 file list | `firebase.json` points to these files and the Storage emulator needs a rules file. Deny-all is the safe start. |
| 15 | Functions code is ES modules (`"type": "module"`, NodeNext); relative imports end in `.js`. One `tsconfig.json` covers `src` and `test`, so the entry point builds to `lib/src/index.js`. | No module system given | Node 24 and every tool in 4.4 support ES modules. One tsconfig keeps `build` = `tsc` and `typecheck` = `tsc --noEmit` as specified, and type-checks the tests too. |
| 16 | `backend/functions` has `.npmrc` with `save-exact=true` and a `.prettierignore` (`lib/`, `coverage/`, `package-lock.json`). | Not in the P1-B01 file list | `save-exact` keeps later installs pinned (4.4); `prettier --check .` must skip build output and the lock file. |
| 17 | npm's install scripts stay off for esbuild, re2, protobufjs, @firebase/util and google-logging-utils. | Not covered | npm 11 asks before running them. Build, lint and the emulator tests pass without them; approve one only when a task needs it. |
| 18 | Each deploy and App Distribution job first checks that its environment has the secrets it needs. If any is missing, it skips its other steps and leaves a notice that names them. | Deploy on every push to `main` and on tags | Until Phase 0 is done (P0-H05, P0-H06, P0-H09), a merge would otherwise end in a failed deploy. Deploys start by themselves once the secrets are added. |
| 19 | `schemas/docs.ts` with all of section 6 is written in P1-B02. | P1-B03 writes it | `lib/` (call context, notify, audit) needs the document types first. P1-B03 checks it against section 6 and adds `io.ts`, `inputs.ts` and the zod tests. |
| 20 | `schemas/config.ts` holds plain TypeScript types for the five config docs from P1-B02. | P5-B05 adds `schemas/config.ts` with zod | `getConfig` must be typed now. P5-B05 adds the zod schemas in the same file and derives the types from them. |
| 21 | `ADMIN_URL` is a map from project to URL, `"TODO-HUMAN"` for now `[DECISION-16]`. | `ADMIN_URL` in `lib/app.ts` | The admin Hosting sites only exist after P5-W01; admin alerts start in Phase 5. |
| 22 | Admin functions skip the profile and suspended checks in `makeCallable`. | 8.1: every function except `completeProfile` needs a profile | Dashboard accounts sign in with Google and have no `users/{uid}`. Section 8.1 now says so. |
| 23 | `schemas/io.ts` holds 8.2 plus the output type of every Phase 4–8 callable. | P1-B03 lists io.ts as "8.2" | Section 4.2 says io.ts holds all outputs, section 8 specifies them, and P1-B10's contract test needs them. |
| 24 | Callable input objects are strict: an unknown field is `INVALID_INPUT`. | Not covered | Catches app bugs and typos early; the contract changes before the apps, so apps never send fields the server doesn't know. |
| 25 | Text inputs are trimmed before their length is checked; chat text keeps its inner lines and spaces. | Not covered | A name of only spaces must fail the 1-character minimum, and stored values stay clean. |
| 26 | The AI rules live in `AGENTS.md`, with the project status, Islam's working rules and local setup notes. `CLAUDE.md` and `GEMINI.md` only import it. | Copy Appendix A into `CLAUDE.md` (0.1) | The team also uses Google Antigravity and Gemini CLI; one shared file keeps every agent on the same rules. |
| 27 | `distanceM(a, b)` accepts `GeoPointLite` (`{ lat, lng }`) and returns whole meters rounded with `Math.round`. | `geo.ts` exports `distanceM(a, b)` (haversine) (P1-B04) | Matches Firestore schema types (`checkInDistanceM: number | null`) and thresholds; sub-meter GPS decimals are noise. |
| 28 | `quote(...)` defensively throws an `Error` if the vehicle type has `null` in `PricingConfig` (e.g. scooter). | "caller checks null first" (8.5) | Prevents silent `NaN` calculations in domain logic if called directly or without prior check. |
| 29 | `trafficLevelAt(...)` throws a `RangeError` if `minuteOfDayCairo` is not an integer between 0 and 1439. | `trafficLevelAt(minuteOfDayCairo: number, c: PricingConfig): TrafficLevel` (8.5) | Fails fast on invalid inputs instead of silently defaulting or wrapping. |
| 30 | `hhmmToMinutes(time: HHmm): number` is implemented and exported in `src/lib/time.ts` with strict format and range checks ("00:00" to "23:59"). | Not covered | Reusable across time-handling domain functions and callable validators. |

## Consequences

- P1-B11, P2-A14 and P5-W01 check these workflows against the real code.
- To go back to the exact Appendix A text, change `CLAUDE.md` and this ADR together.
