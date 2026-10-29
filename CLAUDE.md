# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Hitchy: carpooling for Greater Cairo commuters. Riders are free; hosts buy trip packages.
Full spec: docs/BUILD_PLAN.md. Read its sections 1–10 before any task.
Decisions: docs/adr/ (0002 = pinned versions and open points, 0003 = where the repo differs from the plan, 0004 = design review).
Plan v1.1 (BUILD_PLAN 2.1) changed the product after the design review: owners accept each booking (a request holds its seats), one booking can hold several seats, chat and calls after acceptance, payment confirmation, an inbox. The app's text says "Owner"; code and data say `host`.

## Status
The backend scaffold exists (P1-B01): backend/functions builds, lints and tests on the emulators. The Android and admin web projects arrive in P2-A01 and P5-W01; until then their commands do not run, and their CI workflows skip their jobs.

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
- Callables that create things take a `requestId` and are idempotent (lib/idempotency.ts). Notifications go out after the transaction commits, never inside it; booking notifications come only from the onBookingChanged trigger.
- Business numbers live in Firestore config/* (getConfig, 60 s cache) and change only through the updateConfig callable. Remote Config holds app-side flags only; the server checks the matching config/limits flags.
- Code never calls the system clock directly: lib/clock.ts in the backend, an injected Clock on Android. dateKey ("YYYY-MM-DD") and "HH:mm" values are Cairo time.

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
