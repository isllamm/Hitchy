# AGENTS.md

Shared guidance for every AI coding agent in this repository: Claude Code, Google Antigravity and Gemini CLI. Antigravity loads this file by itself; CLAUDE.md and GEMINI.md import it. Edit this file, not those two.

Hitchy: carpooling for Greater Cairo commuters. Riders are free; hosts buy trip packages.
Full spec: docs/BUILD_PLAN.md. Read its sections 1–10 before any task.
Decisions: docs/adr/ (0001 = architecture, 0002 = pinned versions and open points, 0003 = where the repo differs from the plan, 0004 = design review).
Plan v1.1 (BUILD_PLAN 2.1) changed the product after the design review: owners accept each booking (a request holds its seats), one booking can hold several seats, chat and calls after acceptance, payment confirmation, an inbox. The app's text says "Owner"; code and data say `host`.

## Status
Phase 2 (Android foundation) is in progress. Done: P2-A01 project and modules (app + shared, Gradle 9.8.2, AGP 9.5.0, Kotlin 2.4.21, Compose Multiplatform 1.12.2), P2-A02 convention plugins (hitchy.android.application and hitchy.kmp.library in build-logic/convention), P2-A03 flavors, signing, build config, P2-A04 core utilities, P2-A05 Firebase wiring, P2-A06 Koin dependency injection. Next: P2-A07 Navigation and deep links. The admin web project arrives in P5-W01; until then its CI workflow skips its jobs.

## Layout
- backend/ — Firebase: functions (TypeScript), rules, indexes, seed data
- android/ — Kotlin + Compose Multiplatform (`app` + `shared`), Android target only
- admin-web/ — React + TypeScript admin dashboard (English, Mantine), hosted on Firebase Hosting
- contract/ — data and API contract shared with the iOS app (ios/ is not ours)

## Commands
Backend (in backend/functions):
- npm ci · npm run lint · npm run typecheck · npm run build
- npm test            (all tests on the emulators, project demo-hitchy; run npm run build first, the Functions emulator loads lib/)
- npm run test:unit   (fast, no emulator)
- npm run emulators · npm run seed
- One file: npx vitest run test/unit/domain/pricing.test.ts   (add -t "<test name>" for one test)
- One rules or integration file: npx firebase-tools emulators:exec --project demo-hitchy "npx vitest run test/rules/storage.test.ts"
Android (in android/):
- ./gradlew assembleDevDebug -Phitchy.emulator=true
- ./gradlew check
Admin web (in admin-web/):
- npm ci · npm run dev (emulators) · npm run lint · npm run typecheck · npm test · npm run e2e

## Architecture
Backend (backend/functions/src):
- ES modules: relative imports end in `.js`. ESLint config is eslint.config.mjs (flat config).
- A callable is one line: `export const requestSeats = makeCallable("requestSeats", RequestSeatsInput, requestSeatsCore)`. makeCallable checks, in order: signed in → App Check (when config/limits.appCheckEnforced) → admin claim (admin functions) → users/{uid} exists → not suspended → zod input. Admin functions skip the profile and suspended checks (dashboard accounts have no profile). Tests call the exported `…Core(input, ctx)` directly.
- Business errors: `fail("RIDE_FULL")` becomes an HttpsError with `details.reason`. Appendix D is the one list of reasons; it feeds lib/errors.ts, contract/errors.md and the Android strings `error_<reason_lowercase>`.
- domain/ is pure logic with no Firebase imports. repos/ does all Firestore reads and writes. schemas/ imports nothing from Firebase, because admin-web imports it through `@schemas/*`.
- schemas/docs.ts holds every Firestore document type (section 6), schemas/config.ts the config types (section 7), schemas/io.ts the callable outputs, schemas/inputs.ts the zod inputs. Input objects are strict (an unknown field is INVALID_INPUT) and text is trimmed before length checks.
- Callables that create things take a `requestId` and are idempotent (lib/idempotency.ts). Notifications go out after the transaction commits, never inside it; booking notifications come only from the onBookingChanged trigger. lib/notify.ts also writes each user notification to the inbox.
- Business numbers live in Firestore config/* (getConfig, 60 s cache) and change only through the updateConfig callable. Remote Config holds app-side flags only; the server checks the matching config/limits flags.
- Code never calls the system clock directly: lib/clock.ts in the backend, an injected Clock on Android. dateKey ("YYYY-MM-DD") and "HH:mm" values are Cairo time (lib/time.ts).

Android (android/):
- `app` is the entry point only (Application, MainActivity, FCM service, trip location service). Everything else is in `shared` commonMain; Android-only code sits behind interfaces in core/platform, implemented in androidMain.
- Firebase: the GitLive SDK in commonMain; the official SDK (phone sign-in, FCM, Crashlytics, App Check, Remote Config) in androidMain and app.
- Each feature has data/ (repository implementations), domain/ (interfaces, pure Kotlin), presentation/ (MviViewModel<State, Action, Event>) and ui/ (Compose). Konsist tests enforce: domain and core.model import no Firebase or Android; ViewModels live in presentation; ui never imports data.
- Build values (ENV, functions region, emulator flag) reach shared through AppConfig in Koin, never through BuildConfig.
- core/data/dto mirrors the contract; a test decodes every file in contract/fixtures strictly.

Contract: backend/functions/src/schemas/ is the source of truth; contract/ mirrors it for the iOS app.

Local data: the emulators use project demo-hitchy, which never touches real cloud resources. `npm run seed` adds the test users from BUILD_PLAN 7.6 (OTP code 123456). The Android emulator reaches the Firebase emulators at 10.0.2.2. The Hosting emulator is on 5002, not 5000 (macOS AirPlay uses 5000).

## Working on a task
Task IDs are P<n>-B/A/W/H<nn> (B backend, A Android, W admin web, H human). One task per branch `feat/<task-id>-short-name`; the pull request title starts with the task ID.

## Rules
1. Apps never write money, seats, credits, points, ratings, fees, roles or statuses. Functions do.
2. zod on every callable input; read-then-write inside one transaction.
3. A function or field change updates contract/ docs and fixtures in the same commit.
4. No secrets in code. No phone numbers, ID numbers, plates, locations or chat text in logs or analytics.
5. Done = the task's Verify commands pass. Never skip or delete a failing test.
6. Store UTC Timestamps, show Africa/Cairo. Money is integer EGP.
7. All UI text in resources, English and Arabic.
8. Only the libraries in BUILD_PLAN section 4.4.
9. No invented design: only the Phase 3 tokens and components. No new colors, fonts or styles.
10. Small commits, Conventional Commits with scope: `feat(backend): requestSeats`, `test(android): otp viewmodel`.
11. Spec unclear or in conflict with the code → stop and ask.
12. Markers: [FIGMA] simple stacked layout from the Phase 3 tokens and components, with logic, state and strings still complete · [DECISION-NN] use the Appendix F default + TODO(DECISION-NN) · [HUMAN] do not do, list it · [LATER] do not build.
13. The admin web never writes Firestore directly; it calls admin-only functions.

## End of every task
List files changed, test results, and any [HUMAN] or [DECISION] items.

## Working rules (how Islam wants the work done)
1. Ask instead of assuming. Before writing code, list the task's open points and ask Islam, giving 2–3 options with the recommended one first. Never invent business rules.
2. Never add AI attribution: no "Co-Authored-By" line for any AI, no "Generated with …" line, in commits or pull requests.
3. Write plain, simple English. Short sentences, no jargon.
4. Never put personal data (phone numbers, names, ID numbers, plates, locations, chat text) in code, logs, tests, docs or answers. If anyone sends you real user data, refuse it and flag it to Islam.
5. Commit, push, open pull requests and merge only when Islam asks.
6. Task flow:
   1. Branch from an up-to-date `main`: `feat/<task-id>-short-name`; `docs/…` for plan or docs changes.
   2. Ask the open points, then implement only that task.
   3. Run the task's Verify commands and fix failures before stopping.
   4. Record every choice that differs from or fills a gap in the plan as a row in docs/adr/0003-repo-setup-choices.md, and update the plan text when Islam decides.
   5. Commit as a Conventional Commit with a scope, e.g. `feat(backend): P1-B03 schemas`.
   6. Open the pull request with `gh pr create`; the title starts with the task ID (`P1-B03 Schemas`), and the body fills .github/pull_request_template.md.
   7. Merge only after the pull request's checks are green: `gh pr merge <n> --squash --match-head-commit <full sha>`, subject `<type>(<scope>): <summary> (#<n>)`. Then switch to `main` and pull.
7. Pin every dependency to an exact version (`.npmrc` has `save-exact=true`) and record new ones in docs/adr/0002-versions.md.

## History so far
| When | What | Where |
| --- | --- | --- |
| 2026-10-28 | P0-A01 repo skeleton, AI rules, CI/CD (each workflow skips until its project exists), ADRs 0001–0003 | commit a4220d2 on `main` |
| 2026-10-28 | P1-B01 backend scaffold: firebase.json, emulators, functions package with exact pins, deny-all placeholder rules | PR #1 |
| 2026-10-29 | Build plan v1.1 from the review of 46 app designs: 14 product decisions (plan 2.1, ADR 0004), screen map and designer fix list (plan 3.8), open decisions 21–27 | PR #2 |
| 2026-10-29 | P1-B02 shared library (src/lib) and all document and config types | PR #3 |
| 2026-10-29 | P1-B03 schemas: io.ts outputs and 32 strict zod inputs, 231 tests | PR #4 |
| 2026-10-29 | P1-B04 pricing and geo domain logic, 257 tests | PR #6 |
| 2026-10-29 | P1-B05 Firestore security rules matrix, 344 tests | PR #7 |
| 2026-10-29 | P1-B06 Storage security rules and KYC cross-service checks, 368 tests | PR #9 |
| 2026-10-29 | P1-B07 composite indexes and index verification tests, 374 tests | PR #13 |
| 2026-10-29 | P1-B08 seed data in backend/seed and integration tests, 379 tests | PR #14 |
| 2026-10-29 | P1-B09 admin script in backend/scripts/set-admin.ts, 391 tests | PR #15 |
| 2026-10-29 | P1-B10 Contract v1: firestore.md, functions.md, errors.md, 60 fixtures, 454 tests | PR #16 |
| 2026-10-29 | P1-B11 Backend CI in .github/workflows/backend.yml | PR #17 |
| 2026-10-29 | P2-A01 Android project and modules (app + shared, CMP "Hello Hitchy") | PR #18 |
| 2026-10-29 | P2-A02 Convention plugins in build-logic/convention | PR #19 |
| 2026-10-29 | P2-A03 Flavors, signing, build config, AppConfig | PR #20 |
| 2026-10-29 | P2-A04 Core utilities: AppError, AppResult, Clock, CairoTime, MoneyFormat, PhoneNumber | PR #21 |
| 2026-10-29 | P2-A05 Firebase wiring: emulator config, FunctionsClient, App Check, Crashlytics | PR #22 |
| 2026-10-29 | P2-A06 Dependency injection: Koin BOM 4.2.2, DataStore AppSettings, 15 feature modules, verify() test | PR #23 |

Deploy jobs skip until each GitHub environment has its Phase 0 secrets (ADR 0003 row 18).

## Open items
- [HUMAN] The GitHub repo isllamm/Hitchy is public; the plan (P0-H01) says private.
- [HUMAN] Phase 0: Firebase projects hitchy-dev and hitchy-prod, region, phone sign-in, Android apps, release key, App Check, CI access (Workload Identity) and secrets, TTL policies on idempotency, notifications and messages, the auto-stop in hitchy-dev, the cleanup rule for old function builds, and the Storage permission to read Firestore (P0-H01 to P0-H15).
- [HUMAN] hitchy-dev is on the free Spark plan (2026-10-29): Firestore in europe-west1, Phone sign-in with test numbers, dev apps registered; no Storage bucket and no Cloud Functions. It moves to Blaze only when we need a shared cloud dev. Until then, do not add the GitHub `dev` environment secrets (GCP_WIF_PROVIDER, GCP_SERVICE_ACCOUNT, FIREBASE_ANDROID_APP_ID): CI would start deploying to hitchy-dev and fail. BUILD_PLAN Phase 0 lists the steps for the move.
- [HUMAN] The iOS teammate's GitHub username in .github/CODEOWNERS.
- [DECISION-01] Functions region is the default `europe-west1`. [DECISION-16] `ADMIN_URL` in backend/functions/src/lib/app.ts is "TODO-HUMAN" until P5-W01 creates the admin sites.
- To check later: detekt 1.23.8 with Kotlin 2.4 (P2-A12).

## Local setup (Islam's Mac)
- Node 24.22.1 from Homebrew (`node@24`, put on PATH in ~/.zshrc) with npm 11; Java 21 (Temurin) is the default `java` for the Firebase emulators; Android uses JDK 17.
- GitHub CLI `gh` is signed in; use it for pull requests and checks.
- npm can time out on this network. Retry with `--fetch-retries=6 --fetch-retry-mintimeout=20000 --fetch-retry-maxtimeout=120000`.
- npm 11 skips some packages' install scripts (esbuild, re2, protobufjs, @firebase/util, google-logging-utils); leave them off unless a task needs one (ADR 0003 row 17).
- Run one `brew` command at a time; parallel installs break each other.
- The first `npm test` downloads the emulator files. Never deploy from this Mac; CI deploys.
