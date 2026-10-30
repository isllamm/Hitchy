# AGENTS.md

Shared guidance for every AI coding agent in this repository: Claude Code, OpenAI Codex, Google Antigravity and Gemini CLI. Codex and Antigravity load this file by themselves; CLAUDE.md and GEMINI.md import it. Edit this file, not those two. Keep it under 32 KiB: Codex stops reading project instructions after that by default.

Hitchy: carpooling for Greater Cairo commuters. Riders are free; hosts buy trip packages.
Full spec: docs/BUILD_PLAN.md. Read its sections 1–10 before any task.
Decisions: docs/adr/ (0001 = architecture, 0002 = pinned versions and open points, 0003 = where the repo differs from the plan, 0004 = design review).
Rider designs: docs/design/rider-flow.md (screen index and flow map of the full rider design set; the images stay off the repo).
Plan v1.1 (BUILD_PLAN 2.1) changed the product after the design review: owners accept each booking (a request holds its seats), one booking can hold several seats, chat and calls after acceptance, payment confirmation, an inbox. The app's text says "Owner"; code and data say `host`.
Plan v1.2 (BUILD_PLAN 2.2, 2026-10-30) for the demo: a wallet that pays owners, ride stops, the free wait from the owner's arrival, one Rides list. Demo designs: docs/design/demo-flow.md.

## Status
Phase 3 (Design system): P3-A01 tokens and theme, P3-A02 Cairo font, P3-A03 Icons, P3-A04 Components from Figma, P3-A05 Components not in Figma, P3-A06 Gallery. The demo build (BUILD_PLAN 3.9, for Friday 2026-10-30 evening) is in progress. **The backend lane is finished:** P3-B01 demo contract, P3-B02 offer a ride, P3-B03 search and booking, P3-B04 trip day, P3-B05 wallet payments, P3-B06 demo inbox. The backend now runs the whole demo cycle on the emulators (601 to 610 tests). Also merged: P3-A08 demo data and sign-in, P3-A09 rider demo screens, P3-A11 Maps SDK, and the two Phase 4 callables sign-in needs (P4-B01 completeProfile, P4-B02 switchRole). P3-A10 owner demo screens are merged too, so every task in the demo build is done except P3-H01, the demo run (Islam). The rider entry screens were rebuilt to the Figma DEMO frames (#58), and two owner-screen fixes landed (#60, #61). Open before the run: real names and coordinates for the demo pickup points, the Maps tiles check on a device, and one walk through the full cycle on the emulators. After the demo: P3-A07 Swap the screens. The admin web project arrives in P5-W01; until then its CI workflow skips its jobs.

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
- Maps: only the Maps SDK map without a map ID; never Directions, Routes, Places, Geocoding or Street View (paid). Route lines are straight lines between points. The key comes from `MAPS_API_KEY` in android/local.properties or CI, never committed (BUILD_PLAN 5.10).

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
| 2026-10-29 | P2-A07 Navigation and deep links: 38 routes, DeepLinks.parse, HitchyNavHost, 29 tests | PR #24 |
| 2026-10-29 | P2-A08 Screen pattern: MviViewModel, AppError.message(), SampleScreen and test | PR #25 |
| 2026-10-29 | P2-A09 Strings and language: AppLanguage, LanguageSwitcher, default Arabic, 71 error strings, ErrorCatalogTest | PR #26 |
| 2026-10-29 | P2-A10 Platform interfaces: 7 core platform interfaces, fakes, Android implementations, DI wiring | PR #27 |
| 2026-10-29 | P2-A11 Debug screen: DEV-only route, versionName in AppConfig, live pricing, anon emulator auth, BoM messaging/config | PR #29 |
| 2026-10-30 | P2-A12 Code quality: hitchy.quality convention plugin, detekt 1.23.8, Spotless 8.10.4, ktlint 1.8.0, Lint baselines, LeakCanary 2.14, 4 Konsist architecture tests | PR #30 |
| 2026-10-30 | P2-A13 Contract DTO tests: 30 callable DTOs, encodeDefaults = false, ContractDtoTest 60 fixtures | PR #33 |
| 2026-10-30 | P2-A14 Android CI: ready gate removed, workflow_dispatch, placeholder fallback for PRs | PR #34 |
| 2026-10-30 | P3-A01 Tokens and theme: Color, HitchyColors, Type, Shape, Spacing, HitchyTheme, ThemeTokensTest | PR #35 |
| 2026-10-30 | P3-A02 Cairo font: Regular, SemiBold, Bold with OFL license, English/Arabic previews | PR #36 |
| 2026-10-30 | P3-A03 Icons: 32 vector drawables, HitchyIcons, IconCatalogTest, IconGallery preview | PR #37 |
| 2026-10-30 | P3-A04 Components from Figma: 27 components in core/ui/components, previews, strings | PR #38 |
| 2026-10-30 | P3-A05 Components not in Figma: 16 components, design review badges, previews, tests | PR #39 |
| 2026-10-30 | P3-A06 Gallery: dev-only gallery screen, category filters, interactive RTL/LTR toggle, 5 sections | PR #40 |
| 2026-10-30 | P3-B01 Demo contract v1.2: RideStop, wallets, booking pickup fields, 4 new reasons, pickupZones index, seeded wallets, contract and fixtures, 479 tests | PR #42 |
| 2026-10-30 | P3-B02 Offer a ride: quotePrice and createRide with stops, first repos (users, vehicles, zones, routes, rides), 500 tests | PR #43 |
| 2026-10-30 | P4-B01 completeProfile callable, defaults, kyc doc, referral codes, locked gender, 8 integration tests | PR #44 |
| 2026-10-30 | P4-B02 switchRole callable, host status enforcement, 4 integration tests | PR #45 |
| 2026-10-30 | P3-B03 Search, request, accept, cancel: pickup-based searchRides, ranking, requestSeats with the wallet hold, respondToRequest, cancelBooking, 549 tests | PR #46 |
| 2026-10-30 | P3-A08 Demo data and sign-in: phone sign-in, repositories for auth, profile, wallet, places, vehicles, rides, bookings, trip and inbox, root navigation by role | PR #48 |
| 2026-10-30 | P3-B04 Trip day: startRide, arriveAtStop, checkIn, markBoarded, reportLate, finishRide with one credit and a ledger row, 586 tests | PR #49 |
| 2026-10-30 | P3-B05 Wallet payments: confirmRidePayment and topUpWallet, every balance equal to the sum of its entries, 601 tests | PR #50 |
| 2026-10-30 | P3-B06 Demo inbox: onBookingChanged, payment_confirmed, the inbox written even when the push fails, 610 tests | PR #51 |
| 2026-10-30 | P3-A09 Rider demo screens: auth, profile setup, place picker, rider Home and search, ride details, rider trip, Rides tab, notifications, 133 unit tests | PR #53 |
| 2026-10-30 | Fix: the Compose resources are packaged into the APK; the app crashed on launch with a MissingResourceException and CI did not catch it | PR #57 |
| 2026-10-30 | P3-A11 Maps SDK: play-services-maps and maps-compose, the key from MAPS_API_KEY into the manifest, HitchyMap with fixed pins and a straight route line, dev gallery Map section, 6 tests | PR #54 |
| 2026-10-30 | P3-A10 Owner demo screens: owner Home, Post ride with stops, ride details with requests, host trip with the free wait, Confirm Payment | PR #59 |
| 2026-10-30 | P3-A09 rework: rider entry screens built to the Figma DEMO frames, design tokens fixed to Figma, the +20 phone number accepted, 29 strings with a stray backslash fixed | PR #58 |
| 2026-10-30 | Fix: owner app sends UUID request ids (offer a ride and Confirm Payment were refused), detekt errors cleared on main | PR #60 |
| 2026-10-30 | Fix: the owner sees the rider's first name, not part of the user id | PR #61 |

Deploy jobs skip until each GitHub environment has its Phase 0 secrets (ADR 0003 row 18).

## Open items
- [HUMAN] The GitHub repo isllamm/Hitchy is public; the plan (P0-H01) says private.
- [HUMAN] Phase 0: Firebase projects hitchy-dev and hitchy-prod, region, phone sign-in, Android apps, release key, App Check, CI access (Workload Identity) and secrets, TTL policies on idempotency, notifications and messages, the auto-stop in hitchy-dev, the cleanup rule for old function builds, and the Storage permission to read Firestore (P0-H01 to P0-H15).
- [HUMAN] Both projects are on Blaze and set up to cost 0 USD (2026-10-29, BUILD_PLAN Phase 0 "Keeping both projects at 0 USD"): Storage bucket in us-central1 (free tier), no real text messages until launch (SMS region policy allows no region), and only the free map (BUILD_PLAN 5.10). Still to do in hitchy-dev: budget alerts, auto-stop, Storage bucket, P0-H15, then the `dev` environment secrets (CI starts deploying when they exist), then P0-H14. Before launch: check that ID photos may be stored in the US under Egypt's data protection law.
- [HUMAN] The iOS teammate's GitHub username in .github/CODEOWNERS.
- [HUMAN] **The seed's `config/limits` holds demo values** (ADR 0003 row 81): `minLeadMinutes` 5, `maxRidesPerHostPerDay` 10, `rideGapMinutes` 90 → 5. The plan's values are 30, 2 and 90 (BUILD_PLAN 7.3). Restore them before `hitchy-dev` or `hitchy-prod` serves anyone, because `npm run seed -- --project dev` writes the same file. The tests pin the plan's numbers, so the real rules stay tested.
- [HUMAN] P3-H01: real names and coordinates for the demo pickup points in `backend/seed/data/zones.json` (today they are "Test point 1"), and `MAPS_API_KEY` in `android/local.properties`. Never invent coordinates.
- The Emulator Suite does not emulate FCM, so no push arrives on the emulators. The Notifications screen works because it reads the inbox from Firestore; the system notification tray does not.
- Not built yet, by plan: `expireRequest` (P7-B02), so a request waits until the owner answers; the `rideReminder` enqueue (P7-B05); `onRideFinished` and `closeStaleRides` (P8-B06), so `tripsCompleted` and `verifiedTogether` stay unchanged after a trip; `cancelBooking` with reason `host_late` (P7-B03, needs trip presence); fee disputes (P8-B04).
- [DECISION-01] Functions region is the default `europe-west1`. [DECISION-16] `ADMIN_URL` in backend/functions/src/lib/app.ts is "TODO-HUMAN" until P5-W01 creates the admin sites.
- A user whose activeRole differs from the notification's audience (e.g. booking_requested while in rider mode) gets the wrong screen; to fix in the notification-tap task.

## Local setup (Islam's Mac)
- Node 24.22.1 from Homebrew (`node@24`, put on PATH in ~/.zshrc) with npm 11; Java 21 (Temurin) is the default `java` for the Firebase emulators; Android uses JDK 17.
- GitHub CLI `gh` is signed in; use it for pull requests and checks.
- npm can time out on this network. Retry with `--fetch-retries=6 --fetch-retry-mintimeout=20000 --fetch-retry-maxtimeout=120000`.
- npm 11 skips some packages' install scripts (esbuild, re2, protobufjs, @firebase/util, google-logging-utils); leave them off unless a task needs one (ADR 0003 row 17).
- Run one `brew` command at a time; parallel installs break each other.
- The first `npm test` downloads the emulator files. Never deploy from this Mac; CI deploys.
