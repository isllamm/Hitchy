# Hitchy — End-to-End Build Plan (Android + Firebase backend)

> **For:** Claude Code and the Hitchy team
> **Version:** 1.1 · **Date:** 2026-10-29 · **Owner:** Islam
> **Scope:** the Android app (Kotlin + Compose Multiplatform), the admin web dashboard (React + TypeScript) and the Firebase backend. The iOS app is built separately in Swift by the iOS teammate, using only `contract/`.
> **Based on:** Hitchy Feasibility Study v10, the "Hitchy — Android & Backend Build Plan" doc, and the app designs reviewed on 2026-10-29.
> **Changes in 1.1:** the design review decisions in [2.1](#21-design-review-v11) (owner accepts bookings, several seats per booking, chat and calls, payment confirmation, fixed points with the designs' picker, one picked time, "Owner" wording, Shop tab, notification inbox) and the screen map in [3.8](#38-app-designs-screen-map-and-fix-list).

---

## Table of contents

0. [How to use this file](#0-how-to-use-this-file)
1. [Rules for the agent](#1-rules-for-the-agent)
2. [Product rules](#2-product-rules) · [2.1 Design review (v1.1)](#21-design-review-v11)
3. [Architecture](#3-architecture)
4. [Repo layout](#4-repo-layout)
5. [Conventions](#5-conventions)
6. [Data model (Firestore)](#6-data-model-firestore)
7. [Config and seed data](#7-config-and-seed-data)
8. [Functions contract](#8-functions-contract)
9. [Security rules](#9-security-rules)
10. [Indexes](#10-indexes)
11. [Phase 0 — Accounts, GitHub, Firebase](#phase-0--accounts-github-firebase)
12. [Phase 1 — Backend foundation](#phase-1--backend-foundation)
13. [Phase 2 — Android foundation](#phase-2--android-foundation)
14. [Phase 3 — Design system](#phase-3--design-system)
15. [Phase 4 — Sign-in, profile, roles](#phase-4--sign-in-profile-roles)
16. [Phase 5 — Host verification and admin](#phase-5--host-verification-and-admin)
17. [Phase 6 — Posting rides and pricing](#phase-6--posting-rides-and-pricing)
18. [Phase 7 — Search, booking, notifications](#phase-7--search-booking-notifications)
19. [Phase 8 — Trip day](#phase-8--trip-day)
20. [Phase 9 — Packages and credits](#phase-9--packages-and-credits)
21. [Phase 10 — Hitchy Rewards](#phase-10--hitchy-rewards)
22. [Phase 11 — AI assistant](#phase-11--ai-assistant)
23. [Phase 12 — Fixed Ride and backup rides](#phase-12--fixed-ride-and-backup-rides)
24. [Phase 13 — Safety, privacy, account deletion](#phase-13--safety-privacy-account-deletion)
25. [Phase 14 — Release and operations](#phase-14--release-and-operations)
- [Appendix A — CLAUDE.md](#appendix-a--claudemd)
- [Appendix B — Prompts for Claude Code](#appendix-b--prompts-for-claude-code)
- [Appendix C — End-to-end test script](#appendix-c--end-to-end-test-script)
- [Appendix D — Error catalog (EN/AR)](#appendix-d--error-catalog-enar)
- [Appendix E — Analytics events](#appendix-e--analytics-events)
- [Appendix F — Open decisions and defaults](#appendix-f--open-decisions-and-defaults)

---

## 0. How to use this file

### 0.1 Setup

1. Save this file in the repo as `docs/BUILD_PLAN.md`.
2. Copy [Appendix A](#appendix-a--claudemd) into `CLAUDE.md` at the repo root. Claude Code reads `CLAUDE.md` at the start of every session ([Claude Code docs](https://docs.claude.com/en/docs/claude-code/overview)).
3. Build in order: Phase 0 → Phase 14. Inside a phase, do backend tasks (`B`) first, then Android (`A`) and admin web (`W`), unless the task says otherwise.
4. One task per session or per commit. Start each task with the prompt in [0.4](#04-prompt-to-start-a-task).

### 0.2 Task IDs

| Prefix | Meaning | Example |
| --- | --- | --- |
| `P<n>-B<nn>` | Backend task in phase n | `P7-B02` = `requestSeats` |
| `P<n>-A<nn>` | Android task in phase n | `P7-A03` = request sheet |
| `P<n>-W<nn>` | Admin web task in phase n | `P5-W05` = host review page |
| `P<n>-H<nn>` | Human task (console, accounts, money) | `P0-H02` = create Firebase projects |

Every task has: **Goal**, **Files**, **Spec** (or a link to sections 6–10), **Done when**, **Verify**.

### 0.3 Markers

| Marker | Meaning | What the agent does |
| --- | --- | --- |
| `[HUMAN]` | Needs console, account, or payment access | Do not attempt. List it at the end of the task summary for Islam. |
| `[DECISION-NN]` | Open decision ([Appendix F](#appendix-f--open-decisions-and-defaults)) | Use the default value given there. Add `TODO(DECISION-NN)` next to the code that uses it. |
| `[FIGMA]` | The screen layout is not designed yet | Build the screen from the Phase 3 tokens and components with a simple stacked layout. No new colors, fonts or visual styles. All logic, state and strings are still complete. |
| `[LATER]` | Not part of the pilot | Do not build. |

### 0.4 Prompt to start a task

```text
Read docs/BUILD_PLAN.md sections 1 to 10, then the task <TASK-ID>.
Implement only that task, following CLAUDE.md.
Run every command under "Verify" and fix failures before you stop.
If the spec is unclear or conflicts with existing code, stop and ask me.
At the end, list: files changed, test results, and any [HUMAN] or [DECISION] items.
```

---

## 1. Rules for the agent

These rules are not negotiable. Every pull request is checked against them.

1. **Server decides.** The apps never write money, seats, credits, points, ratings, fees, roles, or any `status` field. Only Cloud Functions write them.
2. **Validate and lock.** Every callable input is parsed with zod. Every write that depends on a read happens inside one Firestore transaction.
3. **Contract in the same commit.** Any change to a function input/output or a Firestore field also updates `contract/` (docs and fixtures) in the same commit.
4. **No secrets, no personal data in logs.** No keys in code or in the repo. Never log or send to analytics: phone numbers, ID numbers, plates, locations, or chat text.
5. **Done means tested.** A task is done only when its tests pass locally and in CI. Never delete or skip a failing test to make CI green.
6. **Time and money.** Store instants as Firestore `Timestamp` (UTC). Show them in `Africa/Cairo`. Money is integer Egyptian pounds (EGP).
7. **Text in resources.** Every user-facing string lives in resources, in English and Arabic. No hard-coded text in code.
8. **No surprise dependencies.** Use only the libraries in [4.4](#44-libraries). Anything else needs a one-line reason in the pull request.
9. **No invented design.** Use only the Phase 3 tokens and components. Do not invent colors, fonts or styles. Follow `[FIGMA]` for screens not designed yet.
10. **Small commits.** Conventional Commits with scope: `feat(backend): requestSeats`, `test(android): otp viewmodel`.
11. **Ask instead of guessing.** If the spec does not cover a case, stop and ask. Do not invent business rules.

---

## 2. Product rules

Hitchy matches commuters in Greater Cairo who drive to work with colleagues going the same way. Riders use the app for free. Hosts (car owners) buy trip packages. Seat money goes from rider to host directly (cash or InstaPay); Hitchy never holds it.

| # | Rule | Value | Enforced in |
| --- | --- | --- | --- |
| R1 | Riders pay Hitchy nothing | — | No rider payment code anywhere |
| R2 | Host trip packages | 10 trips = 99 EGP, 20 = 179, 30 = 249, Fixed Ride 40 = 249 | `requestPackage`, `confirmPackageOrder` |
| R3 | A trip uses 1 credit only if at least 1 rider boarded | — | `finishRide` |
| R4 | First month free | 30 days from host approval | `approveHost`, `finishRide`, `createRide` |
| R5 | Max seats | car 3, scooter 1 | `addVehicle`, `createRide` |
| R6 | Seat price range | min / recommended / max formula, [Pricing formula](#pricing-formula); the rider pays price × seats to the host directly and sees a receipt | `quotePrice`, `createRide` |
| R7 | Max rides per host per day | 2, one each direction | `createRide`, `generateFixedRides` |
| R8 | Women-only rides | only women hosts create them; only women riders see and request them; a rider asking for several seats confirms every passenger is a woman `[DECISION-24]` | `createRide`, `searchRides`, `requestSeats` |
| R9 | Rider late fee | 10 EGP per booking after 5 minutes grace, paid to the host directly; an unpaid fee blocks new requests `[DECISION-24]` | `reportLate`, `requestSeats` |
| R10 | Host late | more than 10 minutes late → rider cancels free | `cancelBooking` |
| R11 | Verified badge | optional criminal record → badge, booking priority, higher ranking | `setVerified`, `domain/ranking.ts` |
| R12 | One account per phone, two roles | host mode only when `host.status == "approved"`; the Welcome screen's "I have a vehicle / I need a ride" only picks the starting path | `switchRole`, host functions |
| R13 | Same company badge | shown without the company name | `searchRides` |
| R14 | AI assistant | real rides only, daily limit, 1,000 EGP monthly budget | `askAssistant` |
| R15 | Hitchy Rewards | points, tiers, redemption, 6-month expiry | Phase 10 |
| R16 | Points need proof | host and rider phones traveled together (GPS) | `onRideFinished` |
| R17 | Fixed Ride | weekly plan turned into daily rides | Phase 12 |
| R18 | Phone numbers stay private | only `getContactPhone` returns another user's phone, and only between a host and a rider with an accepted booking, from acceptance until the ride ends `[DECISION-22]` | `getContactPhone`, all |
| R19 | No wallet | seat price and late fees are paid between people | — |
| R20 | The host accepts each booking | a request holds its seats until the host accepts or declines, or the request expires `[DECISION-21]` | `requestSeats`, `respondToRequest`, `expireRequest` |
| R21 | Several seats per booking | one booking holds 1 … seats left; price, cancel and late fee apply per booking | `requestSeats` |
| R22 | Payment proof | after the trip the host marks each rider as paid; a record only, no money passes through Hitchy | `confirmSeatPayment` |
| R23 | Chat and calls | only between the host and riders with an accepted booking `[DECISION-22]` `[DECISION-23]` | `sendChatMessage`, `getContactPhone` |

**Pilot:** route Fifth Settlement / Rehab ↔ Smart Village, peak hours 07:00–10:00 and 16:00–19:00 Cairo time.

**Words:** the app's text calls the host **Owner** (Arabic صاحب العربية) and never "driver" `[DECISION-20]`. Code, data and this plan keep the name `host`. In the data and security tables, "owner" still means the account that owns a document.

### 2.1 Design review (v1.1)

On 2026-10-29 the finished app designs (46 PNG exports: onboarding, sign-in, home, route picker, search, ride details, my rides, tracking, rating, receipt, notifications, shop) were checked against this plan. Islam decided every conflict, and the rest of the plan follows these decisions. The screen map and the fix list for the designer are in [3.8](#38-app-designs-screen-map-and-fix-list).

| # | Topic | Decision | Where it changes the plan |
| --- | --- | --- | --- |
| 1 | Booking | The host accepts each booking. A request holds the seats until the host answers or it expires (R20) | `requestSeats`, `respondToRequest`, `expireRequest` (8.6), booking statuses (6.5), rules (9) |
| 2 | Rider fee | Riders pay Hitchy nothing (R1 unchanged). The design's "Pay the Hitchy fee" line goes | 3.8 fix list |
| 3 | Price | The host's price per seat (R6). The rider sees price × seats and a receipt "paid directly to the owner" | RideCard (8.2), receipt (P8-A10) |
| 4 | Payment proof | The host marks each rider as paid after the trip (R22) | `confirmSeatPayment` (8.7) |
| 5 | Places | Fixed points inside zones, picked with the designs' search, saved, recent and map picker. Only points appear, never free addresses or pins. "300 m walk" is the distance from the rider's chosen point to the ride's pickup point | `searchRides` (8.6), `savedPlaces` (6.2), P7-A02 |
| 6 | Time | The rider picks one time; search covers 1 hour before and after it `[DECISION-25]`. Arrival = departure + the route's usual minutes | `searchRides` (8.6), RideCard (8.2) |
| 7 | Seats | One booking can hold several seats, up to the seats left (R21) | `BookingDoc.seats` (6.5), `requestSeats` (8.6) |
| 8 | Contact | Chat and calls between the host and riders with an accepted booking (R18, R23) | `sendChatMessage`, `getContactPhone` (8.7), `[DECISION-22]`, `[DECISION-23]` |
| 9 | Bottom tabs | Home, Rides, Shop, Profile. Shop holds Rewards (rider) and Packages (host) | `[DECISION-18]`, P4-A04, Phases 9 and 10 |
| 10 | Word | The app says "Owner"; code and data keep `host` | `[DECISION-20]`, 8.15, Appendix D |
| 11 | Trip steps | The designs' steps on plan data: Owner on the way → Arrived → In ride → Completed | 3.6, P8-A04 |
| 12 | Rating | The designs' tags plus the plan's negative tags; no free comment | `RATING_TAGS` (6.6), P8-A06 |
| 13 | Search filters | The designs' sort and filters plus Verified only and Women-only | `searchRides` (8.6), P7-A02 |
| 14 | Role | The Welcome choice only picks the starting path; one account, two roles (R12) | P4-A04 |

The designs' Notifications screen needs a history, so every push is also stored in an inbox for 30 days (`users/{uid}/notifications`, 6.2) `[DECISION-27]`.

---

## 3. Architecture

```text
┌──────────── Apps ─────────────┐        ┌──────────────── Firebase (one region) ────────────────┐
│                               │ sign in│  Auth (phone OTP) + App Check                         │
│  Android app                  ├───────►│                                                       │
│  Kotlin + Compose MP          │  read  │  Firestore ◄──────── write ────────┐                  │
│                               ├───────►│  (security rules)                  │                  │
│                               │  call  │                                    │                  │
│  iOS app                      ├───────►│  Cloud Functions (2nd gen, TS) ────┘                  │
│  Swift, separate codebase     │ upload │    callables · triggers · jobs · task queues ──► LLM API
│  Admin web (React + TS)       ├───────►│  Storage (ID photos, avatars)                         │
│                               │◄───────┤  Cloud Messaging (notifications) ◄── Cloud Functions  │
└───────────────────────────────┘ notify └───────────────────────────────────────────────────────┘
```

- **Apps** read through security rules, upload photos to Storage, write only their own small fields (profile basics, device tokens, trip presence), and call functions for everything else.
- **Admin web** (`admin-web/`) is a separate React + TypeScript site on Firebase Hosting for the team. Admins sign in with Google; every change calls an admin-only function, and the same security rules let admins read what they review.
- **Cloud Functions** own all business logic: pricing, booking, credits, fees, points, ratings, notifications, scheduled jobs.
- **`contract/`** describes every document and function. The iOS app is built from it, never from Android code.
- **Remote Config** holds app-side flags only (feature toggles, minimum app version). Business numbers live in Firestore `config/*` so functions can read them.

---

## 4. Repo layout

### 4.1 Top level

```text
hitchy/
├─ .github/
│  ├─ workflows/            backend.yml · android.yml · admin-web.yml · ios.yml
│  ├─ CODEOWNERS
│  ├─ pull_request_template.md
│  ├─ ISSUE_TEMPLATE/       bug.md · task.md
│  └─ dependabot.yml
├─ contract/
│  ├─ README.md             how to change the contract safely
│  ├─ firestore.md          every path, field, type, who writes it
│  ├─ functions.md          every function: input, output, errors
│  ├─ errors.md             every reason + English and Arabic message
│  └─ fixtures/             <function>.request.json · <function>.response.json
├─ backend/                 see 4.2
├─ android/                 see 4.3
├─ admin-web/               see 4.5
├─ ios/                     iOS teammate
├─ docs/
│  ├─ BUILD_PLAN.md         this file
│  └─ adr/                  0001-architecture.md · 0002-versions.md · …
├─ .gitignore
├─ CLAUDE.md                Appendix A
└─ README.md                run everything locally
```

### 4.2 Backend

```text
backend/
├─ firebase.json            emulators, functions, rules, hosting
├─ .firebaserc              aliases dev/prod + hosting targets site/admin
├─ firestore.rules
├─ firestore.indexes.json
├─ storage.rules
├─ hosting/public/          account deletion page (Phase 13)
├─ seed/
│  ├─ seed.ts               writes config, zones, routes, companies, test users
│  └─ data/                 config.json · zones.json · routes.json · companies.json · users.json
├─ scripts/
│  ├─ set-admin.ts          sets the admin claim and config/admins
│  └─ migrations/           NNNN-description.ts
└─ functions/
   ├─ package.json · tsconfig.json · .eslintrc.cjs · .prettierrc · vitest.config.ts · .nvmrc
   ├─ src/
   │  ├─ index.ts           exports only
   │  ├─ lib/
   │  │  ├─ app.ts          initializeApp, db, auth, storage, messaging, REGION
   │  │  ├─ callable.ts     makeCallable(): options, auth, zod, errors, logging
   │  │  ├─ errors.ts       Reason union, AppFailure, fail(), toHttps()
   │  │  ├─ ctx.ts          CallCtx, buildCtx()
   │  │  ├─ clock.ts        now() — replaceable in tests
   │  │  ├─ config.ts       getConfig(name) with 60 s cache
   │  │  ├─ time.ts         Cairo helpers (luxon)
   │  │  ├─ notify.ts       sendToUser(), sendToUsers(), sendToAdmins(), token cleanup
   │  │  ├─ codes.ts        unique short codes (reference, invite, referral)
   │  │  ├─ idempotency.ts  withIdempotency()
   │  │  └─ audit.ts        logAdminAction()
   │  ├─ schemas/
   │  │  ├─ docs.ts         TypeScript types of every document (section 6)
   │  │  ├─ io.ts           TypeScript types of function outputs (section 8)
│  │  ├─ config.ts       zod schemas of every config doc (section 7)
   │  │  └─ inputs.ts       zod schemas of every callable input (section 8)
   │  ├─ domain/            pure logic, no Firebase imports
   │  │  ├─ pricing.ts · ranking.ts · rewards.ts · tiers.ts
   │  │  ├─ together.ts     traveled-together check
   │  │  ├─ geo.ts          haversine distance
   │  │  └─ impact.ts       fuel and CO₂ saved
   │  ├─ repos/             all Firestore reads and writes, one file per collection
   │  ├─ handlers/          one file per callable, grouped by feature
   │  │  ├─ profile/ · host/ · admin/ · rides/ · booking/ · trip/
   │  │  └─ packages/ · rewards/ · assistant/ · fixed/ · safety/
   │  ├─ triggers/          syncPublicProfile.ts · onBookingChanged.ts · onRideFinished.ts
   │  ├─ jobs/              scheduled functions
   │  ├─ tasks/             task queue functions
   │  └─ ai/                llmClient.ts · providers/ · prompt.ts · tools.ts
   └─ test/
      ├─ unit/              domain tests (no emulator)
      ├─ rules/             Firestore and Storage rules tests
      ├─ integration/       handler cores against the emulators
      └─ helpers/           seed, fixed clock, fake LLM, auth contexts
```

### 4.3 Android

```text
android/
├─ settings.gradle.kts · build.gradle.kts · gradle.properties
├─ gradle/libs.versions.toml
├─ build-logic/convention/   hitchy.android.application · hitchy.kmp.library · hitchy.quality
├─ config/detekt/detekt.yml
├─ app/                      Android application module (entry point only)
│  ├─ src/main/AndroidManifest.xml
│  ├─ src/main/kotlin/com/hitchy/app/
│  │  ├─ HitchyApplication.kt     Koin, Firebase, App Check, Crashlytics
│  │  ├─ MainActivity.kt          AppCompatActivity hosting the Compose root
│  │  ├─ messaging/HitchyMessagingService.kt
│  │  └─ location/TripLocationService.kt
│  ├─ src/main/res/xml/locales_config.xml
│  ├─ src/dev/google-services.json     (git-ignored)
│  └─ src/prod/google-services.json    (git-ignored)
└─ shared/                   KMP + Compose Multiplatform, target: androidTarget only
   └─ src/
      ├─ commonMain/kotlin/com/hitchy/
      │  ├─ core/model/      pure Kotlin models and enums
      │  ├─ core/data/       Functions client, Firestore helpers, AppError, AppResult, DTOs
      │  ├─ core/platform/   interfaces implemented in androidMain
      │  ├─ core/ui/         theme [FIGMA], shared components
      │  ├─ core/util/       CairoTime, MoneyFormat, PhoneNumber
      │  ├─ feature/<name>/  data/ · domain/ · presentation/ · ui/
      │  ├─ navigation/      Routes.kt · HitchyNavHost.kt · DeepLinks.kt
      │  └─ di/              Koin modules
      ├─ commonMain/composeResources/   values/strings.xml · values-ar/strings.xml
      ├─ commonTest/kotlin/…
      └─ androidMain/kotlin/com/hitchy/platform/   Android implementations
```

Features under `feature/`: `auth`, `profile`, `host`, `rides`, `booking`, `trip`, `packages`, `rewards`, `assistant`, `fixed`, `safety`, `debug`, and from v1.1 `places` (route picker, saved places), `chat` and `inbox` (notification history).

### 4.4 Libraries

Pin every version in `gradle/libs.versions.toml` or `package.json`. In `P1-B01` and `P2-A01`, look up the latest **stable** release of each, pin it exactly (no `+`, no ranges), and record the list in `docs/adr/0002-versions.md`.

| Side | Need | Library |
| --- | --- | --- |
| Backend | Runtime | Node.js LTS supported by Cloud Functions, TypeScript (strict) |
| Backend | Firebase | `firebase-functions` (v2 APIs), `firebase-admin` (modular imports) |
| Backend | Validation | `zod` |
| Backend | Time | `luxon` |
| Backend | Tests | `vitest`, `@firebase/rules-unit-testing`, Firebase Emulator Suite |
| Backend | Lint | `eslint`, `typescript-eslint`, `prettier` |
| Android | UI | Compose Multiplatform + Material 3 |
| Android | ViewModel | JetBrains `lifecycle-viewmodel-compose` |
| Android | Navigation | JetBrains `navigation-compose` (type-safe `@Serializable` routes) |
| Android | DI | Koin (`koin-core`, `koin-compose`, `koin-compose-viewmodel`, `koin-android`) |
| Android | Firebase in `commonMain` | GitLive `firebase-kotlin-sdk`: `firebase-auth`, `firebase-firestore`, `firebase-functions`, `firebase-storage` |
| Android | Firebase in `androidMain`/`app` | Official Firebase Android SDK via BoM: Auth (phone flow), Messaging, Crashlytics, Analytics, App Check (Play Integrity + debug), Remote Config |
| Android | Async, JSON, time | kotlinx.coroutines, kotlinx.serialization, kotlinx-datetime |
| Android | Images, settings, logs | Coil 3, DataStore Preferences, Kermit |
| Android | Location | Google Play services Location (Fused Location Provider) |
| Android | Tests | kotlin.test, kotlinx-coroutines-test, Turbine, Konsist, Roborazzi (after Figma) |
| Android | Quality | detekt, Spotless + ktlint, Android Lint, LeakCanary (`dev` debug only) |
| Admin web | App | React, TypeScript (strict), Vite, React Router |
| Admin web | Firebase | Firebase JS SDK (modular): Auth (Google), Firestore, Functions, Storage, Messaging, App Check (reCAPTCHA Enterprise) |
| Admin web | UI | Mantine (core, notifications, dates) |
| Admin web | Validation | `zod`, using the backend schemas |
| Admin web | Tests | Vitest, React Testing Library, Playwright (against the emulators) |
| Admin web | Lint | `eslint`, `typescript-eslint`, `prettier` |

### 4.5 Admin web

```text
admin-web/
├─ package.json · tsconfig.json · vite.config.ts · .eslintrc.cjs · playwright.config.ts · index.html
├─ public/firebase-messaging-sw.js   web push service worker
├─ src/
│  ├─ main.tsx · App.tsx · routes.tsx
│  ├─ firebase/       init.ts (config per mode, emulators, App Check) · config.dev.ts · config.prod.ts
│  ├─ auth/           AuthProvider.tsx · RequireAdmin.tsx
│  ├─ data/           useDoc · useCollection · useCount · useCallable
│  ├─ components/     Layout · EnvBadge · DataTable · ImageViewer · ConfirmDialog · ReasonPicker · JsonConfigEditor
│  ├─ pages/          Login · NotAuthorized · Dashboard · Hosts · HostReview · Vehicles · Orders · Disputes
│  │                  Reports · Sos · Users · UserDetail · Config · Audit
│  └─ lib/            errors.ts (reason → English message) · format.ts (Cairo time, EGP)
└─ e2e/               Playwright tests
```

Rules for the admin web:

- It imports the backend types and zod schemas from `backend/functions/src/schemas/` through a path alias `@schemas/*`, so the two never drift. Those files therefore import nothing from Firebase (see 6.1).
- It never writes Firestore directly. Every change calls an admin-only function. The only exception is its own web push token in `users/{uid}/devices`.
- English only `[DECISION-17]`, Mantine components with the Hitchy colors and Cairo font (P3-W01).
- A coloured environment badge (DEV yellow, PROD red) is always visible, so nobody approves on the wrong project.
- Its own Firebase Hosting site (`admin`) with headers `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex`, and a Content-Security-Policy that allows only the app and Google/Firebase endpoints.

---

## 5. Conventions

### 5.1 Time

- Instants are Firestore `Timestamp` (UTC) in documents and ISO-8601 UTC strings (`"2026-11-02T05:30:00Z"`) in function inputs and outputs.
- The business time zone is `Africa/Cairo` (Egypt uses summer time). Never hard-code +2 or +3.
- `dateKey` = the Cairo calendar date, `"YYYY-MM-DD"`. Times of day are `"HH:mm"` in Cairo time.
- Backend: `lib/time.ts` with luxon (`toCairo(date)`, `dateKey(date)`, `minuteOfDay(date)`, `cairoDateTime(dateKey, "HH:mm")`, `startOfCairoDay`, `startOfCairoWeek` (Sunday)).
- Android: `core/util/CairoTime.kt` with kotlinx-datetime and `TimeZone.of("Africa/Cairo")`.
- Code never calls the system clock directly: backend uses `lib/clock.ts`, Android injects a `Clock`. Tests set the time.

### 5.2 Money and numbers

- Integer EGP everywhere (`pricePerSeat: 56`).
- Only `domain/pricing.ts` uses decimals, and it ends with `floorEgp(x) = Math.floor(Math.round(x * 100) / 100)`.
- Digits on screen: `[DECISION-04]` (default: Western digits, Arabic text).

### 5.3 IDs and codes

| Document | ID |
| --- | --- |
| `users`, `profiles`, `kyc`, `presence` | Firebase UID |
| `bookings` | rider UID (one booking per rider per ride) |
| `ratings` | `{rideId}_{fromUid}_{toUid}` |
| `aiUsage` | `{uid}_{yyyymmdd}` (Cairo date) |
| Fixed Ride daily ride | `fixed_{fixedRideId}_{dateKey}` |
| `codes` | the code itself |
| everything else | Firestore auto ID |

Short codes (package reference `HT-XXXXXX`, invite code, referral code) use 6 characters from `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`. Uniqueness: create `codes/{code}` inside a transaction and retry on collision.

### 5.4 Idempotency

Callables that create things take `requestId` (a UUID the app makes once per user action). `lib/idempotency.ts` stores `idempotency/{uid}_{requestId}` with the result inside the same transaction; a repeat call returns the stored result. Add a Firestore TTL policy on `idempotency.expireAt` (24 hours) `[HUMAN]`.

### 5.5 Errors

- Business errors use a `reason` from [Appendix D](#appendix-d--error-catalog-enar).
- Backend: `fail("RIDE_FULL")` throws `AppFailure`; `makeCallable` turns it into `HttpsError(code, reason, { reason, extra })`.
- Android: the Functions client maps the error to `AppError.Reason(reason, extra)`. Each reason has a string resource `error_<reason_lowercase>`; unknown reasons show `error_internal`.
- The app retries only network errors (`AppError.Network`), never business errors.

### 5.6 Naming

- Kotlin package root `com.hitchy`. Firestore collections plural camelCase, fields camelCase, enum values lowercase snake (`"cancelled_by_host"`).
- String resource keys snake_case with a feature prefix: `booking_confirm_title`.
- Function names camelCase verbs: `requestSeats`, `finishRide`.

### 5.7 Logging and privacy

- Backend: `logger.info("requestSeats.ok", { uid, rideId })`. Structured, event-style names.
- Android: Kermit with one tag per feature.
- Never log or send to analytics: phone numbers, names, ID numbers, plates, coordinates, chat text.

### 5.8 Feature flags (Remote Config, app side)

| Key | Default | Turned on in |
| --- | --- | --- |
| `minVersionCode` | 1 | Phase 14 |
| `scooterEnabled` | false | after `[DECISION-07]` |
| `assistantEnabled` | false | Phase 11 |
| `fixedRideEnabled` | false | Phase 12 |
| `rewardsEnabled` | false | Phase 10 |

The server checks matching values in `config/limits` too, so a flag cannot be bypassed by an old app.

### 5.9 Git

- Branches `feat/<id>-short-name`, e.g. `feat/P7-B02-book-seat`.
- Conventional Commits with scope `backend`, `android`, `admin`, `contract`, `ci`, `docs`.
- Pull request title starts with the task ID.

---

## 6. Data model (Firestore)

These types are the source of truth. Put them in `backend/functions/src/schemas/docs.ts` and mirror them in `contract/firestore.md`. Android DTOs and iOS models follow the same names; the admin web imports these files directly.

### 6.1 Shared types

```ts
// No Firebase imports in schemas/: the admin web imports these files too.
// The Admin SDK and web SDK Timestamp classes both match this shape.
export interface Timestamp { seconds: number; nanoseconds: number; toDate(): Date }

export type Gender = "female" | "male";
export type Role = "rider" | "host";
export type Language = "ar" | "en";
export type Tier = "bronze" | "silver" | "gold";
export type VehicleType = "car" | "scooter";
export type Direction = "to_work" | "to_home";
export type TrafficLevel = "offPeak" | "normal" | "peak";
export type HostStatus = "none" | "pending" | "approved" | "rejected";
export type LocalizedName = { ar: string; en: string };
export type GeoPointLite = { lat: number; lng: number };
export type HHmm = string;    // "08:30", Cairo time
export type DateKey = string; // "2026-11-02", Cairo date

interface Base {
  schemaVersion: 1;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

### 6.2 Users and profiles

```ts
// users/{uid} — private. Read: owner, admin.
export interface UserDoc extends Base {
  phone: string;                         // "+2010…", taken from the auth token only
  firstName: string;
  lastName: string;
  gender: Gender;                        // locked after it is set (admin can fix)
  companyId: string | null;
  companyOther: string | null;           // free text when the company is not in the list
  workHours: { start: HHmm; end: HHmm } | null;
  language: Language;
  photoPath: string | null;              // "avatars/{uid}.jpg"
  emergencyContact: { name: string; phone: string } | null;
  savedPlaces: Array<{ label: "home" | "work" | "other"; zoneId: string; pointId: string }>;
                                         // max maxSavedPlaces; fixed points only (v1.1)
  activeRole: Role;                      // written only by switchRole
  referralCode: string;
  referredBy: string | null;             // uid of the inviter
  referralRewarded: boolean;
  blockedUids: string[];                 // max 200
  suspended: boolean;
  rider: {
    tier: Tier;
    ratingSum: number; ratingCount: number;
    tripsCompleted: number;
    unpaidFees: number;                  // unpaid + disputed fees
    lateCancels: number;
  };
  host: {
    status: HostStatus;
    verified: boolean;
    approvedAt: Timestamp | null;
    freeUntil: Timestamp | null;         // approvedAt + freeMonthDays
    freeEndingNotified: boolean;
    credits: { regular: number; fixed: number }; // may go below 0 (see finishRide)
    tier: Tier;
    ratingSum: number; ratingCount: number;
    tripsCompleted: number;
    lateCancels: number;
    hostLateCount: number;
  };
  points: { balance: number };
}

// Fields the owner may write directly (rules check this list):
export const OWNER_WRITABLE_USER_FIELDS = [
  "firstName", "lastName", "companyId", "companyOther", "workHours",
  "language", "photoPath", "emergencyContact", "savedPlaces", "updatedAt",
] as const;

// profiles/{uid} — public card. Read: any signed-in user. Written by syncPublicProfile.
export interface ProfileDoc {
  schemaVersion: 1;
  firstName: string;
  photoPath: string | null;
  hostRating: { avg: number | null; count: number };
  hostTrips: number;                     // host.tripsCompleted, for "★ 4.9 · 128 rides"
  riderRating: { avg: number | null; count: number };
  verified: boolean;
  hostTier: Tier;
  riderTier: Tier;
  updatedAt: Timestamp;
}

// users/{uid}/devices/{installId} — written by the owner.
export interface DeviceDoc {
  fcmToken: string;
  platform: "android" | "ios" | "web";
  appVersion: string;
  language: Language;
  updatedAt: Timestamp;
}

// users/{uid}/notifications/{id} — the inbox (v1.1). Written by lib/notify.ts with every push.
// Read: owner. The owner may set only readAt. Deleted by a TTL policy on expireAt [DECISION-27].
export interface NotificationDoc {
  type: string;                          // a type from section 8.15
  title: LocalizedName;                  // both languages, so a language switch still reads well
  body: LocalizedName;
  deepLink: string;
  createdAt: Timestamp;
  readAt: Timestamp | null;
  expireAt: Timestamp;                   // createdAt + notificationRetentionDays
}

// users/{uid}/vehicles/{vehicleId} — written by functions.
export interface VehicleDoc extends Base {
  type: VehicleType;
  make: string; model: string; year: number; color: string;
  plate: string;
  maxSeats: number;                      // from config: car 3, scooter 1
  status: "pending" | "approved" | "rejected";
}

// users/{uid}/ledger/{entryId} — every credit change. Written by functions.
export interface LedgerEntry {
  delta: number;                         // +10, -1, +3 …
  bucket: "regular" | "fixed";
  reason: "package" | "trip" | "reward" | "admin";
  refId: string;                         // orderId, rideId, points entry id
  balanceAfter: number;
  createdAt: Timestamp;
}

// users/{uid}/points/{entryId} — every points change. Written by functions.
export interface PointsEntry {
  amount: number;                        // + earn, - spend or expire
  remaining: number;                     // earn entries only; 0 for spend/expire
  open: boolean;                         // remaining > 0
  reason: "trip" | "on_time_week" | "five_star" | "fixed_week" | "referral"
        | "redeem" | "expire" | "admin";
  refId: string;
  expiresAt: Timestamp | null;           // earn entries only
  createdAt: Timestamp;
}
```

### 6.3 Host verification

```ts
export type KycDocKey = "idFront" | "idBack" | "drivingLicense" | "vehicleLicense" | "criminalRecord";

// kyc/{uid}. Read: owner, admin. Written by functions.
export interface KycDoc extends Base {
  status: "none" | "submitted" | "approved" | "rejected";
  vehicleId: string | null;
  docs: Record<KycDocKey, {
    path: string;                        // "kyc/{uid}/id_front.jpg"
    status: "missing" | "uploaded" | "accepted" | "rejected";
    rejectReason: "blurry" | "expired" | "mismatch" | "missing" | "other" | null;
  }>;
  submittedAt: Timestamp | null;
  reviewedAt: Timestamp | null;
  reviewerUid: string | null;
  note: string | null;
}
```

Storage file names: `id_front.jpg`, `id_back.jpg`, `driving_license.jpg`, `vehicle_license.jpg`, `criminal_record.jpg`.

### 6.4 Places

```ts
// companies/{companyId}. Read: signed-in. Written by admin scripts.
export interface CompanyDoc { name: LocalizedName; zoneId: string; active: boolean }

// zones/{zoneId}. Read: signed-in.
export interface ZoneDoc { name: LocalizedName; active: boolean; order: number }

// zones/{zoneId}/points/{pointId}. Read: signed-in.
export interface PointDoc { name: LocalizedName; lat: number; lng: number; active: boolean }

// routes/{fromZone}__{toZone}. Read: signed-in.
export interface RouteDoc { fromZone: string; toZone: string; distanceKm: number; usualMinutes: number }
```

### 6.5 Rides and bookings

```ts
export type RideStatus = "open" | "full" | "started" | "finished" | "cancelled" | "expired";

// rides/{rideId}. Read: host, riders with a booking, admin. Written by functions.
export interface RideDoc extends Base {
  hostId: string;
  hostCompanyId: string | null;
  vehicleId: string;
  vehicle: { type: VehicleType; make: string; model: string; color: string; plate: string };
  fromZone: string; fromPointId: string; fromPointName: LocalizedName; fromPoint: GeoPointLite;
  toZone: string;   toPointId: string;   toPointName: LocalizedName;
  direction: Direction;
  departAt: Timestamp;
  dateKey: DateKey;
  seatsTotal: number;
  seatsLeft: number;                     // seats not held by a requested or accepted booking
  pricePerSeat: number;
  priceRange: { min: number; recommended: number; max: number; trafficLevel: TrafficLevel;
                distanceKm: number; configVersion: number };
  womenOnly: boolean;
  status: RideStatus;
  fixedRideId: string | null;
  bookingsCount: number;                 // accepted bookings, for "first booking" logic
  creditConsumed: boolean;
  postProcessed: boolean;                // set by onRideFinished
  autoFinished: boolean;
  startedAt: Timestamp | null;
  finishedAt: Timestamp | null;
  cancelledAt: Timestamp | null;
  cancelReason: "sick" | "car_problem" | "plans_changed" | "other" | "not_started" | "suspended" | null;
}

export type BookingStatus = "requested" | "booked" | "boarded" | "completed"
  | "declined" | "expired" | "cancelled_by_rider" | "cancelled_by_host" | "no_show";
// "requested" waits for the host; "booked" means the host accepted (R20).

// rides/{rideId}/bookings/{riderUid}. Read: that rider, the host, admin. Written by functions.
export interface BookingDoc extends Base {
  rideId: string;
  riderUid: string;
  hostId: string;
  departAt: Timestamp;                   // copy, for the rider's trips query
  status: BookingStatus;
  seats: number;                         // 1 … seatsLeft at request time (R21)
  price: number;                         // per seat, copied at request time
  total: number;                         // price × seats, paid to the host directly
  expiresAt: Timestamp;                  // request deadline [DECISION-21]
  respondedAt: Timestamp | null;         // when the host accepted or declined
  checkedInAt: Timestamp | null;
  checkInDistanceM: number | null;
  boardedAt: Timestamp | null;
  lateFeeId: string | null;
  paymentConfirmedAt: Timestamp | null;  // the host confirmed the rider paid (R22)
  verifiedTogether: boolean;
  rewarded: boolean;
  lateCancel: boolean;
  cancelledAt: Timestamp | null;
  cancelReason: "host_late" | "plans_changed" | "found_another_ride" | "other"
              | "ride_cancelled" | "not_started" | null;
}

// rides/{rideId}/presence/{uid}. Written by the owner while the ride is started.
export interface PresenceDoc {
  last: { lat: number; lng: number; accuracyM: number; at: Timestamp };
  samples: Array<{ lat: number; lng: number; at: Timestamp }>; // max 200
  updatedAt: Timestamp;
}

// rides/{rideId}/chats/{riderUid}/messages/{messageId} (v1.1, R23).
// Read: the host and that rider (not admins). Written by sendChatMessage.
export interface ChatMessageDoc {
  fromUid: string;
  text: string;                          // 1–500 characters; never logged
  createdAt: Timestamp;
  expireAt: Timestamp;                   // TTL: ride departAt + chatRetentionDays [DECISION-23]
}
```

### 6.6 Fees, ratings, packages

```ts
// fees/{feeId}. Read: that rider, that host, admin. Written by functions.
export interface FeeDoc extends Base {
  rideId: string;
  riderUid: string;
  hostId: string;
  amount: number;                        // config.limits.lateFeeEgp
  reason: "late" | "no_show";
  status: "unpaid" | "paid" | "disputed" | "waived";
  evidence: { checkedInAt: Timestamp | null; checkInDistanceM: number | null; reportedAt: Timestamp };
  dispute: { text: string; at: Timestamp } | null;
  resolvedBy: string | null;
}

// ratings/{rideId}_{fromUid}_{toUid}. Read: author, admin. Written by functions.
export interface RatingDoc {
  rideId: string; fromUid: string; toUid: string;
  roleRated: Role;
  stars: 1 | 2 | 3 | 4 | 5;
  tags: string[];                        // from RATING_TAGS
  createdAt: Timestamp;
}
export const RATING_TAGS = {
  host:  ["on_time", "safe_driving", "clean_car", "friendly", "followed_route", "late", "unsafe_driving", "rude"],
  rider: ["on_time", "polite", "easy_pickup", "late", "rude"],
} as const;

// packageOrders/{orderId}. Read: owner host, admin. Written by functions.
export interface PackageOrderDoc extends Base {
  hostId: string;
  packageId: string;
  trips: number;
  bucket: "regular" | "fixed";
  amountEgp: number;
  reference: string;                     // "HT-7K3QX9"
  status: "pending" | "confirmed" | "rejected";
  note: string | null;
  confirmedAt: Timestamp | null;
  confirmedBy: string | null;
}
```

### 6.7 Fixed Ride

```ts
// fixedRides/{fixedRideId}. Read: host, its riders, admin. Written by functions.
export interface FixedRideDoc extends Base {
  hostId: string;
  vehicleId: string;
  fromZone: string; fromPointId: string;
  toZone: string;   toPointId: string;
  direction: Direction;
  weekdays: number[];                    // 0 = Sunday … 6 = Saturday (Cairo)
  departTime: HHmm;
  seats: number;
  pricePerSeat: number;
  womenOnly: boolean;
  riderUids: string[];
  inviteCode: string;
  startDate: DateKey;
  status: "active" | "paused" | "ended";
}

// fixedRides/{fixedRideId}/skips/{dateKey}. Written by functions.
export interface FixedSkipDoc { riderUids: string[]; hostSkipped: boolean; updatedAt: Timestamp }
```

### 6.8 Safety, admin, system

```ts
// reports/{id}. Read: admin.
export interface ReportDoc extends Base {
  reporterUid: string | null;            // null for system reports (low rating)
  targetUid: string;
  rideId: string | null;
  category: "unsafe" | "harassment" | "no_show" | "payment" | "low_rating" | "other";
  text: string | null;
  status: "open" | "closed";
}

// safetyEvents/{id}. Read: admin.
export interface SafetyEventDoc { uid: string; rideId: string; type: "sos"; lat: number | null;
  lng: number | null; createdAt: Timestamp; handledBy: string | null }

// adminActions/{id}. Read: admin.
export interface AdminActionDoc { adminUid: string; action: string; targetUid: string | null;
  refId: string | null; note: string | null; createdAt: Timestamp }

// metrics/impact. Read: signed-in. metrics/{isoWeek}: admin.
export interface ImpactDoc { tripsCompleted: number; litersSaved: number; co2Kg: number;
  egpSaved: number; updatedAt: Timestamp }
export interface WeekMetricsDoc { week: string; cohortSize: number; retained: number;
  rate: number; createdAt: Timestamp }

// Functions only (no client access):
// codes/{code}               { type: "reference" | "invite" | "referral", ownerId: string, refId: string, createdAt }
// idempotency/{uid}_{reqId}  { result: unknown, expireAt: Timestamp }
// aiUsage/{uid}_{yyyymmdd}   { count: number, updatedAt }
// aiBudget/{yyyymm}          { estimatedEgp: number, updatedAt }
// config/admins              { uids: string[] /* dashboard accounts */, notifyUids: string[] /* app accounts that get admin alerts on the phone */ }
```

---

## 7. Config and seed data

All numbers live in `config/*` documents. Functions read them through `lib/config.ts` (60-second cache). Every config doc has `version` (integer); bump it on each change.

### 7.1 `config/pricing`

```json
{
  "version": 1,
  "fuelPriceEgpPerLiter": 22.25,
  "litersPer100Km": { "car": 8, "scooter": null },
  "runningCostEgpPerKm": { "car": 0.75, "scooter": null },
  "trafficFactor": { "offPeak": 1.0, "normal": 1.15, "peak": 1.3 },
  "peakWindows": [ { "from": "07:00", "to": "10:00" }, { "from": "16:00", "to": "19:00" } ],
  "normalWindows": [ { "from": "10:00", "to": "16:00" } ],
  "marginCap": 0.2,
  "maxSeats": { "car": 3, "scooter": 1 }
}
```

`scooter: null` means scooters are disabled `[DECISION-07]`. `normalWindows` is `[DECISION-06]`. Windows include `from` and exclude `to`. Every other time is `offPeak`.

### 7.2 `config/packages`

```json
{
  "version": 1,
  "items": [
    { "id": "p10",     "trips": 10, "priceEgp": 99,  "bucket": "regular" },
    { "id": "p20",     "trips": 20, "priceEgp": 179, "bucket": "regular" },
    { "id": "p30",     "trips": 30, "priceEgp": 249, "bucket": "regular" },
    { "id": "fixed40", "trips": 40, "priceEgp": 249, "bucket": "fixed" }
  ],
  "freeMonthDays": 30,
  "instapayAddress": "TODO-HUMAN",
  "maxPendingOrders": 3,
  "lowBalanceAlert": 2
}
```

### 7.3 `config/limits`

```json
{
  "version": 1,
  "minLeadMinutes": 30,
  "maxDaysAhead": 7,
  "rideGapMinutes": 90,
  "maxRidesPerHostPerDay": 2,
  "bookingCutoffMinutes": 15,
  "freeCancelMinutes": 60,
  "hostLateCancelMinutes": 60,
  "startWindowMinutes": 30,
  "lateGraceMinutes": 5,
  "lateFeeEgp": 10,
  "noShowMinutes": 15,
  "checkInRadiusM": 150,
  "hostLateMinutes": 10,
  "hostLateDistanceM": 300,
  "presenceIntervalSec": 60,
  "presenceMaxSamples": 200,
  "togetherMinSamples": 5,
  "togetherMaxGapSec": 180,
  "togetherMaxDistanceM": 300,
  "staleRideHours": 3,
  "expireNotStartedHours": 1,
  "ratingWindowDays": 7,
  "lowRatingThreshold": 2,
  "maxVehiclesPerHost": 2,
  "searchMaxResults": 20,
  "searchWindowMinutes": 60,
  "requestTtlMinutes": 30,
  "arrivedDistanceM": 300,
  "etaSpeedKmh": 25,
  "chatOpenHoursAfterRide": 24,
  "chatRetentionDays": 30,
  "notificationRetentionDays": 30,
  "maxSavedPlaces": 5,
  "aiDailyMessages": 20,
  "aiMonthlyBudgetEgp": 1000,
  "aiEgpPer1kInputTokens": 0,
  "aiEgpPer1kOutputTokens": 0,
  "fixedSkipCutoff": "20:00",
  "backupWindowMinutes": 45,
  "backupPriorityDelaySec": 120,
  "deleteBlockHours": 24,
  "kycPhotoRetentionDays": 30,
  "appCheckEnforced": false,
  "assistantEnabled": false,
  "fixedRideEnabled": false,
  "rewardsEnabled": false
}
```

`freeCancelMinutes` is `[DECISION-08]`, `noShowMinutes` is `[DECISION-09]`, `fixedSkipCutoff` is `[DECISION-13]`, `kycPhotoRetentionDays` is `[DECISION-14]`. Set the AI token prices once the provider is chosen `[DECISION-12]`. From v1.1: `requestTtlMinutes` is `[DECISION-21]`, `chatOpenHoursAfterRide` and `chatRetentionDays` are `[DECISION-23]`, `searchWindowMinutes` is `[DECISION-25]`, `arrivedDistanceM` and `etaSpeedKmh` are `[DECISION-26]`, `notificationRetentionDays` is `[DECISION-27]`.

### 7.4 `config/rewards`

```json
{
  "version": 1,
  "perTrip": 10,
  "onTimeWeek": 20,
  "fiveStar": 2,
  "fixedWeek": 15,
  "referral": 100,
  "referralTrips": 5,
  "tiers": { "silver": { "trips": 20, "rating": 4.5 }, "gold": { "trips": 40, "rating": 4.8 } },
  "tierWindowDays": 30,
  "hostMultiplier": { "bronze": 1.0, "silver": 1.1, "gold": 1.25 },
  "redeem": { "points": 300, "trips": 3 },
  "expiryMonths": 6,
  "maxPairTripsPerDay": 2
}
```

### 7.5 `config/impact`

```json
{ "version": 1, "shareOfSeatsReplacingSoloTrip": 0.3333, "co2KgPerLiter": 2.35 }
```

From the feasibility study: one seat in three replaces a solo car or ride-hailing trip; 2.35 kg CO₂ per liter of fuel. Liters use `config/pricing` (car consumption × traffic factor).

### 7.6 Seed data (`backend/seed/data/`)

| File | Content |
| --- | --- |
| `zones.json` | `fifth_settlement` (التجمع الخامس / Fifth Settlement), `rehab` (الرحاب / Rehab), `smart_village` (القرية الذكية / Smart Village), `zewail_city` (مدينة زويل / Zewail City, demo only) |
| points | 3–5 pickup points per zone (gates, known landmarks). Coordinates: `TODO-HUMAN` — take them from Google Maps. Never invent coordinates. |
| `routes.json` | `fifth_settlement__smart_village` 55 km (study), and the reverse. Other pairs: `TODO-HUMAN` measured distances. |
| `companies.json` | 5–10 companies in Smart Village and New Cairo: `TODO-HUMAN` |
| `users.json` (emulator only) | `rider@test` +201000000001 (man), `rider-w@test` +201000000002 (woman), `host@test` +201000000003 (approved host with a car), `host-w@test` +201000000004 (approved woman host), `admin@test` +201000000005 (admin claim), `host-new@test` +201000000006 (plain user). Code `123456` for all. For the dashboard: sign in through the Auth emulator's Google popup with `admin-web@test.com`, then run `npm run set-admin -- --email admin-web@test.com`. |
| config | the JSON in 7.1–7.5 |

`npm run seed` writes all of it to the emulator. `npm run seed -- --project dev` writes config, zones, routes and companies (never test users) to `hitchy-dev`.

---

## 8. Functions contract

Every function below is the exact spec. Inputs go in `schemas/inputs.ts` (zod), outputs in `schemas/io.ts`, and both are copied to `contract/functions.md` with fixtures.

### 8.1 Rules for every callable

- Region from `lib/app.ts` (`REGION`, see `[DECISION-01]`), `memory: "256MiB"`, `timeoutSeconds: 30`, `maxInstances: 10` (raise per function only with a reason).
- `enforceAppCheck: false` in the options. Instead, `buildCtx` refuses calls without a valid App Check token (`request.app` undefined) when `config/limits.appCheckEnforced` is true. This lets you switch enforcement without a deploy.
- Common checks, in this order, done by `makeCallable` before the handler runs:
  1. Signed in → else `UNAUTHENTICATED`.
  2. App Check (when enforced) → else `UNAUTHENTICATED`.
  3. Admin functions: `request.auth.token.admin === true` → else `PERMISSION_DENIED`.
  4. Profile required (all except `completeProfile` and admin functions): `users/{uid}` exists → else `PROFILE_REQUIRED`. Admin dashboard accounts have no profile (ADR 0003).
  5. Not suspended (all except read-only, safety, account and admin functions) → else `SUSPENDED`.
  6. Input parses with zod → else `INVALID_INPUT` (with the failing field paths in `extra`). Input objects are strict (an unknown field fails), and text is trimmed before its length is checked (ADR 0003).
- Instants in inputs and outputs are ISO-8601 UTC strings.
- Each handler exports a pure-ish core for tests: `export async function requestSeatsCore(input, ctx)`. The callable is one line: `export const requestSeats = makeCallable("requestSeats", RequestSeatsInput, requestSeatsCore)`.
- Notifications are sent after the transaction commits, never inside it.

```ts
// lib/callable.ts — shape to follow
export function makeCallable<S extends z.ZodTypeAny, O>(
  name: string,
  schema: S,
  core: (input: z.infer<S>, ctx: CallCtx) => Promise<O>,
  opts: { admin?: boolean; profile?: boolean; allowSuspended?: boolean } = {},
) {
  return onCall({ region: REGION, memory: "256MiB", timeoutSeconds: 30, maxInstances: 10 },
    async (req) => {
      const ctx = await buildCtx(req, { admin: opts.admin ?? false, profile: opts.profile ?? true,
                                        allowSuspended: opts.allowSuspended ?? false });
      const parsed = schema.safeParse(req.data);
      if (!parsed.success) throw toHttps(new AppFailure("INVALID_INPUT", parsed.error.issues.map(i => i.path.join("."))));
      try {
        const out = await core(parsed.data, ctx);
        logger.info(`${name}.ok`, { uid: ctx.uid });
        return out;
      } catch (e) {
        if (e instanceof AppFailure) { logger.warn(`${name}.fail`, { uid: ctx.uid, reason: e.reason }); throw toHttps(e); }
        logger.error(`${name}.error`, { uid: ctx.uid, error: String(e) });
        throw toHttps(new AppFailure("INTERNAL"));
      }
    });
}

// lib/ctx.ts
export interface CallCtx { uid: string; phone: string | null; isAdmin: boolean; user: UserDoc | null; now: Date }
```

### 8.2 Shared output types

```ts
// schemas/io.ts
export interface RideCard {
  rideId: string;
  departAt: string;                        // ISO UTC
  arriveAt: string;                        // departAt + route usualMinutes (v1.1)
  fromZone: string; toZone: string;
  fromPointId: string; toPointId: string;
  fromPointName: LocalizedName; toPointName: LocalizedName;
  pricePerSeat: number;
  fairPrice: boolean;                      // pricePerSeat <= priceRange.recommended
  seatsLeft: number;
  walkM: number | null;                    // rider's chosen point → this pickup point (v1.1)
  bestMatch: boolean;                      // first card under "recommended" sort (v1.1)
  womenOnly: boolean;
  vehicleType: VehicleType;
  fixed: boolean;                          // part of a Fixed Ride
  host: {
    uid: string; firstName: string; photoPath: string | null;
    rating: number | null; ratingCount: number;
    trips: number;                         // profiles.hostTrips (v1.1)
    verified: boolean; tier: Tier;
    sameCompany: boolean;                  // never the company name
  };
}

export interface RideView extends RideCard {  // only for the host and riders with an accepted booking
  status: RideStatus;
  fromPoint: GeoPointLite;
  vehicle: { make: string; model: string; color: string; plate: string };
}

export interface QuoteResult {
  min: number; recommended: number; max: number;
  trafficLevel: TrafficLevel; distanceKm: number;
  fuelCostEgp: number; tripCostEgp: number; configVersion: number;
}
```

### 8.3 Profile and roles (Phase 4)

#### `completeProfile` · callable · any signed-in user (profile not required)

```ts
input:  { firstName: string /*1–40*/; lastName: string /*1–40*/; gender: Gender;
          companyId: string | null; companyOther: string | null /*≤60*/;
          workHours: { start: HHmm; end: HHmm } | null; language: Language;
          referralCode: string | null }
output: { activeRole: Role; hostStatus: HostStatus; referralApplied: boolean }
```

1. `phone = request.auth.token.phone_number` → missing: `PHONE_REQUIRED`.
2. `companyId` given → `companies/{id}` exists and is active → else `INVALID_INPUT`.
3. `users/{uid}` exists → if `gender` differs: `GENDER_LOCKED`; otherwise update the owner-writable fields and stop.
4. New user → create `UserDoc` with defaults: `activeRole: "rider"`, `host.status: "none"`, credits `{regular: 0, fixed: 0}`, tiers `bronze`, counters 0, `points.balance: 0`, `blockedUids: []`, `suspended: false`, a new unique `referralCode`. `referredBy` = owner of `referralCode` when it exists and is not the caller (otherwise `null`, `referralApplied: false`; never an error).
5. Create `kyc/{uid}` with `status: "none"` and every doc `missing` with its path.

#### `switchRole` · callable

```ts
input:  { role: Role }
output: { activeRole: Role }
```

`role == "host"` requires `host.status == "approved"` → else `HOST_NOT_APPROVED`. Writes `users.activeRole`.

#### `syncPublicProfile` · trigger · `users/{uid}` written

Deleted → delete `profiles/{uid}`. Otherwise build `ProfileDoc` (`avg = count ? round(sum / count, 1) : null`). Skip the write when nothing changed.

### 8.4 Host verification and admin (Phase 5)

#### `addVehicle` · callable

```ts
input:  { type: VehicleType; make: string /*1–30*/; model: string /*1–30*/;
          year: number /*1990 … this year + 1*/; color: string /*1–20*/; plate: string /*1–15*/ }
output: { vehicleId: string }
```

1. Vehicles of the caller < `maxVehiclesPerHost` → else `VEHICLE_LIMIT`.
2. `type == "scooter"` and `config/pricing.litersPer100Km.scooter == null` → `SCOOTER_DISABLED`.
3. Create the vehicle: `status: "pending"`, `maxSeats = config/pricing.maxSeats[type]`.

#### `submitHostApplication` · callable

```ts
input:  { vehicleId: string }
output: { status: "submitted" }
```

1. `kyc.status` is `none` or `rejected` → else `KYC_ALREADY_SUBMITTED`.
2. The vehicle exists under the caller → else `NOT_FOUND`.
3. Storage has `id_front.jpg`, `id_back.jpg`, `driving_license.jpg`, `vehicle_license.jpg` under `kyc/{uid}/` → else `KYC_MISSING_FILES` with `extra: KycDocKey[]`. After a rejection, each rejected file must be newer than `kyc.reviewedAt`.
4. Write `kyc`: `status: "submitted"`, `vehicleId`, `submittedAt`, present docs `uploaded` (including `criminalRecord` when present). Write `users.host.status: "pending"`.
5. After commit: `sendToAdmins("new_host_application")`.

#### `approveHost` · callable · admin

```ts
input:  { uid: string; vehicleId: string }
output: { freeUntil: string }
```

1. `kyc.status == "submitted"` → else `KYC_NOT_SUBMITTED`.
2. One transaction: `kyc.status: "approved"`, required docs `accepted`, `reviewedAt`, `reviewerUid`; vehicle `approved`; `host.status: "approved"`, `approvedAt: now`, `freeUntil: now + freeMonthDays`; `adminActions` entry.
3. After commit: notify the user `host_approved`.

#### `rejectHost` · callable · admin

```ts
input:  { uid: string;
          rejections: Array<{ doc: KycDocKey; reason: "blurry" | "expired" | "mismatch" | "missing" | "other" }> /*≥1*/;
          note: string | null }
output: {}
```

`kyc.status == "submitted"` → else `KYC_NOT_SUBMITTED`. Listed docs → `rejected` with reason, other uploaded docs → `accepted`, `kyc.status: "rejected"`, `host.status: "rejected"`, audit, notify `host_rejected`.

#### `approveVehicle` · callable · admin

```ts
input:  { uid: string; vehicleId: string; approve: boolean }
output: {}
```

For a host's second vehicle. Sets `approved` or `rejected`, audit, notify `vehicle_reviewed`.

#### `setVerified` · callable · admin

```ts
input:  { uid: string; verified: boolean }
output: {}
```

`verified: true` requires `kyc/{uid}/criminal_record.jpg` in Storage → else `KYC_MISSING_FILES`. Writes `host.verified` and `kyc.docs.criminalRecord.status: "accepted"`, audit, notify `verified_granted` (only when true).

#### `setGender` · callable · admin

```ts
input:  { uid: string; gender: Gender }
output: {}
```

The only way to change gender. Audit.

#### `updateConfig` · callable · admin

```ts
input:  { name: "pricing" | "packages" | "limits" | "rewards" | "impact"; value: object }
output: { version: number }
```

`value` must pass that config's full zod schema in `schemas/config.ts` → else `INVALID_INPUT`. Writes it with `version = old version + 1` and an `adminActions` entry holding the old and new values. Functions pick it up within 60 seconds (config cache). This is how the fuel price and other numbers change, never by hand in the console.

### 8.5 Rides and pricing (Phase 6)

#### Pricing formula

```ts
// domain/pricing.ts — no Firebase imports
export const floorEgp = (x: number) => Math.floor(Math.round(x * 100) / 100);

export function trafficLevelAt(minuteOfDayCairo: number, c: PricingConfig): TrafficLevel {
  // windows include "from" and exclude "to"; anything not peak or normal is offPeak
}

export function quote(p: { distanceKm: number; vehicleType: VehicleType; trafficLevel: TrafficLevel; seats: number },
                      c: PricingConfig) {
  const litersPer100 = c.litersPer100Km[p.vehicleType]!;          // caller checks null first
  const fuelCost = p.distanceKm * (litersPer100 / 100) * c.trafficFactor[p.trafficLevel] * c.fuelPriceEgpPerLiter;
  const tripCost = fuelCost + p.distanceKm * c.runningCostEgpPerKm[p.vehicleType]!;
  return {
    min: floorEgp(fuelCost / p.seats),
    recommended: floorEgp(tripCost / p.seats),
    max: floorEgp((tripCost * (1 + c.marginCap)) / p.seats),
    fuelCostEgp: floorEgp(fuelCost),
    tripCostEgp: floorEgp(tripCost),
  };
}
```

Golden test (55 km, car, 8 L/100 km, 22.25 EGP/L, 0.75 EGP/km). Each cell is min / recommended / max in EGP and matches the feasibility study:

| Traffic level | 1 seat | 2 seats | 3 seats |
| --- | --- | --- | --- |
| offPeak (1.00) | 97 / 139 / 166 | 48 / 69 / 83 | 32 / 46 / 55 |
| normal (1.15) | 112 / 153 / 184 | 56 / 76 / 92 | 37 / 51 / 61 |
| peak (1.30) | 127 / 168 / 202 | 63 / 84 / 101 | 42 / 56 / 67 |

#### `quotePrice` · callable · host

```ts
input:  { vehicleId: string; fromZone: string; toZone: string; departAt: string; seats: number /*1–3*/ }
output: QuoteResult
```

1. `host.status == "approved"` → else `HOST_NOT_APPROVED`.
2. Vehicle exists under the caller → else `NOT_FOUND`; scooter disabled → `SCOOTER_DISABLED`.
3. `routes/{fromZone}__{toZone}` exists → else `ROUTE_NOT_SUPPORTED`.
4. `seats <= vehicle.maxSeats` → else `SEATS_INVALID`.
5. Traffic level from the Cairo time of `departAt`; return `quote(...)` plus `distanceKm`, `trafficLevel`, `configVersion`.

#### `createRide` · callable · host

```ts
input:  { vehicleId: string; fromZone: string; fromPointId: string; toZone: string; toPointId: string;
          direction: Direction; departAt: string; seats: number; pricePerSeat: number;
          womenOnly: boolean; requestId: string /*uuid*/ }
output: { rideId: string }
```

Checks in this order, inside one transaction (idempotent by `requestId`):

1. `host.status == "approved"` → else `HOST_NOT_APPROVED`.
2. Vehicle under the caller with `status == "approved"` → else `VEHICLE_NOT_APPROVED`.
3. Both points exist and are active; the route exists → else `ROUTE_NOT_SUPPORTED`.
4. `departAt >= now + minLeadMinutes` → else `DEPART_TOO_SOON`; `departAt <= now + maxDaysAhead days` → else `DEPART_TOO_FAR`.
5. `1 <= seats <= vehicle.maxSeats` → else `SEATS_INVALID`.
6. Server quote: `min <= pricePerSeat <= max` → else `PRICE_OUT_OF_RANGE`.
7. `womenOnly` → caller `gender == "female"` → else `WOMEN_ONLY_HOST`.
8. Host rides with the same `dateKey` and status `open | full | started | finished`: fewer than `maxRidesPerHostPerDay` and none with the same `direction` → else `DAILY_RIDE_LIMIT`.
9. No host ride with status `open | full | started` within `rideGapMinutes` of `departAt` → else `RIDE_TIME_CONFLICT`.
10. `now < host.freeUntil`, or `credits.regular >= (host rides open | full | started without fixedRideId) + 1` → else `NO_CREDITS`.

Writes the `RideDoc`: vehicle copy, point names and pickup coordinates, `hostCompanyId = user.companyId`, `priceRange` with `configVersion`, `dateKey`, `status: "open"`, `seatsLeft = seats`, `bookingsCount: 0`, flags `false`.

#### `cancelRide` · callable · host

```ts
input:  { rideId: string; reason: "sick" | "car_problem" | "plans_changed" | "other" }
output: { cancelledBookings: number }
```

1. `ride.hostId == uid` → else `NOT_FOUND`. Status `open | full` → `started`: `RIDE_HAS_STARTED`; anything else: `RIDE_NOT_OPEN`.
2. One transaction: ride `cancelled` with `cancelledAt`, `cancelReason`; every `requested` or `booked` booking → `cancelled_by_host` with `cancelReason: "ride_cancelled"`; when `bookingsCount > 0` and `now >= departAt - hostLateCancelMinutes`: `host.lateCancels += 1`.
3. Notifications come from `onBookingChanged`. Phase 12 adds the backup flow for each affected rider.

### 8.6 Search and booking (Phase 7)

#### `searchRides` · callable · rider

```ts
input:  { fromZone: string; toZone: string; fromPointId: string | null; date: DateKey; time: HHmm;
          seats: number /*1–3*/; sort: "recommended" | "earliest" | "cheapest";
          timeBand: "any" | "before" | "first_hour" | "after"; maxPrice: number | null;
          minRating: 4.5 | 4.8 | null; verifiedOnly: boolean; womenOnly: boolean }
output: { rides: RideCard[] }
```

All logic sits in `searchRidesCore(callerUid, input)`; the assistant and the backup flow reuse it. The rider picks fixed points; the app sends their zones, the pickup point (for `walkM`) and one time (v1.1, 2.1 #5 and #6).

1. Query `rides` where `fromZone ==`, `toZone ==`, `status == "open"`, `departAt` between `time − searchWindowMinutes` and `time + searchWindowMinutes` (Cairo) `[DECISION-25]`, ordered by `departAt`, limit 100.
2. Drop: the caller's own rides; hosts in `caller.blockedUids` or who blocked the caller; suspended hosts; Women-only rides when the caller is not a woman; non-Women-only rides when `input.womenOnly`; non-Verified hosts when `input.verifiedOnly`; rides with `seatsLeft < seats`; rides outside `timeBand` (`before` = earlier than `time`, `first_hour` = `time` up to `time + 60 min`, `after` = later); `pricePerSeat > maxPrice`; hosts below `minRating` or with fewer than 3 ratings when `minRating` is set.
3. Build `RideCard`s from `profiles`: `sameCompany = caller.companyId != null && caller.companyId == ride.hostCompanyId`; `walkM` = distance from `fromPointId` to the ride's pickup point (`null` without `fromPointId`); `arriveAt = departAt + route.usualMinutes`; `host.trips = hostTrips`.
4. Sort: `recommended` ranks with `domain/ranking.ts`; `earliest` by `departAt`; `cheapest` by `pricePerSeat`, then `departAt`. Return the top `searchMaxResults`; `bestMatch` is true only for the first card under `recommended`.

Ranking (starting weights; keep them as constants so they can be tuned):

```text
score = 40 × timeFit + 20 × sameCompany + 15 × ratingScore + 10 × verified + tierBonus + 5 × reliability
timeFit     = max(0, 1 − |departAt − the picked time| / 120 minutes)
ratingScore = ratingCount < 3 ? 0.5 : clamp((avg − 3) / 2, 0, 1)
tierBonus   = bronze 0, silver 5, gold 10
reliability = 1 − min(1, (host.lateCancels + host.hostLateCount) / max(5, host.tripsCompleted))
ties        → earlier departAt first
```

#### `requestSeats` · callable · rider (v1.1; replaces `bookSeat`)

```ts
input:  { rideId: string; seats: number /*1–3*/; requestId: string /*uuid*/ }
output: { booking: { rideId: string; status: "requested"; seats: number; price: number; total: number;
                     expiresAt: string }; ride: RideCard }
```

Checks in this order, inside one transaction (idempotent by `requestId`):

1. Ride exists → else `NOT_FOUND`; `status == "open"` → else `RIDE_NOT_OPEN`.
2. `departAt - now > bookingCutoffMinutes` → else `TOO_LATE_TO_BOOK`.
3. `ride.hostId != uid` → else `OWN_RIDE`.
4. Blocked either way → `RIDE_NOT_OPEN` (do not reveal the block).
5. `seatsLeft >= seats` → else `RIDE_FULL`.
6. No booking doc, or the existing one is `declined`, `expired` or cancelled → else `ALREADY_BOOKED`.
7. `user.rider.unpaidFees == 0` → else `UNPAID_FEE`.
8. `!ride.womenOnly || user.gender == "female"` → else `WOMEN_ONLY`. With `seats > 1` on a Women-only ride the app first asks the rider to confirm every passenger is a woman `[DECISION-24]`.
9. No other `requested | booked | boarded` booking of the caller within `rideGapMinutes` (collection group query) → else `TIME_CONFLICT`.

Writes: booking `{ status: "requested", seats, price: ride.pricePerSeat, total: price × seats, expiresAt, respondedAt: null, paymentConfirmedAt: null, departAt, hostId, riderUid, … }` with `expiresAt = min(now + requestTtlMinutes, departAt − bookingCutoffMinutes)` `[DECISION-21]`; `seatsLeft -= seats` (`status: "full"` at 0). The seats stay held until the host answers or the request expires.
After commit: enqueue `expireRequest` with task id `expire-{rideId}-{riderUid}-{requestedAt ms}` at `expiresAt`. The host's notification comes from `onBookingChanged`.

#### `respondToRequest` · callable · host

```ts
input:  { rideId: string; riderUid: string; accept: boolean }
output: { status: "booked" | "declined" }
```

1. `ride.hostId == uid` → else `NOT_FOUND`; ride `open | full` → else `RIDE_NOT_OPEN`.
2. Booking `status == "requested"` → else `REQUEST_NOT_PENDING`; `now < expiresAt` → else `REQUEST_EXPIRED`.
3. One transaction. Accept: booking `booked`, `respondedAt`; `bookingsCount += 1`. Decline: booking `declined`, `respondedAt`; `seatsLeft += seats`, `full → open`.
4. After commit, on accept: when `bookingsCount` became 1, enqueue `rideReminder` with task id `reminder-{rideId}` at `departAt - 30 minutes` (ignore "already exists"). Notifications come from `onBookingChanged`.

#### `expireRequest` · task queue

`{ rideId, riderUid }` → when the booking is still `requested` and `now >= expiresAt`: booking `expired`, `seatsLeft += seats`, `full → open` (one transaction). `closeStaleRides` also expires missed requests.

#### `cancelBooking` · callable · rider

```ts
input:  { rideId: string; reason: "host_late" | "plans_changed" | "found_another_ride" | "other" }
output: { lateCancel: boolean }
```

1. Booking with `status == "requested"` or `"booked"` → else `BOOKING_NOT_FOUND`. Cancelling a `requested` booking is always free and skips steps 2 and 3.
2. `reason == "host_late"`: `now >= departAt + hostLateMinutes` and the host's `presence.last` is missing or farther than `hostLateDistanceM` from the pickup point → else `HOST_NOT_LATE`.
3. Any other reason: ride status `open | full` → else `RIDE_HAS_STARTED`.
4. One transaction: booking `cancelled_by_rider` with `cancelReason`; `lateCancel = status was "booked" && reason != "host_late" && now >= departAt - freeCancelMinutes` `[DECISION-08]`; `seatsLeft += seats`, `full → open`; `bookingsCount -= 1` when it was `booked`; `host_late` → `host.hostLateCount += 1`; `lateCancel` → `rider.lateCancels += 1`.
5. Phase 12: `host_late` starts the backup flow for this rider.

#### `onBookingChanged` · trigger · `rides/{rideId}/bookings/{riderUid}` written

The only place that sends booking notifications:

| Change | Notify |
| --- | --- |
| created as `requested` | host: `booking_requested` |
| `requested → booked` | rider: `booking_accepted` |
| `requested → declined` | rider: `booking_declined` |
| `requested → expired` | rider: `request_expired` |
| `requested → cancelled_by_rider` or `booked → cancelled_by_rider` | host: `booking_cancelled` |
| `requested → cancelled_by_host` or `booked → cancelled_by_host` | rider: `ride_cancelled` |

#### `rideReminder` · task queue

`{ rideId }` → when the ride is `open | full` and `bookingsCount > 0`: host and booked riders get `ride_reminder`.


### 8.7 Trip day (Phase 8)

#### `startRide` · callable · host

```ts
input:  { rideId: string }
output: {}
```

`ride.hostId == uid` → else `NOT_FOUND`; status `open | full` → else `RIDE_NOT_OPEN`; `|now - departAt| <= startWindowMinutes` → else `START_WINDOW_CLOSED`. Writes `status: "started"`, `startedAt`. After commit: booked riders get `ride_started`.

#### `checkIn` · callable · rider ("I'm at the pickup point")

```ts
input:  { rideId: string; lat: number; lng: number; accuracyM: number }
output: { distanceM: number }
```

Booking `booked` → else `BOOKING_NOT_FOUND`; ride `open | full | started` → else `RIDE_NOT_OPEN`; `now <= departAt + noShowMinutes` → else `CHECK_IN_CLOSED`. Writes `checkedInAt` (server time) and `checkInDistanceM` (haversine to `ride.fromPoint`). After commit: host gets `rider_at_pickup`. Server time is the evidence used by `reportLate`.

#### `markBoarded` · callable · host or rider

```ts
input:  { rideId: string; riderUid: string }
output: {}
```

Caller is the host, or the caller is `riderUid` → else `NOT_ON_RIDE`; ride `started` → else `RIDE_NOT_STARTED`; booking `booked` → else `BOOKING_NOT_FOUND`. Writes `status: "boarded"`, `boardedAt`. All seats of a booking board together.

#### `reportLate` · callable · host

```ts
input:  { rideId: string; riderUid: string; kind: "late" | "no_show" }
output: { feeId: string }
```

1. `ride.hostId == uid` → else `NOT_FOUND`; booking `booked | boarded` → else `BOOKING_NOT_FOUND`; `booking.lateFeeId == null` → else `FEE_EXISTS`.
2. Rider on time: `checkedInAt <= departAt + lateGraceMinutes` and `checkInDistanceM <= checkInRadiusM` → `RIDER_WAS_ON_TIME`.
3. `late`: `now >= departAt + lateGraceMinutes` → else `GRACE_NOT_OVER`.
4. `no_show` `[DECISION-09]`: booking still `booked` and `now >= departAt + noShowMinutes` → else `GRACE_NOT_OVER`; booking → `no_show`.
5. One transaction: create `FeeDoc` (`amount: lateFeeEgp`, `reason: kind`, `status: "unpaid"`, evidence); `booking.lateFeeId`; `rider.unpaidFees += 1`. One fee per booking, whatever its seats `[DECISION-24]`.
6. After commit: rider gets `fee_created`.

#### `disputeFee` · callable · rider

```ts
input:  { feeId: string; text: string /*1–300*/ }
output: {}
```

`fee.riderUid == uid` → else `FEE_NOT_FOUND`; `status == "unpaid"` → else `FEE_NOT_DISPUTABLE`. Writes `status: "disputed"`, `dispute`. After commit: `sendToAdmins("fee_disputed")`. A disputed fee still blocks booking until it is resolved.

#### `resolveFee` · callable · admin

```ts
input:  { feeId: string; decision: "keep" | "waive"; note: string | null }
output: {}
```

`status == "disputed"` → else `FEE_NOT_DISPUTABLE`. `keep` → `unpaid`; `waive` → `waived` and `rider.unpaidFees -= 1`. `resolvedBy`, audit, rider and host get `fee_resolved`.

#### `markFeePaid` · callable · host

```ts
input:  { feeId: string }
output: {}
```

`fee.hostId == uid` and status `unpaid | disputed` → else `FEE_NOT_FOUND`. Writes `paid`, `rider.unpaidFees -= 1`. Rider gets `fee_paid`.

#### `confirmSeatPayment` · callable · host (v1.1, R22)

```ts
input:  { rideId: string; riderUid: string }
output: {}
```

`ride.hostId == uid` → else `NOT_FOUND`; booking `boarded | completed` → else `BOOKING_NOT_FOUND`; `paymentConfirmedAt == null` → else `PAYMENT_ALREADY_CONFIRMED`. Writes `paymentConfirmedAt`. After commit: the rider gets `payment_confirmed`, which opens the receipt. A record only: no money passes through Hitchy, and an unconfirmed payment blocks nothing.

#### `sendChatMessage` · callable · host or rider (v1.1; replaces `sendQuickMessage`)

```ts
input:  { rideId: string; riderUid: string; text: string /*1–500*/; requestId: string /*uuid*/ }
output: { messageId: string }
```

The caller is the ride's host or `riderUid` → else `NOT_ON_RIDE`. The rider's booking is `booked | boarded | completed`, the ride is not `cancelled | expired`, and `now <= (finishedAt ?? departAt) + chatOpenHoursAfterRide` → else `CHAT_CLOSED` `[DECISION-23]`. Writes `rides/{rideId}/chats/{riderUid}/messages/{id}` with `expireAt = departAt + chatRetentionDays` (idempotent by `requestId`). After commit: the other person gets `chat_message`, which says "New message from {name}" and never carries the text. The chat screen offers the old quick messages ("I'm at the pickup point", "Running 5 minutes late", …) as one-tap chips that send normal messages. Never log the text.

#### `getContactPhone` · callable · host or rider (v1.1, R18)

```ts
input:  { rideId: string; otherUid: string }
output: { phone: string }                 // E.164
```

The caller and `otherUid` are the ride's host and a rider whose booking is `booked | boarded`, and the ride is `open | full | started` → else `CONTACT_NOT_ALLOWED` `[DECISION-22]`. Returns the other person's `users.phone`; the app opens the dialer (`ACTION_DIAL`, no call permission). Log only `{ uid, rideId }`.

#### Presence (direct client writes)

While the ride is `started`, the host app and each booked rider app write `rides/{rideId}/presence/{uid}` every `presenceIntervalSec`: replace `last`, append to `samples` (keep the newest `presenceMaxSamples`). Rules in section 9 enforce who and when.

#### `finishRide` · callable · host

```ts
input:  { rideId: string }
output: { completedBookings: number; creditUsed: boolean }
```

1. `ride.hostId == uid` → else `NOT_FOUND`; `status == "started"` → else `RIDE_NOT_STARTED`.
2. One transaction (`finishRideCore`, also used by `closeStaleRides`):
   - ride `finished`, `finishedAt`;
   - `boarded` bookings → `completed`; `booked` bookings → `no_show` (no fee);
   - when at least one booking completed, `now >= host.freeUntil` and `!creditConsumed`: bucket = `fixedRideId ? "fixed" : "regular"`, `credits[bucket] -= 1` (may go below 0; `createRide` then blocks), ledger entry `{ delta: -1, reason: "trip", refId: rideId }`, `creditConsumed: true`.
3. Everything else runs in `onRideFinished`.

#### `onRideFinished` · trigger · `rides/{rideId}` updated to `finished`

Skip when `postProcessed` is true. Each step is safe to repeat:

1. For each `completed` booking, compute `verifiedTogether` with `domain/together.ts` from both presence docs: count rider samples that have a host sample within `togetherMaxGapSec` and `togetherMaxDistanceM`; true when the count ≥ `togetherMinSamples`.
2. Counters: `host.tripsCompleted += 1` when any booking completed; `rider.tripsCompleted += 1` per completed booking.
3. Rewards (Phase 10, when `rewardsEnabled`), see 8.9.
4. Referral check (Phase 10), see 8.9.
5. Impact (Phase 14): per completed booking, add to `metrics/impact` with `FieldValue.increment`, see 8.13.
6. Notifications: completed riders `rate_trip`; host `rate_riders`; host `low_balance` when `credits.regular == lowBalanceAlert`.
7. Set `postProcessed: true` in a transaction that checks it was false.

#### `closeStaleRides` · scheduled · every 30 minutes

1. `started` rides with `departAt <= now - staleRideHours` → `finishRideCore` with `autoFinished: true`.
2. `open | full` rides with `departAt <= now - expireNotStartedHours` → `expired`; `booked` bookings → `cancelled_by_host` with `cancelReason: "not_started"`; `requested` bookings → `expired`; when `bookingsCount > 0`: `host.lateCancels += 1`.
3. `requested` bookings past `expiresAt` (collection group) → `expired`, seats back. This catches missed `expireRequest` tasks.

#### `submitRating` · callable

```ts
input:  { rideId: string; toUid: string; stars: 1 | 2 | 3 | 4 | 5; tags: string[] /*≤5*/ }
output: {}
```

1. Ride `finished` → else `RATING_NOT_ALLOWED`.
2. Allowed pairs: a rider with a `completed` booking rates the host; the host rates each rider with a `completed` booking → else `NOT_ON_RIDE`.
3. `now <= finishedAt + ratingWindowDays` → else `RATING_WINDOW_CLOSED`.
4. Rating doc does not exist → else `RATING_EXISTS`. Tags must come from `RATING_TAGS[roleRated]` → else `INVALID_INPUT`.
5. One transaction: create the rating; `ratee[roleRated].ratingSum += stars`, `ratingCount += 1`.
6. `stars <= lowRatingThreshold` → `reports` doc (`category: "low_rating"`, `reporterUid: null`). `stars == 5` → five-star points (8.9).

### 8.8 Packages and credits (Phase 9)

#### `requestPackage` · callable · host

```ts
input:  { packageId: string }
output: { orderId: string; reference: string; amountEgp: number; instapayAddress: string }
```

`host.status == "approved"` → else `HOST_NOT_APPROVED`; package exists → else `PACKAGE_NOT_FOUND`; pending orders of the caller < `maxPendingOrders` → else `TOO_MANY_PENDING`. Creates the order with a unique reference `HT-XXXXXX`, `status: "pending"`.

#### `confirmPackageOrder` · callable · admin

```ts
input:  { orderId: string; received: boolean; note: string | null }
output: {}
```

`status == "pending"` → else `ORDER_NOT_PENDING`. One transaction: `received` → `confirmed`, `credits[bucket] += trips`, ledger `{ delta: +trips, reason: "package", refId: orderId }`; otherwise `rejected`. Audit. Host gets `package_confirmed` or `package_rejected`.

#### `notifyFreeMonthEnding` · scheduled · daily 10:00

Hosts with `host.freeEndingNotified == false` and `host.freeUntil` within the next 3 days → `free_month_ending`, set the flag.

### 8.9 Hitchy Rewards (Phase 10)

All of this runs only when `config/limits.rewardsEnabled` is true. `repos/points.ts` exposes `addPoints(uid, amount, reason, refId)`: one transaction creating `{ amount, remaining: amount, open: true, expiresAt: now + expiryMonths }` and `points.balance += amount`. (Add `open: boolean` to `PointsEntry`; it is `remaining > 0`.)

- **Trip points** (in `onRideFinished`): for each completed booking with `verifiedTogether` where the same host and rider have fewer than `maxPairTripsPerDay` rewarded bookings that day → rider `+perTrip`, `booking.rewarded: true`. The host gets `round(perTrip × hostMultiplier[host.tier])` once per ride when at least one booking qualified.
- **Five-star** (in `submitRating`): `+fiveStar` to the person rated.
- **Referral** (in `onRideFinished`): when a participant's `rider.tripsCompleted + host.tripsCompleted` reaches `referralTrips`, `referredBy != null` and `!referralRewarded` → `+referral` to both, set `referralRewarded` (transaction).

#### `weeklyRewards` · scheduled · Thursday 23:00

The week runs from Sunday 00:00 to now (Cairo). `refId = week key` ("2026-W45"); skip users who already have an entry with that `refId` and reason.

- Rider: at least one `completed` booking this week and no fee created this week → `+onTimeWeek`.
- Host: at least one finished ride with a completed booking this week, no late `cancelRide` and no `host_late` cancel this week → `+onTimeWeek`.
- Fixed week: for each active Fixed Ride, if every scheduled day this week has a finished ride and every non-skipped fixed rider's booking is `completed` with `verifiedTogether` → `+fixedWeek` to the host and those riders.

#### `computeTiers` · scheduled · daily 02:00

Per user and role: trips in the last `tierWindowDays` (rider: completed bookings; host: finished rides with at least one completed booking) and the role's all-time average rating. Gold: trips ≥ 40 and rating ≥ 4.8. Silver: trips ≥ 20 and rating ≥ 4.5. Else bronze. Write `rider.tier` / `host.tier` only when changed.

#### `expirePoints` · scheduled · daily 03:00

Collection group `points` where `open == true` and `expiresAt <= now`. Per entry, one transaction: add `{ amount: -remaining, reason: "expire", refId: entryId }`, set `remaining: 0, open: false`, `balance -= remaining`.

#### `redeemPoints` · callable · host

```ts
input:  { bundles: number /*1–10*/ }
output: { creditsAdded: number; pointsBalance: number }
```

`host.status == "approved"` → else `HOST_NOT_APPROVED`; `balance >= bundles × redeem.points` → else `NOT_ENOUGH_POINTS`. One transaction: spend from the oldest open entries first (by `expiresAt`), add `{ amount: -cost, reason: "redeem" }`, `balance -= cost`, `credits.regular += bundles × redeem.trips`, ledger `{ reason: "reward" }`.

### 8.10 AI assistant (Phase 11)

#### `askAssistant` · callable · rider

```ts
input:  { message: string /*1–500*/;
          history: Array<{ role: "user" | "assistant"; text: string /*≤1000*/ }> /*≤6*/;
          language: Language }
output: { reply: string; rides: RideCard[] /*≤3*/; usedSearch: boolean; remainingToday: number }
```

1. `assistantEnabled` → else `AI_UNAVAILABLE`.
2. `aiUsage/{uid}_{yyyymmdd}.count < aiDailyMessages` → else `AI_DAILY_LIMIT`.
3. `aiBudget/{yyyymm}.estimatedEgp < aiMonthlyBudgetEgp` → else `AI_UNAVAILABLE`.
4. `llm.chat(...)` with the system prompt below, the history and the message, tool `search_rides`, at most 2 tool calls, 20 s timeout. Any provider error or timeout → `AI_UNAVAILABLE`.
5. The tool validates its arguments with the `searchRides` zod schema and runs `searchRidesCore(uid, args)` as the caller, so Women-only, blocks and Same company still apply. Missing fields take the defaults: `seats: 1`, `sort: "recommended"`, `timeBand: "any"`, no price or rating limit, `fromPointId: null`. It returns at most 5 cards to the model.
6. `rides` in the output = the top 3 cards from the last tool result (never IDs written by the model).
7. After the call: `aiUsage.count += 1`; `aiBudget.estimatedEgp += tokens × config prices`.
8. Log only token counts, tool call count and result count. Never the message text.

```ts
// ai/llmClient.ts
export interface LlmClient {
  chat(req: {
    system: string;
    messages: Array<{ role: "user" | "assistant"; text: string }>;
    tools: ToolDef[];
    maxToolCalls: number;
    timeoutMs: number;
    runTool: (name: string, args: unknown) => Promise<unknown>;
  }): Promise<{ text: string; inputTokens: number; outputTokens: number; toolCalls: number }>;
}
// ai/providers/<provider>.ts implements it [DECISION-12]; test/helpers/fakeLlm.ts for tests.
```

Tool definition:

```json
{
  "name": "search_rides",
  "description": "Find open Hitchy rides between two zones in a time window on one day. Returns ride cards ranked best first.",
  "input_schema": {
    "type": "object",
    "properties": {
      "fromZone": { "type": "string", "enum": ["<zone ids from Firestore>"] },
      "toZone":   { "type": "string", "enum": ["<zone ids from Firestore>"] },
      "date":     { "type": "string", "description": "Cairo date YYYY-MM-DD" },
      "time":     { "type": "string", "description": "HH:mm, Cairo time; the search covers an hour before and after it" },
      "seats":    { "type": "integer", "minimum": 1, "maximum": 3 },
      "verifiedOnly": { "type": "boolean" },
      "womenOnly":    { "type": "boolean" }
    },
    "required": ["fromZone", "toZone", "date", "time"]
  }
}
```

System prompt (`ai/prompt.ts`, values filled at runtime):

```text
You are Hitchy's ride assistant for commuters in Greater Cairo.
Reply in Egyptian Arabic, or in English if the user writes in English. Use at most 3 short sentences.
Your only job is to help the rider find a seat in a real ride posted on Hitchy.
Always use the search_rides tool to find rides. Never invent rides, times, prices or people.
If the pickup zone, the destination zone or the time is missing, ask one short question.
If the user gives one time ("at 8"), pass it as the time; the search covers an hour before and after it.
Zones: {zone id = Arabic name / English name, one per line}.
Today in Cairo: {weekday, YYYY-MM-DD}, now {HH:mm}.
Rider's usual work zone: {zone or "unknown"}; usual hours: {start–end or "unknown"}.
Recommend at most 3 rides and say briefly why (time, same company, rating, Verified).
Never share phone numbers, plates or any personal details. If there are no rides, say so and suggest another time.
If asked about anything other than rides on Hitchy, politely bring the talk back to rides.
```

### 8.11 Fixed Ride and backup rides (Phase 12)

All Fixed Ride functions require `config/limits.fixedRideEnabled` → else `FEATURE_DISABLED`.

#### `createFixedRide` · callable · host

```ts
input:  { vehicleId: string; fromZone: string; fromPointId: string; toZone: string; toPointId: string;
          direction: Direction; weekdays: number[] /*1–7 unique values, 0–6*/; departTime: HHmm;
          seats: number; pricePerSeat: number; womenOnly: boolean; startDate: DateKey;
          requestId: string /*uuid*/ }
output: { fixedRideId: string; inviteCode: string }
```

Same checks as `createRide` steps 1–7, using `departAt = startDate at departTime`. No other `active` Fixed Ride of the host with the same `direction` → else `FIXED_RIDE_EXISTS`. Creates the doc with a unique `inviteCode`, `riderUids: []`, `status: "active"`.

#### `joinFixedRide` · callable · rider

```ts
input:  { inviteCode: string }
output: { fixedRideId: string }
```

Code exists and the Fixed Ride is `active`, and neither user blocked the other → else `INVITE_INVALID`; caller is not the host → else `OWN_RIDE`; not already a member → else `ALREADY_BOOKED`; members < seats → else `FIXED_RIDE_FULL`; Women-only rule → `WOMEN_ONLY`; no unpaid fee → `UNPAID_FEE`. Adds the caller to `riderUids`. Host gets `fixed_rider_joined`.

#### `leaveFixedRide` · callable

```ts
input:  { fixedRideId: string; riderUid: string | null }   // null = the caller leaves; the host passes a rider to remove
output: {}
```

Removes the rider and cancels their bookings on already generated future rides of this Fixed Ride.

#### `setFixedRideStatus` · callable · host

```ts
input:  { fixedRideId: string; status: "active" | "paused" | "ended" }
output: {}
```

#### `skipFixedDay` · callable · host or member

```ts
input:  { fixedRideId: string; dateKey: DateKey }
output: {}
```

Caller is the host or a member → else `NOT_ON_RIDE`; `dateKey` is a scheduled day after today → else `INVALID_INPUT`; `now < (dateKey - 1 day) at fixedSkipCutoff` → else `SKIP_TOO_LATE`. Writes `fixedRides/{id}/skips/{dateKey}`: a rider is added to `riderUids`; the host sets `hostSkipped: true`. After the cutoff, people use `cancelBooking` or `cancelRide`.

#### `generateFixedRides` · scheduled · daily 20:05

For each `active` Fixed Ride scheduled tomorrow with `startDate <= tomorrow` and no `hostSkipped` for tomorrow:

1. Skip if ride `fixed_{id}_{dateKey}` already exists.
2. Re-quote. When `pricePerSeat` is outside the new range, use `recommended` and notify the host `fixed_price_adjusted`.
3. Check the host's daily limit and time conflicts; on conflict skip the day and notify `fixed_day_conflict`.
4. Credits: inside the free month, or `credits.fixed >= 1` → else skip and notify `fixed_no_credits`.
5. Create the ride with `fixedRideId` and a `booked` booking (`seats: 1`, already accepted by joining) for each member who did not skip, is not suspended and has no unpaid fee (those get `fixed_skipped_unpaid_fee`). `seatsLeft = seats - bookings`; free seats show in normal search.

#### Backup flow

- `cancelRide` enqueues `backupAlert` for each affected rider; `cancelBooking` with `host_late` enqueues it for that rider.
- Task `backupAlert` `{ cancelledRideId, riderUid }`: scheduled now for Silver and Gold riders and after `backupPriorityDelaySec` for Bronze. It searches the same zones within `backupWindowMinutes` of the old `departAt` using `searchRidesCore(riderUid)`. With results, the rider gets `backup_options` (deep link `hitchy://backup/{cancelledRideId}`).

#### `getBackupOptions` · callable · rider

```ts
input:  { cancelledRideId: string }
output: { rides: RideCard[] /*≤3*/ }
```

The caller had a booking on that ride → else `NOT_ON_RIDE`. Runs the same search as the task.

`[LATER]` Offering a freed Fixed Ride seat to Gold riders first.

### 8.12 Safety, privacy and account (Phase 13)

#### `reportSafetyEvent` · callable (allowed while suspended)

```ts
input:  { rideId: string; lat: number | null; lng: number | null }
output: {}
```

Caller is on the ride (host or `booked | boarded` booking) → else `NOT_ON_RIDE`. Creates `safetyEvents` doc; `sendToAdmins("sos")` with high priority.

#### `reportUser` · callable

```ts
input:  { targetUid: string; rideId: string | null;
          category: "unsafe" | "harassment" | "no_show" | "payment" | "other"; text: string | null /*≤500*/ }
output: {}
```

`targetUid != uid` → else `CANNOT_REPORT_SELF`. Creates `reports` doc (`status: "open"`); `sendToAdmins("new_report")`.

#### `blockUser` / `unblockUser` · callable

```ts
input:  { targetUid: string }
output: {}
```

Not yourself → `INVALID_INPUT`; at most 200 blocked users → `INVALID_INPUT`. `arrayUnion` / `arrayRemove` on `blockedUids`.

#### `setSuspended` · callable · admin

```ts
input:  { uid: string; suspended: boolean; note: string | null }
output: {}
```

Writes `suspended`. When suspending: cancel the user's `open | full` rides (`cancelReason: "suspended"`) and `requested` or `booked` bookings with the normal cores. Audit.

#### `closeReport` / `handleSafetyEvent` · callable · admin

`{ reportId, note }` → report `closed`; `{ eventId }` → `handledBy = uid`. Audit.

#### `deleteAccount` · callable (allowed while suspended)

```ts
input:  { confirm: true }
output: {}
```

1. No host ride `open | full | started` and no `booked | boarded` booking with `departAt <= now + deleteBlockHours` → else `ACTIVE_TRIP_SOON`.
2. Cancel later rides and bookings with the normal cores (people get notified).
3. Leave or end the user's Fixed Rides.
4. Delete Storage `kyc/{uid}/` and `avatars/{uid}.jpg`.
5. Replace the UID with `"deleted"` in `ratings` (`fromUid`, `toUid`), `fees`, `packageOrders`, bookings (`riderUid`) and rides (`hostId`, and empty the plate).
6. Delete `codes` owned by the user, `kyc/{uid}`, `profiles/{uid}`, `aiUsage` docs and the user's chat messages; `recursiveDelete(users/{uid})` (this also removes the inbox).
7. `getAuth().deleteUser(uid)` last.

Each step is safe to repeat, so a failed run can be retried.

#### `purgeKycPhotos` · scheduled · daily 04:00 `[DECISION-14]`

`kyc` docs with status `approved | rejected` and `reviewedAt <= now - kycPhotoRetentionDays`: delete the Storage files, clear the paths, set `photosPurgedAt`.

### 8.13 Metrics (Phase 14)

- **Impact** (in `onRideFinished`), per completed booking, counting its seats:
  `liters = distanceKm × litersPer100Km.car / 100 × trafficFactor[trafficLevel] × shareOfSeatsReplacingSoloTrip × seats`;
  `co2Kg = liters × co2KgPerLiter`; `egpSaved = liters × fuelPriceEgpPerLiter`.
  Increment `metrics/impact` (`tripsCompleted`, `litersSaved`, `co2Kg`, `egpSaved`).
- **`weeklyMetrics`** · scheduled · Sunday 06:00: riders with a completed booking 4 weeks ago (the cohort) and how many of them had a completed booking last week. Writes `metrics/{isoWeek}` with `cohortSize`, `retained`, `rate`.

### 8.14 Triggers, jobs and task queues

All schedules use `timeZone: "Africa/Cairo"`.

| Name | Kind | When | Phase |
| --- | --- | --- | --- |
| `syncPublicProfile` | Firestore trigger | `users/{uid}` written | 4 |
| `onBookingChanged` | Firestore trigger | `rides/{rideId}/bookings/{riderUid}` written | 7 |
| `onRideFinished` | Firestore trigger | `rides/{rideId}` status becomes `finished` | 8 |
| `rideReminder` | Task queue | 30 minutes before departure | 7 |
| `expireRequest` | Task queue | at a request's deadline | 7 |
| `backupAlert` | Task queue | after a cancellation | 12 |
| `closeStaleRides` | Scheduled | every 30 minutes | 8 |
| `computeTiers` | Scheduled | daily 02:00 | 10 |
| `expirePoints` | Scheduled | daily 03:00 | 10 |
| `purgeKycPhotos` | Scheduled | daily 04:00 | 13 |
| `notifyFreeMonthEnding` | Scheduled | daily 10:00 | 9 |
| `generateFixedRides` | Scheduled | daily 20:05 | 12 |
| `weeklyRewards` | Scheduled | Thursday 23:00 | 10 |
| `weeklyMetrics` | Scheduled | Sunday 06:00 | 14 |

### 8.15 Notifications

`lib/notify.ts` sends to every device token of a user (`users/{uid}/devices`) and deletes tokens FCM reports as unregistered. The text uses the device's `language`. Every message carries `data: { type, deepLink }` so the app can route a tap. From v1.1 it also writes each message to the user's inbox (`users/{uid}/notifications`, both languages) `[DECISION-27]`; admin alerts are not stored. Text that riders read says "owner", not "host" `[DECISION-20]`.

| Type | To | Android channel | Deep link | English | Arabic |
| --- | --- | --- | --- | --- | --- |
| `new_host_application` | admins | admin | `{admin}/hosts` | New host application to review | فيه طلب صاحب عربية جديد للمراجعة |
| `host_approved` | user | account | `hitchy://host/status` | You're approved. Your first month is free! | اتوافق عليك! أول شهر ببلاش |
| `host_rejected` | user | account | `hitchy://host/status` | Some documents need fixing | فيه ورق محتاج يتعدل |
| `vehicle_reviewed` | user | account | `hitchy://host/status` | Your vehicle was reviewed | عربيتك اتراجعت |
| `verified_granted` | user | account | `hitchy://profile` | You're now Verified | بقيت Verified |
| `account_ready` | user, after the first `completeProfile` | account | `hitchy://home` | Your Hitchy account is ready | حسابك على Hitchy جاهز |
| `verify_request` | admins | admin | `{admin}/hosts` | A host asked for Verified | صاحب عربية طالب علامة Verified |
| `booking_requested` | host | bookings | `hitchy://ride/{rideId}` | {name} asked for {seats} seat(s) | {name} طلب يحجز {seats} كرسي |
| `booking_accepted` | rider | bookings | `hitchy://trip/{rideId}` | {name} accepted your request. Your seat is confirmed | {name} وافق على طلبك، كرسيك اتأكد |
| `booking_declined` | rider | bookings | `hitchy://search` | {name} couldn't take your request | {name} مقدرش ياخد طلبك |
| `request_expired` | rider | bookings | `hitchy://search` | Your request wasn't answered in time | طلبك متردش عليه في الوقت |
| `booking_cancelled` | host | bookings | `hitchy://ride/{rideId}` | {name} cancelled their booking | {name} لغى الحجز |
| `ride_cancelled` | rider | bookings | `hitchy://trips` | Your ride was cancelled | مشوارك اتلغى |
| `ride_reminder` | host, riders | trips | `hitchy://trip/{rideId}` | Your ride leaves in 30 minutes | مشوارك هيتحرك كمان نص ساعة |
| `ride_started` | riders | trips | `hitchy://trip/{rideId}` | Your owner is on the way | صاحب العربية في الطريق |
| `rider_at_pickup` | host | trips | `hitchy://trip/{rideId}` | {name} is at the pickup point | {name} وصل مكان الركوب |
| `fee_created` | rider | account | `hitchy://fees` | A 10 EGP late fee was recorded | اتسجلت عليك غرامة تأخير 10 جنيه |
| `fee_paid` | rider | account | `hitchy://fees` | Your late fee is marked as paid | الغرامة اتسجلت إنها اتدفعت |
| `fee_resolved` | rider, host | account | `hitchy://fees` | The fee dispute was resolved | الاعتراض على الغرامة اتحسم |
| `fee_disputed` | admins | admin | `{admin}/disputes` | A fee was disputed | فيه اعتراض على غرامة |
| `chat_message` | other side | trips | `hitchy://chat/{rideId}/{riderUid}` | New message from {name} | رسالة جديدة من {name} |
| `rate_trip` | rider | trips | `hitchy://rate/{rideId}` | How was your ride? | المشوار كان عامل إزاي؟ |
| `rate_riders` | host | trips | `hitchy://rate/{rideId}` | Rate your riders | قيّم الركاب |
| `payment_confirmed` | rider | account | `hitchy://receipt/{rideId}` | {name} confirmed your payment | {name} أكد إنك دفعت |
| `low_balance` | host | account | `hitchy://packages` | You have 2 trips left | فاضلك رحلتين |
| `free_month_ending` | host | account | `hitchy://packages` | Your free month ends soon | الشهر المجاني قرب يخلص |
| `package_confirmed` | host | account | `hitchy://packages` | Your package is active | الباكدج اتفعّل |
| `package_rejected` | host | account | `hitchy://packages` | We couldn't confirm your payment | مقدرناش نأكد الدفع |
| `fixed_rider_joined` | host | bookings | `hitchy://fixed/{id}` | {name} joined your Fixed Ride | {name} انضم لمشوارك الثابت |
| `fixed_price_adjusted` | host | account | `hitchy://fixed/{id}` | Tomorrow's seat price was adjusted | سعر الكرسي بكرة اتعدل |
| `fixed_day_conflict` | host | account | `hitchy://fixed/{id}` | Tomorrow's Fixed Ride was skipped (time conflict) | مشوار بكرة الثابت اتلغى بسبب تعارض |
| `fixed_no_credits` | host | account | `hitchy://packages` | Tomorrow's Fixed Ride needs credits | مشوار بكرة الثابت محتاج رصيد |
| `fixed_skipped_unpaid_fee` | rider | account | `hitchy://fees` | Pay your fee to keep your Fixed Ride seat | ادفع الغرامة عشان تحتفظ بكرسيك |
| `backup_options` | rider | bookings | `hitchy://backup/{rideId}` | We found other rides for you | لقينالك مشاوير تانية |
| `sos` | admins | admin | `{admin}/sos` | SOS from a ride | طلب استغاثة من مشوار |
| `new_report` | admins | admin | `{admin}/reports` | New user report | فيه بلاغ جديد |

`{name}` is the other person's first name and `{seats}` the number of seats. The quick phrases from v1.0 (`quick_*`) are now chat chips, so they have no notification types of their own. Review the Arabic copy before release.

`{admin}` is the admin dashboard URL (`ADMIN_URL` in `lib/app.ts`). `sendToAdmins` sends to the web push tokens of `config/admins.uids` and to the phones of `config/admins.notifyUids` (in English). On the phone, a link starting with `https://` opens in the browser.

---

## 9. Security rules

Rules allow reads that the apps need and only four kinds of client writes: the owner's basic profile fields (including saved places), the owner's device tokens, trip presence, and the `readAt` mark on the owner's inbox items. Everything else is written by functions (the Admin SDK skips rules). Admin tools also use the Admin SDK or admin-only reads.

### 9.1 Firestore matrix

| Path | get | list | create / update | delete |
| --- | --- | --- | --- | --- |
| `users/{uid}` | owner, admin | admin | owner update only: `OWNER_WRITABLE_USER_FIELDS`, valid types, `updatedAt == request.time` | — |
| `users/{uid}/devices/{id}` | owner | owner | owner, valid fields | owner |
| `users/{uid}/vehicles/{id}` | owner, admin | owner, admin | — | — |
| `users/{uid}/ledger/{id}` | owner, admin | owner, admin | — | — |
| `users/{uid}/points/{id}` | owner, admin | owner, admin | — | — |
| `users/{uid}/notifications/{id}` | owner | owner | owner update only: `readAt == request.time` | — (TTL) |
| `profiles/{uid}` | signed-in | signed-in | — | — |
| `kyc/{uid}` | owner, admin | admin | — | — |
| `companies`, `zones`, `zones/*/points`, `routes` | signed-in | signed-in | — | — |
| `config/{name}` | signed-in, except `admins` | — | — | — |
| `rides/{rideId}` | host, rider with an accepted booking (`booked`, `boarded`, `completed`), admin | host (query must filter `hostId == uid`), admin | — | — |
| `rides/{rideId}/bookings/{riderUid}` | that rider, the host, admin | the host, admin | — | — |
| collection group `bookings` | — | rider (query must filter `riderUid == uid`) | — | — |
| collection group `vehicles` | — | admin | — | — |
| `rides/{rideId}/presence/{uid}` | people on the ride (host or accepted booking), admin | people on the ride, admin | owner, on the ride, ride `started`, valid fields | — |
| `rides/{rideId}/chats/{riderUid}/messages/{id}` | the host, that rider | the host, that rider | — (`sendChatMessage`) | — (TTL) |
| `fees/{id}` | that rider, that host, admin | same, with a matching filter | — | — |
| `ratings/{id}` | author, admin | author (filter `fromUid == uid`), admin | — | — |
| `packageOrders/{id}` | owner host, admin | same, with a matching filter | — | — |
| `fixedRides/{id}` and `/skips/*` | host, members, admin | host (filter `hostId`), member (filter `riderUids array-contains uid`), admin | — | — |
| `reports`, `safetyEvents`, `adminActions` | admin | admin | — | — |
| `metrics/impact` | signed-in | — | — | — |
| `metrics/{week}` | admin | admin | — | — |
| `codes`, `idempotency`, `aiUsage`, `aiBudget` | — | — | — | — |

### 9.2 `firestore.rules` starting point

Write the rest of the file from the matrix. Every line must be covered by a test in `test/rules/`.

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() { return request.auth != null; }
    function isAdmin() { return signedIn() && request.auth.token.admin == true; }
    function isUser(uid) { return signedIn() && request.auth.uid == uid; }
    function ride(rideId) { return get(/databases/$(database)/documents/rides/$(rideId)); }
    // A request is not enough: the plate and the pickup coordinates are shown only after acceptance.
    function hasAcceptedBooking(rideId) {
      let b = /databases/$(database)/documents/rides/$(rideId)/bookings/$(request.auth.uid);
      return exists(b) && get(b).data.status in ['booked', 'boarded', 'completed'];
    }
    function isRideHost(rideId) { return signedIn() && ride(rideId).data.hostId == request.auth.uid; }
    function onRide(rideId) { return isRideHost(rideId) || hasAcceptedBooking(rideId); }

    function validProfile(d) {
      return d.firstName is string && d.firstName.size() > 0 && d.firstName.size() <= 40
        && d.lastName is string && d.lastName.size() > 0 && d.lastName.size() <= 40
        && (d.companyId == null || exists(/databases/$(database)/documents/companies/$(d.companyId)))
        && (d.companyOther == null || (d.companyOther is string && d.companyOther.size() <= 60))
        && (d.workHours == null || (d.workHours.start is string && d.workHours.end is string))
        && d.language in ['ar', 'en']
        && (d.photoPath == null || d.photoPath == 'avatars/' + request.auth.uid + '.jpg')
        && (d.emergencyContact == null
            || (d.emergencyContact.name is string && d.emergencyContact.phone is string))
        && d.savedPlaces is list && d.savedPlaces.size() <= 5
        && d.updatedAt == request.time;
    }
    function validDevice(d) {
      return d.keys().hasOnly(['fcmToken', 'platform', 'appVersion', 'language', 'updatedAt'])
        && d.fcmToken is string && d.fcmToken.size() < 4096
        && d.platform in ['android', 'ios', 'web'] && d.appVersion is string
        && d.language in ['ar', 'en'] && d.updatedAt == request.time;
    }
    function validPresence(d) {
      return d.keys().hasOnly(['last', 'samples', 'updatedAt'])
        && d.last.lat is number && d.last.lng is number && d.last.accuracyM is number
        && d.last.at is timestamp
        && d.samples is list && d.samples.size() <= 200
        && d.updatedAt == request.time;
    }

    match /users/{uid} {
      allow get: if isUser(uid) || isAdmin();
      allow list: if isAdmin();
      allow update: if isUser(uid)
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly([
             'firstName', 'lastName', 'companyId', 'companyOther', 'workHours',
             'language', 'photoPath', 'emergencyContact', 'savedPlaces', 'updatedAt'])
        && validProfile(request.resource.data);
      match /devices/{deviceId} {
        allow read, delete: if isUser(uid);
        allow create, update: if isUser(uid) && validDevice(request.resource.data);
      }
      match /notifications/{id} {
        allow read: if isUser(uid);
        allow update: if isUser(uid)
          && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['readAt'])
          && request.resource.data.readAt == request.time;
      }
      match /{sub}/{id} {
        allow read: if (isUser(uid) || isAdmin()) && sub in ['vehicles', 'ledger', 'points'];
      }
    }

    match /rides/{rideId} {
      allow get: if isAdmin() || (signedIn()
        && (resource.data.hostId == request.auth.uid || hasAcceptedBooking(rideId)));
      allow list: if isAdmin() || (signedIn() && resource.data.hostId == request.auth.uid);
      match /bookings/{riderUid} {
        allow get: if isAdmin() || isUser(riderUid) || isRideHost(rideId);
        allow list: if isAdmin() || isRideHost(rideId);
      }
      match /presence/{uid} {
        allow read: if isAdmin() || onRide(rideId);
        allow create, update: if isUser(uid) && onRide(rideId)
          && ride(rideId).data.status == 'started'
          && validPresence(request.resource.data);
      }
      match /chats/{riderUid}/messages/{messageId} {
        allow read: if isRideHost(rideId) || isUser(riderUid);
      }
    }

    match /{path=**}/bookings/{riderUid} {
      allow list: if signedIn() && resource.data.riderUid == request.auth.uid;
    }

    match /{path=**}/vehicles/{vehicleId} {
      allow list: if isAdmin();
    }

    // … profiles, kyc, companies, zones, routes, config, fees, ratings,
    //   packageOrders, fixedRides, reports, safetyEvents, adminActions, metrics
    //   exactly as in the matrix. Anything not matched is denied.
  }
}
```

### 9.3 `storage.rules`

```text
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    function signedIn() { return request.auth != null; }
    function isAdmin() { return signedIn() && request.auth.token.admin == true; }
    function isUser(uid) { return signedIn() && request.auth.uid == uid; }
    function kycOpen(uid) {
      return !firestore.exists(/databases/(default)/documents/kyc/$(uid))
        || firestore.get(/databases/(default)/documents/kyc/$(uid)).data.status in ['none', 'rejected'];
    }

    match /kyc/{uid}/{file} {
      allow read: if isAdmin();
      allow create, update: if isUser(uid)
        && file in ['id_front.jpg', 'id_back.jpg', 'driving_license.jpg',
                    'vehicle_license.jpg', 'criminal_record.jpg']
        && request.resource.contentType == 'image/jpeg'
        && request.resource.size < 5 * 1024 * 1024
        && (kycOpen(uid) || file == 'criminal_record.jpg');
      allow delete: if false;
    }

    match /avatars/{fileName} {
      allow read: if signedIn();
      allow create, update: if signedIn() && fileName == request.auth.uid + '.jpg'
        && request.resource.contentType == 'image/jpeg'
        && request.resource.size < 2 * 1024 * 1024;
    }
  }
}
```

The criminal record can be uploaded at any time, so an approved host can ask for the Verified badge later.

---

## 10. Indexes

`backend/firestore.indexes.json`. Deploy indexes a few minutes before the code that needs them. If a query fails with a "requires an index" error, the error message contains a link that creates it: add the same index to this file.

```json
{
  "indexes": [
    { "collectionGroup": "rides", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "fromZone", "order": "ASCENDING" }, { "fieldPath": "toZone", "order": "ASCENDING" },
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "departAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "rides", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "hostId", "order": "ASCENDING" }, { "fieldPath": "departAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "rides", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "hostId", "order": "ASCENDING" }, { "fieldPath": "departAt", "order": "DESCENDING" } ] },
    { "collectionGroup": "rides", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "departAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "bookings", "queryScope": "COLLECTION_GROUP", "fields": [
      { "fieldPath": "riderUid", "order": "ASCENDING" }, { "fieldPath": "departAt", "order": "DESCENDING" } ] },
    { "collectionGroup": "bookings", "queryScope": "COLLECTION_GROUP", "fields": [
      { "fieldPath": "riderUid", "order": "ASCENDING" }, { "fieldPath": "status", "order": "ASCENDING" },
      { "fieldPath": "departAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "bookings", "queryScope": "COLLECTION_GROUP", "fields": [
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "departAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "bookings", "queryScope": "COLLECTION_GROUP", "fields": [
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "expiresAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "kyc", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "submittedAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "packageOrders", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "packageOrders", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "hostId", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "DESCENDING" } ] },
    { "collectionGroup": "fees", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "fees", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "riderUid", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "DESCENDING" } ] },
    { "collectionGroup": "fees", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "hostId", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "DESCENDING" } ] },
    { "collectionGroup": "reports", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "users", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "host.freeEndingNotified", "order": "ASCENDING" },
      { "fieldPath": "host.freeUntil", "order": "ASCENDING" } ] },
    { "collectionGroup": "points", "queryScope": "COLLECTION", "fields": [
      { "fieldPath": "open", "order": "ASCENDING" }, { "fieldPath": "expiresAt", "order": "ASCENDING" } ] },
    { "collectionGroup": "points", "queryScope": "COLLECTION_GROUP", "fields": [
      { "fieldPath": "open", "order": "ASCENDING" }, { "fieldPath": "expiresAt", "order": "ASCENDING" } ] }
  ],
  "fieldOverrides": [
    { "collectionGroup": "bookings", "fieldPath": "riderUid", "indexes": [
      { "order": "ASCENDING", "queryScope": "COLLECTION" },
      { "order": "DESCENDING", "queryScope": "COLLECTION" },
      { "order": "ASCENDING", "queryScope": "COLLECTION_GROUP" } ] },
    { "collectionGroup": "vehicles", "fieldPath": "status", "indexes": [
      { "order": "ASCENDING", "queryScope": "COLLECTION" },
      { "order": "DESCENDING", "queryScope": "COLLECTION" },
      { "order": "ASCENDING", "queryScope": "COLLECTION_GROUP" } ] }
  ]
}
```

---

## Phase 0 — Accounts, GitHub, Firebase

**Exit:** both Firebase projects on Blaze with budget alerts, the repo protected, and a first pull request passing CI.

### Human tasks

| ID | Task | Details |
| --- | --- | --- |
| P0-H01 | GitHub repo | Private repo `hitchy`. Protect `main`: pull request, 1 approval, required checks, no force push, squash merge only. Turn on secret scanning and push protection. Create Environments `dev` and `prod` (prod needs Islam's approval). |
| P0-H02 | Firebase projects | `hitchy-dev` and `hitchy-prod`, both on Blaze. Budget alerts at 10, 25 and 50 USD to Islam's email. |
| P0-H03 | Region `[DECISION-01]` | Create Firestore (Native mode) and the default Storage bucket in the chosen region, in both projects. Firestore's location can never change. |
| P0-H04 | Phone sign-in | Turn on the Phone provider. In `hitchy-dev`, add test numbers `+201000000001` … `+201000000009` with code `123456`. Set the SMS region policy to allow Egypt only. |
| P0-H05 | Android apps `[DECISION-02]` | `com.hitchy.app.dev` in `hitchy-dev`, `com.hitchy.app` in `hitchy-prod`. Add SHA-1 and SHA-256 of the debug and release keys. Save each `google-services.json` base64-encoded as GitHub secrets `GOOGLE_SERVICES_DEV` and `GOOGLE_SERVICES_PROD`. |
| P0-H06 | Release key | Create the release keystore. GitHub secrets: `ANDROID_KEYSTORE_B64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Keep a backup in a password manager. |
| P0-H07 | App Check | Register Play Integrity (Android) and App Attest (iOS) in both projects. Enforcement stays off until Phase 14. |
| P0-H08 | API keys | In Google Cloud console, restrict the Android key to the package names and SHA-1s, and the iOS key to the bundle IDs. |
| P0-H09 | CI access | Workload Identity Federation from GitHub Actions to each project, with a deploy service account that can deploy functions, rules, indexes, hosting and App Distribution. Environment secrets: `GCP_WIF_PROVIDER`, `GCP_SERVICE_ACCOUNT`, `FIREBASE_ANDROID_APP_ID`. |
| P0-H10 | Accounts | Google Play Console `[DECISION-03]`, Apple Developer, a domain, and a support email. |
| P0-H11 | iOS config | Register the iOS app in both projects and send the config files to the iOS teammate privately. |
| P0-H12 | TTL policy | Firestore TTL policies on `expireAt` for the `idempotency`, `notifications` and `messages` collection groups, in both projects. |

### P0-A01 · Repo skeleton

- **Files:** `.gitignore`, `README.md`, `CLAUDE.md` (Appendix A), `docs/BUILD_PLAN.md` (this file), `docs/adr/0001-architecture.md`, `.github/CODEOWNERS`, `.github/pull_request_template.md`, `.github/ISSUE_TEMPLATE/bug.md`, `.github/ISSUE_TEMPLATE/task.md`, `.github/dependabot.yml`, `contract/README.md`.
- **Spec:**
  - `.gitignore` covers: `local.properties`, `*.jks`, `*.keystore`, `google-services.json`, `GoogleService-Info.plist`, `node_modules/`, `backend/functions/lib/`, `.firebase/`, `*-debug.log`, `.env*`, `.idea/` (except shared code style), `build/`, `.gradle/`, `.DS_Store`.
  - `CODEOWNERS`: `/contract/` → Islam and the iOS teammate; `/backend/` and `/android/` → Islam; `/ios/` → iOS teammate.
  - The pull request template has the task ID, what changed, how to test, screenshots, "contract changed?" and a checklist of the rules in section 1.
  - `dependabot.yml`: weekly, grouped updates for `gradle` (`/android`), `npm` (`/backend/functions`) and `github-actions` (`/`).
  - `0001-architecture.md` records the decisions in section 3 with one line of reasoning each.
  - `contract/README.md`: any change needs both owners' approval, fixtures in the same pull request, and no field is removed or renamed without a deprecation period.
- **Done when:** all files exist and the README explains the layout. Commands in the README are added by later tasks.

---

## Phase 1 — Backend foundation

**Exit:** `npm test` runs unit, rules and integration tests against the emulators; `npm run seed` loads the emulator; CI deploys to `hitchy-dev` on merge; contract v1 is merged with the iOS teammate's approval.

### P1-B01 · Scaffold the backend

- **Files:** `backend/firebase.json`, `backend/.firebaserc`, `backend/functions/{package.json, tsconfig.json, .eslintrc.cjs, .prettierrc, vitest.config.ts, .nvmrc, src/index.ts}`.
- **Spec:**
  - Pin the latest Node.js LTS that Cloud Functions supports in `.nvmrc`, `package.json` `engines`, and the functions `runtime` in `firebase.json`.
  - `tsconfig`: `strict`, `noUncheckedIndexedAccess`, `noImplicitOverride`, output to `lib/`.
  - `firebase.json` emulators: auth 9099, firestore 8080, functions 5001, storage 9199, hosting 5000, UI 4000, `singleProjectMode: true`. Functions `predeploy` runs lint and build.
  - `.firebaserc` aliases: `dev` → `hitchy-dev`, `prod` → `hitchy-prod`.
  - Local runs and tests use the project ID `demo-hitchy`. A `demo-` project never touches real cloud resources.
  - Scripts:

    | Script | Command |
    | --- | --- |
    | `build` | `tsc` |
    | `typecheck` | `tsc --noEmit` |
    | `lint` | `eslint . && prettier --check .` |
    | `test:unit` | `vitest run test/unit` |
    | `test` | `firebase emulators:exec --project demo-hitchy "vitest run"` |
    | `emulators` | `firebase emulators:start --project demo-hitchy` |
    | `seed` | `tsx ../seed/seed.ts` (emulator by default; `--project dev` for `hitchy-dev`) |

- **Done when:** `npm run build`, `npm run lint` and `npm test` pass with one placeholder test.

### P1-B02 · Shared backend library

- **Files:** everything in `src/lib/` from [4.2](#42-backend), plus `src/lib/notifyTemplates.ts` (section 8.15) and tests in `test/unit/lib/`.
- **Spec:** as in sections 5 and 8.1. `errors.ts` holds the `Reason` union and code map from Appendix D. `config.ts` exports typed `getConfig("pricing" | "packages" | "limits" | "rewards" | "impact")` with a 60-second cache and `clearConfigCache()` for tests. `REGION` in `app.ts` is a constant `[DECISION-01]`.
- **Done when:** unit tests cover: every reason maps to its HTTPS code; `dateKey`, `minuteOfDay` and `startOfCairoWeek` across the days Egypt's summer time starts and ends (compute them with luxon, do not hard-code); short codes use only the allowed alphabet; the config cache expires.

### P1-B03 · Schemas

- **Files:** `src/schemas/docs.ts` (all of section 6; written in P1-B02, this task checks it), `src/schemas/io.ts` (8.2 and the outputs of Phases 4 to 8), `src/schemas/inputs.ts` (inputs of Phases 4 to 8; later phases add theirs).
- **Done when:** types compile; each zod schema has a unit test with one valid and one invalid example.

### P1-B04 · Pricing and geo

- **Files:** `src/domain/pricing.ts`, `src/domain/geo.ts`, `test/unit/domain/pricing.test.ts`, `geo.test.ts`.
- **Spec:** section 8.5 "Pricing formula". `geo.ts` exports `distanceM(a, b)` (haversine).
- **Done when:** all 9 golden cells pass; traffic level is right at 06:59, 07:00, 09:59, 10:00, 15:59, 16:00, 18:59, 19:00; scooter with `null` config is refused by the caller.

### P1-B05 · Firestore rules

- **Files:** `backend/firestore.rules`, `test/rules/firestore/*.test.ts`.
- **Spec:** section 9.1 and 9.2.
- **Done when:** for every row of the matrix there is at least one allowed and one denied test, including: owner cannot write `host`, `credits`, `points`, `activeRole` or `gender`; a rider cannot read another rider's booking; a non-booked user cannot get a ride; presence write is refused when the ride is not `started`.

### P1-B06 · Storage rules

- **Files:** `backend/storage.rules`, `test/rules/storage.test.ts`.
- **Spec:** section 9.3.
- **Done when:** tests cover: other users cannot read `kyc`; owner cannot upload while `kyc` is `submitted` (except `criminal_record.jpg`); files over 5 MB or not JPEG are refused; avatars are readable by signed-in users only.

### P1-B07 · Indexes

- **Files:** `backend/firestore.indexes.json` (section 10).
- **Done when:** `firebase deploy --only firestore:indexes --project demo-hitchy --dry-run` style validation passes (or the emulator loads it without errors).

### P1-B08 · Seed data

- **Files:** `backend/seed/seed.ts`, `backend/seed/data/*.json` (section 7.6).
- **Spec:** idempotent (safe to run twice). Uses the Admin SDK against the emulator by default. Creates test users in the Auth emulator with their phone numbers, the admin claim for `admin@test`, `config/admins`, and an approved host with an approved car. `--project dev` writes only config, zones, routes and companies.
- **Done when:** `npm run emulators` then `npm run seed` shows all documents in the emulator UI.

### P1-B09 · Admin script

- **Files:** `backend/scripts/set-admin.ts`.
- **Spec:** `npm run set-admin -- --uid <uid> --project <dev|prod> [--remove]`. Sets or removes the `admin` custom claim and updates `config/admins.uids`.
- **Done when:** running it against the emulator changes the claim (checked by an integration test).

### P1-B10 · Contract v1

- **Files:** `contract/firestore.md`, `contract/functions.md` (Phases 4–8), `contract/errors.md` (Appendix D), `contract/fixtures/*.json`, `test/unit/contract.test.ts`.
- **Spec:** fixtures are realistic JSON for every request and response in `functions.md`, named `<function>.request.json` and `<function>.response.json`.
- **Done when:** the test parses every request fixture with its zod schema and every response fixture against its TypeScript type (via a zod mirror). `[HUMAN]` Islam and the iOS teammate review it together and both approve.

### P1-B11 · Backend CI

- **Files:** `.github/workflows/backend.yml`.
- **Spec:**
  - Triggers: pull requests and pushes to `main` touching `backend/**`, `contract/**` or the workflow; tags `backend-v*`; manual run.
  - Job `test`: Node from `.nvmrc`, Java 21 (emulators), `npm ci`, lint, typecheck, build, `npm test`.
  - Job `deploy-dev`: after `test`, on push to `main`, environment `dev`, auth with `google-github-actions/auth` (Workload Identity), then `npx firebase-tools deploy --only functions,firestore,storage --project hitchy-dev --non-interactive`.
  - Job `deploy-prod`: on tag `backend-v*`, environment `prod` (manual approval), same command with `hitchy-prod`.
- **Done when:** a pull request shows the `test` job green.

---

## Phase 2 — Android foundation

**Exit:** `devDebug` and `prodRelease` build; the debug screen reads `config/pricing` from the emulator and from `hitchy-dev`; `android.yml` passes.

### P2-A01 · Project and modules

- **Files:** `android/` as in [4.3](#43-android).
- **Spec:** start from the current Kotlin Multiplatform wizard output (Android target, Compose Multiplatform, no iOS), then shape it into `app` (Android application) and `shared` (KMP library with the Android KMP library plugin `com.android.kotlin.multiplatform.library`, `androidTarget` only). Package root `com.hitchy`. Pin the latest stable versions and record them in `docs/adr/0002-versions.md`.
- **Done when:** `./gradlew :app:assembleDebug` builds an app that shows a "Hello Hitchy" Compose screen from `shared`.

### P2-A02 · Convention plugins

- **Files:** `android/build-logic/convention/…`.
- **Spec:** `hitchy.android.application` (compileSdk and targetSdk = the latest level Google Play requires, minSdk 26 proposed, Java 17 toolchain), `hitchy.kmp.library` (androidTarget, Compose, serialization, common test dependencies), `hitchy.quality` (detekt, Spotless with ktlint).
- **Done when:** `app` and `shared` build files only apply these plugins plus their own dependencies.

### P2-A03 · Flavors, signing, build config

- **Spec:**
  - Flavor dimension `env`: `dev` (`applicationIdSuffix ".dev"`, app name "Hitchy Dev") and `prod`.
  - `BuildConfig`: `ENV`, `FUNCTIONS_REGION` (`[DECISION-01]`), `USE_EMULATOR` (true only in `dev` when Gradle property `hitchy.emulator=true`).
  - These values reach `shared` through an `AppConfig` data class registered in Koin, not through `BuildConfig` imports in `shared`.
  - Release signing reads environment variables (`HITCHY_KEYSTORE_PATH`, `HITCHY_KEYSTORE_PASSWORD`, `HITCHY_KEY_ALIAS`, `HITCHY_KEY_PASSWORD`); without them the release build uses the debug key locally.
  - Release: R8 on, resource shrinking on, keep rules for `@Serializable` classes.
  - `versionCode` from Gradle property `hitchy.versionCode` (CI run number), default 1; `versionName` from `hitchy.versionName`, default `0.1.0`.
- **Done when:** `assembleDevDebug`, `assembleProdDebug` and `assembleProdRelease` build.

### P2-A04 · Core utilities

- **Files:** `core/data/AppError.kt`, `core/data/AppResult.kt`, `core/platform/Clock.kt`, `core/util/CairoTime.kt`, `core/util/MoneyFormat.kt`, `core/util/PhoneNumber.kt`, tests.
- **Spec:**

```kotlin
sealed interface AppError {
    data object Network : AppError
    data object NotSignedIn : AppError
    data class Reason(val code: String, val extra: String? = null) : AppError  // from details.reason
    data class Unknown(val message: String?) : AppError
}
sealed interface AppResult<out T> {
    data class Ok<T>(val value: T) : AppResult<T>
    data class Err(val error: AppError) : AppResult<Nothing>
}
object PhoneNumber {
    /** "01012345678", "+201012345678", "00201012345678" → "+201012345678"; null when invalid.
     *  Valid: 11 digits starting 010, 011, 012 or 015. Arabic-Indic digits are accepted. */
    fun normalize(input: String): String?
}
```

- **Done when:** tests cover every phone format (valid and invalid, Arabic-Indic digits), Cairo time on the summer time change days, and EGP formatting.

### P2-A05 · Firebase wiring

- **Files:** `core/data/FirebaseModule.kt`, `core/data/FunctionsClient.kt`, `app/HitchyApplication.kt`.
- **Spec:**
  - The official SDK initializes from `google-services.json`; GitLive uses the same default app.
  - When `USE_EMULATOR`: connect Auth (`10.0.2.2:9099`), Firestore (`10.0.2.2:8080`), Functions (`10.0.2.2:5001`) and Storage (`10.0.2.2:9199`) before any other Firebase call.
  - `FunctionsClient.call(name, input)` uses `Firebase.functions(region = AppConfig.functionsRegion)`, serializes with kotlinx.serialization, and maps errors: network → `AppError.Network`; unauthenticated → `NotSignedIn`; anything with `details.reason` → `Reason(code)`; else `Unknown`.
  - App Check: Play Integrity provider in `prod`, debug provider in `dev`.
  - Crashlytics: user ID = Firebase UID after sign-in; collection off when `USE_EMULATOR`.
- **Done when:** a unit test checks the error mapping with a fake; the app starts against the emulator.

### P2-A06 · Dependency injection

- **Spec:** `startKoin` in `HitchyApplication` with `appModule` (Android implementations, `AppConfig`), `coreModule` (Firebase, `FunctionsClient`, `Clock`, `AppSettings`) and one module per feature. ViewModels use `koinViewModel()`.
- **Done when:** a Koin `verify()` test passes for all modules.

### P2-A07 · Navigation and deep links

- **Files:** `navigation/Routes.kt`, `navigation/HitchyNavHost.kt`, `navigation/DeepLinks.kt`, tests.
- **Spec:** type-safe routes, each with a placeholder screen until its phase:

```kotlin
@Serializable data object Welcome
@Serializable data object PhoneEntry
@Serializable data class OtpVerify(val phoneE164: String)
@Serializable data object ProfileSetup
@Serializable data object Home                       // rider or host shell by activeRole
@Serializable data object Settings
@Serializable data object HostApplication
@Serializable data object HostStatus
@Serializable data object AddVehicle
@Serializable data object PostRide
@Serializable data object MyRides
@Serializable data class HostRideDetails(val rideId: String)
@Serializable data object Search
@Serializable data class SearchResults(val fromZone: String, val toZone: String, val fromPointId: String?,
    val date: String, val time: String, val seats: Int)   // sort and filters live in the screen state (v1.1)
@Serializable data class RiderRideDetails(val rideId: String)
@Serializable data object MyTrips
@Serializable data class HostTrip(val rideId: String)
@Serializable data class RiderTrip(val rideId: String)
@Serializable data class RateTrip(val rideId: String)
@Serializable data object Fees
@Serializable data object Packages
@Serializable data class PaymentInstructions(val orderId: String)
@Serializable data object CreditHistory
@Serializable data object Rewards
@Serializable data object Invite
@Serializable data object Assistant
@Serializable data object FixedRides
@Serializable data object CreateFixedRide
@Serializable data class FixedRideDetails(val fixedRideId: String)
@Serializable data object JoinFixedRide
@Serializable data class BackupOptions(val cancelledRideId: String)
@Serializable data object EmergencyContact
@Serializable data object DeleteAccount
@Serializable data object Debug
// Added in v1.1 (designs):
@Serializable data object Onboarding
@Serializable data class RoutePicker(val field: String)       // "from" or "to"
@Serializable data class MapPointPicker(val field: String)    // fixed points only
@Serializable data object Notifications                       // inbox
@Serializable data class Chat(val rideId: String, val riderUid: String)
@Serializable data class Receipt(val rideId: String)
@Serializable data object Shop                                // Rewards (rider) or Packages (host)
```

  Deep links (scheme `hitchy://`): `ride/{rideId}` → host or rider ride details by role · `trip/{rideId}` → host or rider trip · `rate/{rideId}` · `trips` · `fees` · `packages` · `rewards` · `profile` · `host/status` · `fixed/{id}` · `backup/{rideId}`, and from v1.1 `search` · `receipt/{rideId}` · `chat/{rideId}/{riderUid}` · `notifications` · `home` · `shop`. Links starting with `https://` (admin alerts) open in the browser.
- **Done when:** `DeepLinks.parse()` has a test per link, including bad input returning `null`.

### P2-A08 · Screen pattern

- **Files:** `core/ui/MviViewModel.kt`, `core/ui/ErrorText.kt`, one sample screen and its test.

```kotlin
abstract class MviViewModel<S, A, E>(initial: S) : ViewModel() {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<S> = _state.asStateFlow()
    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()
    protected fun setState(reduce: S.() -> S) = _state.update(reduce)
    protected suspend fun emit(event: E) = _events.send(event)
    abstract fun onAction(action: A)
}
```

  Screens: `val state by vm.state.collectAsStateWithLifecycle()`; events collected once in a `LaunchedEffect`. `AppError.message()` returns the string resource from Appendix D (`error_<reason_lowercase>`).
- **Done when:** the sample ViewModel test (Turbine) checks a loading → success → event flow.

### P2-A09 · Strings and language

- **Files:** `composeResources/values/strings.xml`, `values-ar/strings.xml`, `app/src/main/res/xml/locales_config.xml`, `core/platform/LanguageSwitcher.kt` + Android implementation.
- **Spec:** all error strings from Appendix D, common words (OK, Cancel, Retry, Save, Next, Back), app name. `MainActivity` extends `AppCompatActivity`; language switching uses `AppCompatDelegate.setApplicationLocales` and the `locales_config.xml` for Android 13+ settings. Default language `[DECISION-04]`.
- **Done when:** switching the language in Settings re-renders the app in Arabic with RTL layout.

### P2-A10 · Platform interfaces

- **Files:** `core/platform/*.kt` (common) and `androidMain/…/platform/*.kt`.

```kotlin
interface PhoneAuth {                       // implemented in Phase 4
    val events: Flow<PhoneAuthEvent>
    fun start(phoneE164: String, forceResend: Boolean = false)
    suspend fun confirm(code: String): AppResult<Unit>
}
sealed interface PhoneAuthEvent {
    data class CodeSent(val canResendAtMs: Long) : PhoneAuthEvent
    data object AutoVerified : PhoneAuthEvent
    data class Failed(val error: AppError) : PhoneAuthEvent
}
interface LocationProvider { fun hasPermission(): Boolean; suspend fun current(): GeoPoint? }
interface TripTracker { fun start(rideId: String); fun stop() }        // Phase 8
interface ImageCompressor { suspend fun toJpeg(bytes: ByteArray, maxSidePx: Int = 1600, quality: Int = 80): ByteArray }
interface Sharer { fun shareText(text: String) }
interface Dialer { fun dial(number: String); fun sms(number: String, body: String) }
interface Analytics { fun log(event: String, params: Map<String, Any> = emptyMap()) }
interface AppSettings {                    // DataStore
    val lastRole: Flow<Role?>; suspend fun setLastRole(role: Role)
    val installId: Flow<String>            // random UUID created once
}
```

  Pickers and permission prompts are `expect`/`actual` composables (`rememberImagePicker`, `rememberPermissionRequest`) using the Activity Result API on Android. `ActivityProvider` (androidMain only) tracks the current Activity for phone sign-in.
- **Done when:** every interface has an Android implementation (or a clear `TODO(P<n>)` for later phases) and a fake in `commonTest`.

### P2-A11 · Debug screen

- **Spec:** `dev` builds only, opened from Settings with a long press on the version number. Shows UID, `ENV`, region, `USE_EMULATOR`, `config/pricing` values (live), FCM token, App Check token status, Remote Config values, and a "Test crash" button.
- **Done when:** it shows `config/pricing` from the seeded emulator.

### P2-A12 · Code quality

- **Spec:** detekt with `config/detekt/detekt.yml`; Spotless with ktlint; Android Lint with a baseline; LeakCanary in `devDebug` only. Konsist tests in `shared/src/commonTest` (or the Android host test set the wizard creates):
  1. `core.model` and every `..domain..` package import nothing from `dev.gitlive`, `com.google.firebase`, `android` or `androidx`.
  2. Classes ending in `ViewModel` live in `..presentation..`.
  3. `..ui..` packages never import `..data..`.
  4. Repository implementations live in `..data..` and implement an interface from `..domain..`.
- **Done when:** `./gradlew check` runs detekt, Spotless, Lint, Konsist and all unit tests, and passes.

### P2-A13 · Contract DTO tests

- **Spec:** `core/data/dto/` holds `@Serializable` mirrors of every input and output in section 8 (same field names). A test reads every file in `../contract/fixtures/` and decodes it with a strict `Json` (`ignoreUnknownKeys = false`). The production `Json` uses `ignoreUnknownKeys = true`.
- **Done when:** the test passes for all Phase 4–8 fixtures.

### P2-A14 · Android CI

- **Files:** `.github/workflows/android.yml`.
- **Spec:**
  - Pull request touching `android/**` or `contract/**`: Java 17+, `gradle/actions/setup-gradle` cache, decode `GOOGLE_SERVICES_DEV`, run `./gradlew check assembleDevDebug`.
  - Push to `main`: build `devRelease` with `hitchy.versionCode=${{ github.run_number }}` and upload with `npx firebase-tools appdistribution:distribute <apk> --app $FIREBASE_ANDROID_APP_ID --groups team` (environment `dev`).
  - Tag `android-v*`: build `prodRelease`, upload to group `pilot` (environment `prod`).
- **Done when:** a pull request shows the check job green.

---

## Phase 3 — Design system

**Source:** Figma file "Hitchy 🚙", key `M2XGt24CJooYzujBLKqLZL`. Frames: Brand `14:82` · Colors `14:83` · Typography `14:84` · Spacing and layout `14:85` · Inputs and controls `14:86` · Navigation and icons `14:87` · Feedback and status `14:88` · Ride components `14:89`.

The values below were copied from Figma on 2026-10-28. Before each task, open the frame through the Figma connection (screenshot and design context) to read exact sizes. If Figma and this section differ, stop and ask.

From v1.1 the finished app screens (PNG exports, reviewed 2026-10-29) are the source for screen layouts. They use these same tokens; their green, amber, red and blue chips and banners are the semantic sets in 3.1. [3.8](#38-app-designs-screen-map-and-fix-list) maps each design to its screen and task and lists what the designs still need to change.

**Exit:** `HitchyTheme` and every component in 3.5 exist in `core/ui`, each with previews and Roborazzi screenshots in English and Arabic, and the gallery screen shows them all.

### 3.1 Colors

| Group | Token (Figma) | Code name | Hex | Use |
| --- | --- | --- | --- | --- |
| Brand | `color/brand/purple` | `purple` | `#7C3AED` | Primary: buttons, active states, links |
| Brand | `color/brand/deep-purple` | `deepPurple` | `#4F46E5` | Secondary accents, gradient end |
| Brand | `color/brand/navy` | `navy` | `#1E1B4B` | Headings and primary text |
| Brand | `color/brand/violet` | `violet` | `#A855F7` | Gradient start, decoration only (4.0:1, not for small text) |
| Brand | `color/brand` (pin) | `pin` | `#FF3366` | Map pin, drop-off marker |
| Brand | `color/brand/lavender` | `lavender` | `#EDE9FE` | Tonal buttons, selected items, avatar background |
| Brand | `color/brand/lilac` | `lilac` | `#F5F3FF` | Cards and info panels |
| Neutral | `color/neutral/white` | `white` | `#FFFFFF` | Surfaces |
| Neutral | `color/neutral/background` | `background` | `#F8F9FC` | Screen background |
| Neutral | `color/neutral/border` | `border` | `#E5E7EB` | Borders, dividers |
| Neutral | `color/neutral/disabled` | `disabledSurface` | `#F3F4F6` | Disabled fields and buttons |
| Text | `color/text/primary` | `textPrimary` | `#1E1B4B` | Main text |
| Text | `color/text/secondary` | `textSecondary` | `#6B7280` | Helper and meta text |
| Text | `color/text/disabled` | `textDisabled` | `#9CA3AF` | Disabled text |
| Semantic | `color/semantic/success` · `-bg` · `-text` | `success*` | `#16A34A` · `#DCFCE7` · `#166534` | Confirmed, verified |
| Semantic | `color/semantic/warning` · `-bg` · `-text` | `warning*` | `#F59E0B` · `#FEF3C7` · `#92400E` | Pending, late |
| Semantic | `color/semantic/error` · `-bg` · `-text` | `error*` | `#EF4444` · `#FEE2E2` · `#B91C1C` | Errors, cancelled |
| Semantic | `color/semantic/info` · `-bg` · `-text` | `info*` | `#3B82F6` · `#DBEAFE` · `#1D4ED8` | Info |
| Gradient | `Gradient/Brand` | `brandGradient` | `#A855F7 → #7C3AED → #4F46E5` | Splash, branding, hero only. Buttons stay solid purple. |

Rule from Figma: small text on white or tinted backgrounds uses the `…-text` tokens; the base semantic colors are for icons, borders and fills.

Material 3 light `ColorScheme`: `primary` purple, `onPrimary` white, `primaryContainer` lavender, `onPrimaryContainer` purple, `secondary` deepPurple, `onSecondary` white, `tertiary` pin, `background` background, `onBackground` textPrimary, `surface` white, `onSurface` textPrimary, `surfaceVariant` lilac, `onSurfaceVariant` textSecondary, `outline` border, `error` error, `onError` white, `errorContainer` errorBg, `onErrorContainer` errorText. Everything else (navy, violet, pin, lilac, semantic sets, disabled colors, gradient) lives in `HitchyColors` through a `CompositionLocal`. Light theme only `[DECISION-05]`: Figma has no dark tokens.

### 3.2 Typography

One family for English and Arabic: **Cairo** (Regular 400, SemiBold 600, Bold 700) as Compose resources in `composeResources/font/`. This replaces Poppins and IBM Plex Sans Arabic from earlier plans.

| Figma style | Size / line height | Weight | `HitchyTypography` | Material slot |
| --- | --- | --- | --- | --- |
| Display | 32 / 40 | Bold | `display` | `displaySmall` |
| H1 | 28 / 36 | Bold | `h1` | `headlineLarge` |
| H2 | 24 / 32 | Bold | `h2` | `headlineMedium` |
| H3 | 20 / 28 | Bold | `h3` | `headlineSmall` |
| Title | 18 / 26 | SemiBold | `title` | `titleLarge` |
| Body Large | 16 / 24 | Regular | `bodyLarge` | `bodyLarge` |
| Body Large SemiBold | 16 / 24 | SemiBold | `bodyLargeStrong` | `titleMedium` |
| Body | 14 / 22 | Regular | `body` | `bodyMedium` |
| Body SemiBold | 14 / 22 | SemiBold | `bodyStrong` | `titleSmall` |
| Caption | 12 / 18 | Regular | `caption` | `bodySmall` |
| Caption SemiBold | 12 / 18 | SemiBold | `captionStrong` | `labelSmall` |
| Button | 16 / 24 | Bold | `button` | `labelLarge` |

Sizes and line heights in `sp`. Turn off font padding (`PlatformTextStyle(includeFontPadding = false)`) so line heights match Figma. Arabic uses the same styles with the layout mirrored (RTL).

### 3.3 Spacing, radius and layout

| Token | Value |
| --- | --- |
| `space/4 · 8 · 12 · 16 · 24 · 32 · 40 · 48` | 4, 8, 12, 16, 24, 32, 40, 48 dp (`HitchySpacing`) |
| `radius/sm · md · lg · xl` | 8, 12, 16, 24 dp → Material `small`, `medium`, `large`, `extraLarge` |
| `radius/pill` | fully rounded (`RoundedCornerShape(50)`) |
| Design frame | 393 × 852 |
| Screen side padding | 16 dp |
| Minimum touch target | 48 dp on Android `[DECISION-19]` (Figma says 44 px, the iOS minimum) |
| Icons | 24 dp, 2 dp stroke, outlined |

### 3.4 Icons

Export the icons used in the Figma frames as SVG and convert them to vector resources in `composeResources/drawable/ic_<name>.xml`. Do not add an icon library. Seen in Figma: back, more, bell, mail, lock, person, eye, check, scooter, motorbike, add person, ID card verified, upload, camera, car, clock, search, close, chevron down, location dot, pin, swap, star, shield check, info, warning, error, success, repeat, home, rides, profile. Mirror arrows and chevrons in RTL.

### 3.5 Components

All in `core/ui/components/`, named `Hitchy<Component>`, built only from the tokens above.

| Component | Frame | Variants and states | Used in |
| --- | --- | --- | --- |
| `HitchyButton` | 14:86 | Primary (solid purple), Tonal (lavender + purple text), Outline, Text, Destructive (error-bg + error-text); default, disabled, loading (spinner + label) | Everywhere |
| `HitchyTextField` | 14:86 | Label, placeholder, helper; default, focused (purple border), error (red border + error-text helper), disabled (grey fill) | Profile, vehicle, dispute |
| `PhoneField` | 14:86 | `+20` prefix with divider; helper "We'll send a verification code"; error "Enter a valid Egyptian mobile number" | PhoneEntry |
| `SearchField` | 14:86 | Leading search icon, clear button | Company search |
| `LocationField`, `RouteInput` | 14:86, 14:89 | Pickup (purple dot) and drop-off (pin) with small label, value and clear; `RouteInput` stacks both with a swap button | Search, Post ride |
| `HitchySelect` | 14:86 | Closed, open menu, selected item on lavender with a check | Zone and point pickers |
| `HitchyCheckbox`, `HitchyRadio`, `HitchySwitch` | 14:86 | On, off | Settings, Women-only, filters |
| `TopBar` | 14:87 | Back, title, overflow | Inner screens |
| `GreetingHeader` | 14:87 | "Good morning" + first name + bell | Home |
| `BackButton` | 14:87 | Plain, tonal circle | Flows |
| `HitchyTabs` | 14:87 | Underlined purple active tab | My trips, My rides |
| `FilterChip` | 14:87 | Selected (lavender + check), unselected | Rating tags, quick messages, filters |
| `BottomNav` | 14:87 | 4 items, active purple with underline, profile item shows initials | Home shell (tabs: 3.6 item 6) |
| `Avatar` | 14:88 | 4 sizes (read from Figma), initials on lavender, photo | Host and rider cards |
| `RatingDisplay` | 14:88 | Star + value, optional "· N rides" | Cards, profiles |
| `Badge` | 14:88 | Verified (success), Verification pending (warning) | Host cards, host status |
| `StatusChip` | 14:88 | Dot + label: purple, success, warning, neutral, error | Trips, rides, orders (mapping in 3.6) |
| `SelectChip` | 14:88 | Default, selected (purple border + check) | Date chips ("Today", "Tomorrow") |
| `HitchyBanner` | 14:88 | Success, error, warning, info; icon + text + dismiss | In-screen messages, unpaid fee banner |
| `RouteTimeline` | 14:89 | Time + pickup + drop-off | Ride details, cards |
| `SeatIndicator` | 14:89 | Dots + "Full", "1 seat left", "2 seats left", "3 seats left" (never 4) | Ride cards |
| `DepartureRow` | 14:89 | Clock + time, "Departure · Tomorrow · 07:30 AM" | Ride details |
| `PriceTag` | 14:89 | "EGP 45 / seat" large, "EGP 45 per seat" small | Cards, request sheet |
| `RecurringTag` | 14:89 | "Daily ride · Sun – Thu", days from data | Fixed Ride |
| `TripStepper` | 14:89 | 4 steps, done, current, upcoming | Rider trip screen |
| `HostRow` | 14:89 | Avatar, name, rating, Verified badge | Ride details |
| `RideCard` | 14:89 | Status chip, date and time, route, host avatar and name, car "model · color", price | Search results, My trips, My rides |

Now designed in the app screens (v1.1, see 3.8): 6-box OTP field, tappable rating stars and tag chips, bottom sheet, empty state, error state, splash screen, onboarding page, role card, route picker rows, date sheet, time wheel sheet, passengers stepper, sort and filter sheet, ride result card, seat dots, price summary, action tiles (Message, Call, Share trip, Report, Emergency), tracking sheet with ETA and "Live" chip, receipt card, notification row.

Still not designed. Build them from the tokens, mark them "needs design review" in the gallery, and list them for the designer: price slider with the recommended mark and "Fair price" label, Women-only badge, Same company badge, tier badges (Bronze, Silver, Gold), late fee card, SOS sheet, document upload tile, credits and points balance card, assistant chat bubbles, chat screen and bubbles, owner request card (Accept / Decline with its deadline), Shop page content, small monochrome notification icon.

### 3.6 Figma vs product rules

These conflict with the plan. Until they are fixed in Figma or decided, the code does what the last column says. Rows 3, 4, 6 and 11 were decided in v1.1 ([2.1](#21-design-review-v11)).

| # | Figma shows | Product rule | Code does |
| --- | --- | --- | --- |
| 1 | Seat indicator up to 4 seats | Max 3 seats per car, 1 per scooter (R5) | Up to 3 |
| 2 | "Daily ride · Mon – Fri" | Egypt's work week is Sunday to Thursday | Days from the Fixed Ride data, Sun – Thu by default |
| 3 | Stepper Requested → Confirmed → On the way → Completed (Figma); Owner on the way → Arrived → In ride → Completed (app screens) | Decided in v1.1 (2.1 #1, #11): bookings are requests the host accepts | Requested and Confirmed are status chips. The trip stepper is Owner on the way (ride `started`) → Arrived (host within `arrivedDistanceM` of the pickup) → In ride (booking `boarded`) → Completed |
| 4 | Status chips | Decided in v1.1 | Requested = `requested` (purple); Confirmed = `booked` before the ride starts (info); In progress = ride `started` (warning); Completed = `completed` (success); Cancelled = `cancelled_*` or an `expired` ride (error); Declined, Expired and No show = error chips with those labels. Tabs: Upcoming (Requested, Confirmed, In progress), Completed, Cancelled. Pending stays for host verification and package orders. |
| 5 | Route with a stop in the middle | One pickup and one drop-off per ride | Two points only; middle stops `[LATER]` |
| 6 | Bottom nav Home, Rides, Shop, Profile | Decided in v1.1 (2.1 #9) | Home, Rides, Shop, Profile; Shop holds Rewards (rider) or Packages (host) `[DECISION-18]` |
| 7 | Google button and an "Email or phone number" field with a password eye | The app signs in with phone OTP only | `PhoneField` only. Google sign-in exists only on the admin web. |
| 8 | Pin color token named `color/brand` | — | Code name `pin`; rename to `color/brand/pin` in Figma |
| 9 | Side padding 16 px in the table, "20 px" on the sample | — | 16 dp |
| 10 | Touch target 44 × 44 | Android minimum 48 dp | 48 dp `[DECISION-19]` |
| 11 | Copy uses "driver", "Owner", "ride request", "payment failed" | Decided in v1.1 (2.1 #1, #10): car owners share their own commute; requests exist; no in-app payments | English says "Owner", never "driver"; Arabic says "صاحب العربية" `[DECISION-20]`. "Request" is right now. Never "payment failed". Figma texts are placeholders. |

### 3.7 Tasks

#### P3-A01 · Tokens and theme
- **Files:** `core/ui/theme/Color.kt`, `HitchyColors.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `HitchyTheme.kt`.
- **Spec:** 3.1–3.3. `HitchyTheme { }` provides the Material theme plus `HitchyColors`, `HitchyTypography` and `HitchySpacing`.
- **Done when:** a unit test checks every hex value and type size against the tables above, so changes are deliberate.

#### P3-A02 · Cairo font
- **Spec:** add Cairo Regular, SemiBold and Bold (OFL license file in the repo) and use it for every style.
- **Done when:** previews in English and Arabic render in Cairo.

#### P3-A03 · Icons
- **Spec:** 3.4.
- **Done when:** every icon in the gallery renders at 24 dp and mirrors correctly in RTL.

#### P3-A04 · Components from Figma
- **Spec:** every row in 3.5, one file each, with `@Preview` in English and Arabic and a Roborazzi test per variant and state.
- **Done when:** screenshots match the Figma frames side by side (checked by Islam).

#### P3-A05 · Components not in Figma
- **Spec:** the list under 3.5, built only from tokens, labeled "needs design review" in the gallery.

#### P3-A06 · Gallery
- **Spec:** a `dev`-only gallery screen with every component, variant and state, in English and Arabic.

#### P3-A07 · Swap the screens
- **Spec:** replace placeholder UI feature by feature with these components. ViewModels and state do not change; only `ui/` does. Screen layouts follow Figma screens when they exist; until then `[FIGMA]` applies.

#### P3-W01 · Admin web theme
- **Spec:** Mantine theme with a custom `primary` palette built around `#7C3AED`, font Cairo, default radius 12 px, page background `#F8F9FC`, text `#1E1B4B`. No other design work for the admin.

### 3.8 App designs: screen map and fix list

The design exports (46 PNG files, 393 × 852 frames at 2×, English and LTR only) were reviewed on 2026-10-29. Islam keeps the files; the Figma file stays the source for exact sizes. Each screen below follows its design plus the fix list. Screens not listed keep `[FIGMA]`.

| Design file | Screen (route) | Task |
| --- | --- | --- |
| 01 —splash | Splash (system splash with the logo) | P3-A06, P4-A04 |
| onboarding 10, 9, 8 (pages 1, 2, 3) | Onboarding | P4-A04 |
| Welcome Screen | Welcome (start path and language) | P4-A04 |
| Sign in, Invalid number | PhoneEntry | P4-A04 |
| Verification code, Wrong code | OtpVerify | P4-A04 |
| home, Home · Trip details filled, Home · Validation — destination missing | Home, rider ("Plan your ride") | P7-A02 |
| Route · Pickup active / search, Route · Destination active / search (-1 empty, -2 error), 5 · Combined route picker | RoutePicker | P7-A02 |
| Destination · Choose on map | MapPointPicker (point pins only) | P7-A02 |
| Trip · Select date, Trip · Select departure time | Date and time sheets on Home | P7-A02 |
| Search · Finding rides, Search · Available rides, Search · No rides found, Ride Result Card | SearchResults | P7-A02 |
| Search · Filters & sort | Sort & filter sheet | P7-A02 |
| Ride details | RiderRideDetails before booking, and the request sheet | P7-A03 |
| My Rides, Rides Screen, My rides · Past | MyTrips (Rides tab: Upcoming, Completed, Cancelled) | P7-A04 |
| Upcoming ride details, Cancel booking, Booking cancelled | RiderRideDetails after booking, cancel sheet | P7-A04 |
| Notifications, Notifications · Empty | Notifications (inbox) | P7-A05 |
| Tracking · On the way (-1 Arrived, -2 In ride) | RiderTrip | P8-A04 |
| Upcoming ride details-1 (a completed ride) | RiderRideDetails, completed | P8-A04 |
| Rate Owner · Empty, Rate Owner · Selected, Rating thank you | RateTrip (rider rates the owner) | P8-A06 |
| Fuel Share Receipt | Receipt | P8-A10 |
| shop | Shop (empty state; holds Rewards or Packages) | P9-A01, P10-A01 |
| Toggle | `HitchySwitch` | P3-A04 |

Not designed yet, so still `[FIGMA]`: ProfileSetup, Settings, Profile, every owner screen (Become an owner, HostApplication, HostStatus, AddVehicle, PostRide, MyRides as owner, HostRideDetails with requests, HostTrip, rating riders, Packages, CreditHistory), Fees, Rewards, Invite, Assistant, the Fixed Ride screens, BackupOptions, Chat, EmergencyContact, the SOS sheet, DeleteAccount, Debug, and every Arabic (RTL) version.

**Fix list for the designer.** The code already follows these; the designs should catch up:

1. Words: "Owner" in English everywhere, never "driver" ("Verified driver" → "Verified").
2. Remove "Pay the Hitchy fee to lock it in": riders pay Hitchy nothing. Show "Pay the owner directly by cash or InstaPay" on the request sheet and the receipt.
3. Places are fixed points only: no free addresses ("Street 9, Maadi"), no free map pin; the map shows point pins to tap. Sample places should come from the pilot zones (Fifth Settlement, Rehab, Smart Village).
4. Date sheet: days after today + 7 are disabled; the "For today…" note shows only for today.
5. Ride cards and details: first name only ("Omar", not "Omar Hassan"), a photo or matching initials, and the plan's badges (Fair price, Women-only, tier, Same company, Fixed Ride, vehicle type).
6. Ride details: the plate shows after the owner accepts; the button says "Request ride · EGP {total}"; add the late fee rule (10 EGP after 5 minutes) and the Women-only passenger check when seats > 1.
7. Cancel sheet: reasons Change of plans, Found another ride, Other, plus "Owner is late" when allowed; drop "Owner changed the time" (owners can't change a ride's time); say whether the cancel is free or late (60 minutes).
8. Rider trip screen: add "I'm on my way", "I'm at the pickup point" and "I'm in the car"; Emergency opens the SOS sheet (police 122, ambulance 123, emergency contact).
9. Rating: tags On time, Safe driving, Clean car, Friendly, Followed the route, Late, Unsafe driving, Rude; no comment box; "Submit rating" turns on once stars are picked; the thank-you screen ends with "Done", not "Track ride".
10. Receipt: distance, price per seat, seats, total, "Paid directly to the owner", and "Payment confirmed by {name}" once confirmed. No fuel-share split.
11. Notifications: the texts in 8.15. No "Booking request sent" line; add accepted, declined, expired, payment confirmed and chat.
12. Filters: add "Verified only" and "Women-only" (shown to women only); move "Ride reminders" to Settings.
13. Sign-in note: "Your number is shared only with your owner or riders after a booking is accepted."
14. Welcome: add the language choice (Arabic or English).
15. Fix the word breaks on onboarding ("co mmute", "wit h"), show disabled states for Continue, Verify and Submit, and use Android system bars instead of iOS ones.
16. Make every screen in Arabic (RTL) too.

---

## Phase 4 — Sign-in, profile, roles

**Exit:** on the emulator, a test number signs in, fills the profile, lands on the rider home, and switching to host shows "Become an owner" because the host is not approved.

### Backend

#### P4-B01 · `completeProfile`
- **Spec:** section 8.3.
- **Done when:** integration tests cover: new user gets every default; a second call updates allowed fields only; changing gender → `GENDER_LOCKED`; unknown company → `INVALID_INPUT`; a valid referral code sets `referredBy`; own or unknown code → `referralApplied: false`; `kyc/{uid}` is created.

#### P4-B02 · `switchRole`
- **Done when:** a rider becomes host only with `host.status == "approved"`, else `HOST_NOT_APPROVED`.

#### P4-B03 · `syncPublicProfile`
- **Done when:** a profile appears after user creation, ratings and tiers copy over, and an unrelated user change (for example `workHours`) does not rewrite the profile.

### Android

#### P4-A01 · Phone sign-in (`androidMain`)
- **Files:** `platform/AndroidPhoneAuth.kt`, `platform/ActivityProvider.kt`.
- **Spec:** `PhoneAuthProvider.verifyPhoneNumber` with `PhoneAuthOptions` (60 s timeout, current Activity, callbacks). Callbacks become `PhoneAuthEvent`s. `confirm(code)` signs in with `PhoneAuthProvider.getCredential(verificationId, code)`. Resend uses the `ForceResendingToken`. Map auth errors to client-only reasons `OTP_INVALID`, `OTP_EXPIRED`, `TOO_MANY_ATTEMPTS`, `SMS_QUOTA` (Appendix D).
- **Done when:** sign-in works on the Auth emulator with a test number.

#### P4-A02 · Auth and profile data
- **Files:** `feature/auth/data/AuthRepositoryImpl.kt`, `feature/profile/data/ProfileRepositoryImpl.kt`, domain interfaces.

```kotlin
interface AuthRepository {
    val authState: Flow<AuthState>                    // SignedOut | SignedIn(uid)
    suspend fun signOut()
}
interface ProfileRepository {
    fun observeMe(): Flow<User?>                      // users/{uid} snapshot
    fun observeCompanies(): Flow<List<Company>>
    suspend fun completeProfile(input: CompleteProfileInput): AppResult<CompleteProfileResult>
    suspend fun updateProfile(fields: ProfileFields): AppResult<Unit>   // direct write + updatedAt = server time
    suspend fun switchRole(role: Role): AppResult<Role>
}
```

#### P4-A03 · Root navigation
- **Spec:** `RootViewModel` combines `authState` and `observeMe()` into `Loading | SignedOut | NeedsProfile | Ready(activeRole)`. The NavHost start destination follows it; signing out clears the back stack.
- **Done when:** a unit test covers all four states and the switch between them.

#### P4-A04 · Screens (designed: Onboarding, Welcome, PhoneEntry, OtpVerify, see 3.8; the rest `[FIGMA]`)

| Screen | State | Actions | Notes |
| --- | --- | --- | --- |
| Onboarding | `page` | `Next`, `Skip` | First run only; 3 pages (designs onboarding 10 → 9 → 8) |
| Welcome | `language`, `startPath` (`ride` or `vehicle`) | `PickLanguage`, `PickPath`, `Continue` | First run only (design "Welcome Screen"). The path only decides what opens after ProfileSetup: `vehicle` opens HostApplication (R12) |
| PhoneEntry | `input`, `normalized`, `isValid`, `loading`, `error` | `InputChanged`, `Submit` | `PhoneNumber.normalize`; button enabled only when valid (designs "Sign in", "Invalid number") |
| OtpVerify | `code`, `secondsToResend`, `loading`, `error` | `CodeChanged`, `Resend`, `ChangeNumber` | Auto-submit at 6 digits; `verificationId` in `SavedStateHandle` (designs "Verification code", "Wrong code") |
| ProfileSetup | names, `gender`, `companyQuery`, `companies`, `companyId`, `companyOther`, `workStart`, `workEnd`, `language`, `referralCode`, `submitting`, `error`, `canSubmit` | field changes, `Submit` | Gender note: "can't be changed later" |
| Home (shell) | `activeRole`, `hostStatus` | `SwitchRole` | Tabs for both roles: Home, Rides, Shop, Profile `[DECISION-18]`. Rider Home is "Plan your ride" (design "home", built in P7-A02); owner Home `[FIGMA]` shows today's rides, open requests and Post ride. Shop holds Rewards (rider) or Packages (owner). A not-approved owner sees a "Become an owner" card |
| Settings | language, notification permission, version | `ChangeLanguage`, `SignOut`, `OpenDebug` | Debug opens with a long press on the version in `dev` |

#### P4-A05 · Device tokens
- **Spec:** `DeviceRepository.register(token)` writes `users/{uid}/devices/{installId}` (`fcmToken`, `platform: "android"`, `appVersion`, `language`, `updatedAt` = server time) after sign-in and in `HitchyMessagingService.onNewToken`; `unregister()` deletes it on sign-out.
- **Done when:** the device doc appears after sign-in and disappears after sign-out.

#### P4-A06 · Tests
- ViewModel tests for PhoneEntry, OtpVerify (with a fake `PhoneAuth`: code sent, resend timer, each error) and ProfileSetup (validation, submit, error mapping).

**E2E check:** emulators + seed → sign in with `+201000000007` / `123456` → profile → rider home → switch to host → "Become an owner".

---

## Phase 5 — Host verification and admin

**Exit:** a test host uploads all documents and a car, an admin approves in the admin dashboard, and the host switches to host mode and sees the free month end date.

### Backend

#### P5-B01 · `addVehicle`
- **Done when:** tests cover the vehicle limit, scooter disabled, `maxSeats` from config.

#### P5-B02 · `submitHostApplication`
- **Done when:** tests cover missing files (with the missing list), already submitted, re-submit after rejection only with newer files, admins notified.

#### P5-B03 · `approveHost`, `rejectHost`, `approveVehicle`
- **Done when:** approve sets `freeUntil = approval + 30 days`; reject marks only the listed docs; non-admins get `PERMISSION_DENIED`; every call writes `adminActions`.

#### P5-B04 · `setVerified`, `setGender`, `requestVerification`
- **Spec:** `requestVerification` (callable, host, input `{}`) lets an approved host ask for Verified later: `kyc/{uid}/criminal_record.jpg` exists → else `KYC_MISSING_FILES`; sets `kyc.docs.criminalRecord.status: "uploaded"`; `sendToAdmins("verify_request")` (English "A host asked for Verified", Arabic "صاحب عربية طالب علامة Verified", deep link `{admin}/hosts`).
- **Done when:** tests cover each function and the admin-only checks.

#### P5-B05 · Admin accounts and config
- **Spec:** `set-admin` gains `--email <email>` (finds the user by email) and `--notify-uid <uid>` (adds an app account to `config/admins.notifyUids`). `sendToAdmins` sends to both lists. Add the zod schemas for every config doc in section 7 to `schemas/config.ts` (its TypeScript types exist since P1-B02; derive them from the schemas) and the `updateConfig` callable (section 8.4).
- **Done when:** tests cover both script options, both notification lists, and `updateConfig` refusing an invalid value and bumping `version`.

### Android

#### P5-A01 · Host data

```kotlin
interface HostRepository {
    fun observeKyc(): Flow<Kyc?>
    fun observeVehicles(): Flow<List<Vehicle>>
    suspend fun addVehicle(input: AddVehicleInput): AppResult<String>
    fun uploadDoc(key: KycDocKey, jpeg: ByteArray): Flow<UploadProgress>   // Storage kyc/{uid}/{file}
    suspend fun submit(vehicleId: String): AppResult<Unit>
    suspend fun requestVerification(): AppResult<Unit>
}
```

#### P5-A02 · Host application wizard `[FIGMA]`
- **Steps:** Intro (what we need and why) → ID front → ID back → Driving license → Vehicle license → Vehicle details → Criminal record (optional, explains the Verified benefits) → Review → Submitted.
- **State:** `step`, `docs: Map<KycDocKey, DocUi>` (`Missing | Uploading(progress) | Uploaded | Failed | Rejected(reason)`), vehicle form fields, `canNext`, `submitting`, `error`.
- **Photos:** camera or Photo Picker → `ImageCompressor.toJpeg` (long side 1600 px, quality 80; re-encoding drops all EXIF, including GPS) → upload with progress and retry.
- **After a rejection:** only rejected documents can be replaced; the rest are shown as accepted.

#### P5-A03 · Host status screen `[FIGMA]`
Pending, approved (with "Free until {date}"), or rejected (reason per document and a "Fix" button into the wizard). A "Get Verified" card for approved hosts without the badge.

#### P5-A04 · Admin screens
- Moved to the admin web (tasks `P5-W01`–`P5-W06`). The Android app has no admin code.

#### P5-A05 · Profile photo
- Pick, compress to 512 px, upload to `avatars/{uid}.jpg`, write `photoPath`.

#### P5-A06 · Tests
- Wizard ViewModel: step rules, rejected-only editing; compressor output under 5 MB and without EXIF (Android host test with a sample JPEG).

### Admin web

#### P5-W01 · Scaffold and hosting
- **Files:** `admin-web/` as in [4.5](#45-admin-web), `.github/workflows/admin-web.yml`, `backend/firebase.json`, `backend/.firebaserc`.
- **Spec:**
  - Vite + React + TypeScript (strict). Path alias `@schemas/*` → `../backend/functions/src/schemas/*` in `tsconfig.json` and `vite.config.ts` (allow that folder in `server.fs.allow`).
  - Scripts: `dev` (emulators), `build:dev`, `build:prod`, `lint`, `typecheck`, `test` (Vitest), `e2e` (Playwright inside `firebase emulators:exec`).
  - `firebase/init.ts`: web config per mode (not secret); `VITE_USE_EMULATOR=true` connects Auth 9099, Firestore 8080, Functions 5001, Storage 9199; functions region `[DECISION-01]`; App Check with reCAPTCHA Enterprise in prod, debug token in dev.
  - `firebase.json` hosting becomes two targets: `site` (`hosting/public`, the deletion page) and `admin` (`../admin-web/dist`, SPA rewrite to `/index.html`, the headers from 4.5).
  - CI `admin-web.yml`: on pull requests touching `admin-web/**` or `backend/functions/src/schemas/**`: `npm ci`, lint, typecheck, test, `build:prod`. Push to `main`: deploy `hosting:admin` to `hitchy-dev`. Tag `admin-v*`: deploy to `hitchy-prod` (environment approval).
  - `[HUMAN]` Create the `admin` Hosting site in both projects and run `firebase target:apply hosting admin <site-id>`; reCAPTCHA Enterprise key for App Check `[DECISION-16]`.
- **Done when:** `npm run build:prod` builds and `npm run dev` against the emulators shows the login page.

#### P5-W02 · Admin sign-in
- **Spec:** Google sign-in (popup) with session-only persistence (closing the browser signs out). After sign-in, `getIdTokenResult(true)`; without the `admin` claim show NotAuthorized with Sign out. `RequireAdmin` wraps every route except `/login`. `[HUMAN]` turn on the Google provider in both projects, add the dashboard domain to Authorized domains, and use Google accounts with 2-step verification. `[LATER]` an Auth blocking function that allows only listed admin emails.
- **Done when:** Playwright: the seeded admin gets in; another Google account sees NotAuthorized.

#### P5-W03 · Layout and data hooks
- **Spec:** sidebar (Dashboard, Hosts, Vehicles, Orders, Disputes, Reports, SOS, Users, Config, Audit; pages appear as their phase lands), top bar with the environment badge and the admin's email.
  - `useDoc(ref)` and `useCollection(query)` over `onSnapshot`, with loading and error.
  - `useCount(query)` over `getCountFromServer`, refreshed every 60 seconds.
  - `useCallable(name)` returns `{ call, loading }`; on error it shows the English message for `details.reason` (Appendix D), on success a short toast.
  - `DataTable` pages with `limit` + `startAfter`, 25 rows per page. Times shown in Cairo time.
- **Done when:** unit tests cover the `useCallable` error mapping and `DataTable` paging.

#### P5-W04 · Dashboard, first version
- **Spec:** count cards that link to their pages: host applications waiting (`kyc.status == "submitted"`) and vehicles waiting (collection group `vehicles`, `status == "pending"`). Later phases add their counts.

#### P5-W05 · Hosts queue and review
- **Spec:**
  - `/hosts`: `kyc` where `status == "submitted"`, oldest first: submitted time, first name, vehicle, waiting time.
  - `/hosts/:uid`: user summary (name, phone, gender, workplace), each document image from Storage (`getDownloadURL`, viewer with zoom and rotate, no download button), the status of each document, the vehicle, and this user's `adminActions` history.
  - Actions, each with a confirm dialog: Approve (pick the vehicle) → `approveHost`; Reject (reason per document + note) → `rejectHost`; Set Verified → `setVerified`; Set gender → `setGender`.
  - `/vehicles`: second vehicles waiting → `approveVehicle`.
- **Done when:** Playwright: a seeded application is approved in the browser and the user's `host.status` becomes `approved` in the emulator.

#### P5-W06 · Web push for admins
- **Spec:** `firebase-messaging-sw.js`; after sign-in ask for browser notification permission, `getToken(messaging, { vapidKey })`, write `users/{uid}/devices/{installId}` with `platform: "web"` and `language: "en"`. Foreground messages show a Mantine notification; `sos` also plays a sound. `[HUMAN]` create the Web Push certificate (VAPID key) in the Firebase console.
- **Done when:** a test message to the admin's token appears in the browser.

**E2E check:** `host-new@test` uploads 4 photos and a car → the admin approves in the dashboard → the host switches to host mode and sees "Free until".

---

## Phase 6 — Posting rides and pricing

**Exit:** an approved host posts a peak ride with 3 seats at 56 EGP; 70 EGP is refused with `PRICE_OUT_OF_RANGE`; a third ride the same day is refused with `DAILY_RIDE_LIMIT`.

### Backend

#### P6-B01 · `quotePrice`
- **Done when:** results match the golden table through the callable; unknown route → `ROUTE_NOT_SUPPORTED`; too many seats → `SEATS_INVALID`.

#### P6-B02 · `createRide`
- **Done when:** one integration test per check (10), plus: the same `requestId` twice returns the same `rideId` and creates one ride; the ride copies vehicle, point names and coordinates, and the price range.

#### P6-B03 · `cancelRide`
- **Done when:** bookings become `cancelled_by_host`; `lateCancels` grows only when cancelling within 60 minutes of departure with bookings; a started ride → `RIDE_HAS_STARTED`.

### Android

#### P6-A01 · Rides data

```kotlin
interface PlacesRepository {
    fun observeZones(): Flow<List<Zone>>
    fun observePoints(zoneId: String): Flow<List<Point>>
}
interface RidesRepository {
    suspend fun quote(input: QuoteInput): AppResult<Quote>
    suspend fun create(input: CreateRideInput): AppResult<String>     // requestId made once per form submit
    suspend fun cancel(rideId: String, reason: CancelReason): AppResult<Int>
    fun observeMyRides(): Flow<List<Ride>>        // rides where hostId == uid, newest 50
    fun observeRide(rideId: String): Flow<Ride?>
    fun observeBookings(rideId: String): Flow<List<Booking>>
}
```

#### P6-A02 · Post ride `[FIGMA]`
- **State:** `vehicles`, `vehicleId`, `direction`, `fromZone`, `fromPointId`, `toZone`, `toPointId`, `date`, `time` (5-minute steps, Cairo), `seats` (1…`maxSeats`), `womenOnlyVisible` (woman host), `womenOnly`, `quote`, `price`, `fairPrice`, `quoteLoading`, `posting`, `error`, `canPost`.
- **Behavior:** direction defaults to `to_work` before 12:00 and `to_home` after. Zones default to the last ones used (saved in `AppSettings`). `quotePrice` runs 300 ms after the last change of vehicle, route, date, time or seats; the slider runs from `min` to `max`, starts at `recommended`, and shows "Fair price" at or below `recommended` plus a short breakdown (fuel + car costs ÷ seats). Offline → a clear message, no call.
- **After posting:** open HostRideDetails. "Post the same ride tomorrow" pre-fills the form from an existing ride.

#### P6-A03 · My rides and host ride details `[FIGMA]`
- My rides: upcoming and past.
- Details: time, points, price, status, booked riders (first name, photo, rating, seats), Cancel (reason dialog). Requests with Accept / Decline come in Phase 7 (P7-A07); Start trip comes in Phase 8.

#### P6-A04 · Tests
- Post-ride ViewModel: debounce, slider limits, Women-only visibility, `requestId` reused on retry, every error reason shows its message.

### Admin web

#### P6-W01 · Config page
- **Spec:** `/config` shows each `config/*` doc with its `version`. Edit opens `JsonConfigEditor`: the JSON is checked with the same zod schema from `schemas/config.ts` as you type, a diff against the current value is shown, and Save (with confirm) calls `updateConfig`. Read-only for everything not in `updateConfig`.
- **Done when:** Playwright: changing `fuelPriceEgpPerLiter` bumps the version and a new `quotePrice` uses the new price after the cache expires.

**E2E check:** `host@test` posts Fifth Settlement → Smart Village tomorrow 08:00 with 3 seats at 56 EGP → it shows in My rides; 70 EGP and a third ride are refused.

---

## Phase 7 — Search, booking, notifications

**Exit:** two riders race for the last seat and exactly one request holds it; the host accepts it from the notification; declined, expired and cancelled requests free their seats; Women-only rides never reach men; the inbox lists every notification.

### Backend

#### P7-B01 · `searchRides` and ranking
- **Done when:** unit tests for `ranking.ts` (each factor moves the order as expected, ties by time); integration tests for every filter in 8.6 step 2, each sort, `seats`, `walkM` and `bestMatch`; results never contain phone numbers or plates.

#### P7-B02 · `requestSeats`, `respondToRequest`, `expireRequest`
- **Spec:** 8.6 (v1.1 replaces `bookSeat`).
- **Done when:** one test per `requestSeats` check (9); two parallel requests for the last seat leave exactly one `requested` booking (`Promise.all` against the emulator); the same `requestId` twice requests once; seats are held while `requested`; accept moves to `booked` and enqueues the reminder once; decline and expiry return the seats and `full → open`; a late answer gets `REQUEST_EXPIRED`; an answered request gets `REQUEST_NOT_PENDING`.

#### P7-B03 · `cancelBooking`
- **Done when:** all the booking's seats return and `full → open`; cancelling a request is always free; late cancel is flagged only for accepted bookings inside `freeCancelMinutes`; `host_late` works only after `hostLateMinutes` with the host far from the pickup point.

#### P7-B04 · `onBookingChanged` and `lib/notify.ts`
- **Spec:** send **data-only** FCM messages with Android priority high: `{ type, title, body, deepLink, channel }`, text already localized for the device. The app builds the notification itself, so taps always follow the deep link. Every non-admin message is also written to `users/{uid}/notifications` with both languages and `expireAt` (v1.1, `[DECISION-27]`).
- **Done when:** tests with a fake messaging client check who gets what for each change, and that unregistered tokens are deleted.

#### P7-B05 · `rideReminder`
- **Spec:** `onTaskDispatched` with `retryConfig.maxAttempts: 3`. Enqueued from `respondToRequest` on the ride's first accept, with `getFunctions().taskQueue("locations/{REGION}/functions/rideReminder").enqueue({ rideId }, { scheduleTime, id: "reminder-{rideId}" })`.
- **Done when:** a test calls the task handler directly and checks the notifications; cancelled rides send nothing.

#### P7-B06 · Saved places and the inbox in the rules
- **Spec:** `savedPlaces` in `OWNER_WRITABLE_USER_FIELDS` (6.2) and the inbox rules (9.1, 9.2); `completeProfile` creates `savedPlaces: []`.
- **Done when:** rules tests: the owner can save up to `maxSavedPlaces` places and can set only `readAt` on their own inbox items; nobody else can read either.

### Android

#### P7-A01 · Booking data

```kotlin
interface BookingRepository {
    suspend fun search(input: SearchInput): AppResult<List<RideCard>>
    suspend fun request(rideId: String, seats: Int, requestId: String): AppResult<RequestResult>
    suspend fun respond(rideId: String, riderUid: String, accept: Boolean): AppResult<BookingStatus>  // host
    suspend fun cancel(rideId: String, reason: CancelBookingReason): AppResult<Boolean>   // lateCancel
    fun observeMyTrips(): Flow<List<Booking>>     // collection group bookings, riderUid == uid, newest 50
    fun observeBooking(rideId: String): Flow<Booking?>
}
interface SavedPlacesRepository {                 // feature/places
    fun observe(): Flow<List<SavedPlace>>         // users/{uid}.savedPlaces
    suspend fun save(places: List<SavedPlace>): AppResult<Unit>
    fun recent(): Flow<List<PointRef>>            // last points used, kept on the device (DataStore)
}
interface InboxRepository {                       // feature/inbox
    fun observe(): Flow<List<InboxItem>>          // users/{uid}/notifications, newest first
    suspend fun markRead(ids: List<String>): AppResult<Unit>
}
```

#### P7-A02 · Home and search (designed, see 3.8)
- **Home "Plan your ride":** map on top, a sheet with From, To, Date, Departure time, Passengers (1 … 3) and "Search rides". Search with no destination shows the red banner and marks To (design "Home · Validation").
- **RoutePicker:** From and To on one screen with a swap button. Search matches point names in both languages and lists only active points (name plus zone); "Use current location" picks the nearest point (asks for location permission first); Saved places (Home, Work, other) and Recent; "No places found" and "Couldn't load suggestions" states. "Choose on map" opens MapPointPicker, which shows the zone's point pins only; tap a pin, then Confirm.
- **Date and time sheets:** Today and Tomorrow chips plus a calendar up to `maxDaysAhead`; a time wheel in 15-minute steps. Times before now are disabled for today.
- **State:** `fromPoint`, `toPoint`, `date`, `time`, `seats`, `sort`, `timeBand`, `maxPrice`, `minRating`, `verifiedOnly`, `womenOnly` (shown to women only), `results`, `loading`, `error`.
- **Results:** "Finding rides" loading, then "Available rides" with a route summary, "N rides match your trip · Prices are per seat" and the ride cards (time → arrival, seat dots, "N seats left", pickup · walk, drop-off, owner first name, rating · trips, badges, price per seat, "Best match" on the first under Recommended), or "No rides found" with "Change date or time", "Edit pickup or destination" and "Search again".
- **Sort & filter sheet:** Recommended, Earliest, Cheapest; Any time, Before {time}, {time}–{time + 1 h}, After; max price slider; owner rating Any, 4.5+, 4.8+; Verified only; Women-only; Reset and Show results.
- Points and dates pre-fill from the last search. "Ask the assistant" on the empty state stays hidden while `assistantEnabled` is false (Phase 11).

#### P7-A03 · Ride details and the request (designed, see 3.8)
- Details (design "Ride details"): map with the route, owner card (first name, photo or initials, rating · trips, Verified, tier, Same company; car make, model, year and color, plate only after acceptance), pickup and drop-off with the walk distance, date, time → arrival, seats left with the passengers stepper (1 … seats left), price per seat × seats = total, "Open in Maps" for the pickup point (coordinates from `zones/{fromZone}/points/{fromPointId}`, opened with a `geo:` intent).
- The button "Request ride · EGP {total}" opens a short confirm: "Pay the owner directly by cash or InstaPay", the late fee rule with values from `config/limits`, and for Women-only rides with seats > 1 "Everyone on this booking is a woman" `[DECISION-24]`. The `requestId` is made when the sheet opens and reused on retry.
- After the request: status "Requested" with the time left to answer (`expiresAt`), Cancel request. When accepted: car make, model, color and plate, pickup point, time. `RIDE_FULL` → close the sheet, refresh the list, show the message.

#### P7-A04 · Rides tab and booking details (designed, see 3.8)
- Rides tab "My rides" with tabs Upcoming (Requested, Confirmed, In progress), Completed, Cancelled (Cancelled, Declined, Expired, No show). Cards: status chip, date and time, pickup and drop-off, owner first name and photo, car model and color, total.
- Upcoming ride details (design "Upcoming ride details"): status, route, owner card with Message (opens Chat) and Call (`getContactPhone`, then the dialer) once accepted, vehicle and plate, seats, total, "Track ride" (enabled once the ride starts), "Cancel booking".
- Cancel sheet: reasons Change of plans, Found another ride, Other, and "Owner is late" only after `departAt + hostLateMinutes`; it says whether the cancel is free (`now < departAt - freeCancelMinutes`, always free for a request). Then "Booking cancelled" with "Find another ride".

#### P7-A05 · Notifications
- **Spec:** create channels at start (`bookings`, `trips`, `account`, `admin`) with names in both languages. `HitchyMessagingService.onMessageReceived` builds the notification from the data payload with a `PendingIntent` to `MainActivity` carrying the deep link; `MainActivity` passes it to `DeepLinks.parse`.
- Ask for the notification permission after the first request or the first posted ride, not at first launch.
- Inbox screen (designs "Notifications", "Notifications · Empty"): bell on Home with an unread count; Today and Earlier; unread rows tinted with a dot; "Mark all as read"; tap follows the deep link; empty state "You're all caught up" with "Plan a ride".
- **Done when:** a data message from the emulator (or a test push) opens the right screen, and it shows in the inbox in both languages.

#### P7-A06 · Tests
- Search ViewModel (points only, filters, sort, seats, empty state), request sheet ViewModel (`requestId` reuse, `RIDE_FULL`, Women-only check), Rides tab cancel rules, inbox mark-read, notification deep-link routing.

#### P7-A07 · Owner: requests `[FIGMA]`
- On HostRideDetails (and the owner Home): each request with the rider's first name, photo, rating, seats, total and the time left; Accept and Decline call `respondToRequest`. `booking_requested` opens this list.
- **Done when:** a ViewModel test covers accept, decline, `REQUEST_EXPIRED` and `REQUEST_NOT_PENDING`.

**E2E check:** `rider@test` requests 2 seats and the host accepts; `rider-w@test` sees and requests a Women-only ride that `rider@test` never sees; the host gets `booking_requested`, the rider `booking_accepted`; the rider cancels and both seats come back; a request left unanswered expires.

---

## Phase 8 — Trip day

**Exit:** two devices run a full trip (start, check-in, boarding, finish, payment confirmed, both ratings) and can chat and call; a late report creates a fee that blocks the rider's next request until the host marks it paid; after the free month one credit is used per trip with riders.

### Backend

| Task | Function(s) | Done when |
| --- | --- | --- |
| P8-B01 | `startRide` | start window enforced; riders notified |
| P8-B02 | `checkIn` | server time and distance stored; closed after `noShowMinutes` |
| P8-B03 | `markBoarded` | host for anyone, rider for self only; ride must be started |
| P8-B04 | `reportLate`, `disputeFee`, `resolveFee`, `markFeePaid` | grace, on-time check, one fee per booking, `unpaidFees` counter always right, disputed fee still blocks booking |
| P8-B05 | `sendChatMessage`, `getContactPhone` | only the host and riders with an accepted booking; chat closes `chatOpenHoursAfterRide` after the ride; the text is never logged and the push never carries it; a phone only while the ride is open, full or started |
| P8-B06 | `finishRide`, `onRideFinished`, `closeStaleRides`, `domain/together.ts` | exactly one credit even if called twice; none in the free month or without boarded riders; `verifiedTogether` true for matching sample sets and false for far ones; `postProcessed` prevents double work; stale and expired rides closed |
| P8-B07 | `submitRating` | allowed pairs only; once per pair; 7-day window; averages correct; low ratings create a report |
| P8-B08 | `confirmSeatPayment` | host only; `boarded` or `completed` bookings; once per booking (`PAYMENT_ALREADY_CONFIRMED`); the rider gets `payment_confirmed` |

Rewards and impact steps in `onRideFinished` are empty functions until Phases 10 and 14.

### Android

#### P8-A01 · Trip data

```kotlin
interface TripRepository {
    suspend fun start(rideId: String): AppResult<Unit>
    suspend fun checkIn(rideId: String, at: GeoPoint, accuracyM: Double): AppResult<Int>
    suspend fun markBoarded(rideId: String, riderUid: String): AppResult<Unit>
    suspend fun reportLate(rideId: String, riderUid: String, kind: LateKind): AppResult<String>
    suspend fun finish(rideId: String): AppResult<FinishResult>
    suspend fun confirmPayment(rideId: String, riderUid: String): AppResult<Unit>       // host
    suspend fun contactPhone(rideId: String, otherUid: String): AppResult<String>
    fun observePresence(rideId: String, uid: String): Flow<Presence?>
    suspend fun writePresence(rideId: String, last: Sample, samples: List<Sample>)   // whole doc, max 200 samples
}
interface FeesRepository {
    fun observeAsRider(): Flow<List<Fee>>
    fun observeAsHost(): Flow<List<Fee>>
    suspend fun dispute(feeId: String, text: String): AppResult<Unit>
    suspend fun markPaid(feeId: String): AppResult<Unit>
}
interface RatingsRepository { suspend fun submit(input: RatingInput): AppResult<Unit> }
interface ChatRepository {                        // feature/chat
    fun observe(rideId: String, riderUid: String): Flow<List<ChatMessage>>
    suspend fun send(rideId: String, riderUid: String, text: String, requestId: String): AppResult<String>
}
```

#### P8-A02 · Trip location service (`app` module)
- **Manifest:** `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`; `<service android:name=".location.TripLocationService" android:foregroundServiceType="location" android:exported="false"/>`. Never request `ACCESS_BACKGROUND_LOCATION`.
- **Spec:** `TripTracker.start(rideId)` starts the service only from a user action while the app is visible (host: Start trip; rider: "I'm on my way"). It calls `startForeground` with type location and a "Trip in progress" notification, takes a Fused Location update every `presenceIntervalSec` (balanced power), keeps the newest 200 samples in memory and writes the whole presence doc each time. It stops when the ride is `finished`, `cancelled` or `expired`, after `staleRideHours`, or when the user taps Stop.
- **Done when:** on a device, presence updates every minute during a trip and the service stops at finish.

#### P8-A03 · Host trip screen `[FIGMA]`
- **State:** ride, countdown, `canStart`, riders with status (`booked`, `checked in`, `boarded`, `late`, `no show`), host location sharing on or off.
- **Actions:** Start trip (asks for location permission with a short reason first), Boarded (per rider), Rider late, No show (after `noShowMinutes`), Chat and Call (per rider), Finish (confirm dialog), then Mark paid per rider (`confirmSeatPayment`). Share trip and SOS buttons come in Phase 13.

#### P8-A04 · Rider trip screen (designed, see 3.8)
- **Layout (designs "Tracking · On the way", -1, -2):** map with the route, the owner's car from host presence and the drop-off pin; a sheet with the status ("On the way", "Arrived", "In ride"), the ETA, a "Live" chip, the stepper Owner on the way → Arrived → In ride → Completed, the owner and car card with plate, and tiles Share trip, Report, Emergency.
- **State:** ride, own booking, host presence, countdown, step. Arrived = the host is within `arrivedDistanceM` of the pickup point; ETA "Arrives in N min" = straight-line distance at `etaSpeedKmh`, "N min to destination" = route minutes left after boarding `[DECISION-26]`.
- **Actions:** "I'm on my way" (starts the tracker), "I'm at the pickup point" (`checkIn` with the current location), "I'm in the car" (`markBoarded` for self), Chat and Call, Cancel (free "Owner is late" option when allowed). Share trip and Emergency (SOS) are built in Phase 13.
- After the ride: the completed details (design "Upcoming ride details-1") with the stepper done, "Payment confirmed by {name}" once confirmed, "Rate your owner" and "Book again" (opens Search with the same points and time).

#### P8-A05 · Fees `[FIGMA]`
- Rider: unpaid and disputed fees, "Pay the owner directly", Dispute (text up to 300 characters). A banner on Search and the request sheet while a fee is unpaid.
- Host: fees to confirm with "Mark as paid".

#### P8-A06 · Rating (rider side designed, see 3.8)
- Rider rates the owner (designs "Rate Owner · Empty / Selected", "Rating thank you"): owner photo, name and car, stars, tag chips from `RATING_TAGS.host`, "Report a problem" (`reportUser`), "Submit rating" enabled once stars are picked; no comment box. The thank-you screen ends with "Done".
- Owner rates riders `[FIGMA]`: one card per completed rider with `RATING_TAGS.rider`; Skip.

#### P8-W01 · Disputes page (admin web)
- `/disputes`: fees with `status == "disputed"`, oldest first, with the ride time, check-in time and distance, report time, the rider's text, and both users' fee history. Keep or Waive with a note → `resolveFee`. The dashboard adds the disputes count.

#### P8-A08 · Tests
- Host and rider trip ViewModels (button availability by time and status, Arrived and ETA), presence trimming to 200, fee banner logic, rating validation, chat send and closed state, payment confirmation.

#### P8-A09 · Chat `[FIGMA]`
- One thread per booking (`rides/{rideId}/chats/{riderUid}/messages`), newest at the bottom; the old quick messages as one-tap chips above the input (500 characters); Call in the top bar; "Chat closed" once `CHAT_CLOSED`. `chat_message` opens it.

#### P8-A10 · Receipt (designed, see 3.8)
- Design "Fuel Share Receipt", with the plan's numbers: route distance, price per seat, seats, total, "Paid directly to the owner", and "Payment confirmed by {name}" when `paymentConfirmedAt` is set. `payment_confirmed` opens it.

**E2E check:** see Appendix C, steps 10–19.

---

## Phase 9 — Packages and credits

**Exit:** a host requests a package, an admin confirms it, the balance updates live, and the next trip with riders after the free month uses one credit.

| Task | Spec | Done when |
| --- | --- | --- |
| P9-B01 `requestPackage` | 8.8 | unique reference; pending limit; not-approved host refused |
| P9-B02 `confirmPackageOrder` | 8.8 | confirm twice grants once; reject grants nothing; ledger entry written |
| P9-B03 `notifyFreeMonthEnding`, low balance | 8.8, `onRideFinished` step 6 | each host notified once |

**Android**

```kotlin
interface PackagesRepository {
    fun observePackages(): Flow<List<PackageItem>>      // config/packages
    fun observeBalance(): Flow<Credits>                 // users/{uid}.host.credits + freeUntil
    fun observeOrders(): Flow<List<PackageOrder>>
    fun observeLedger(): Flow<List<LedgerEntry>>        // newest 100
    suspend fun request(packageId: String): AppResult<PaymentInstructions>
}
```

- **P9-A01 Packages screen `[FIGMA]`, in the Shop tab for owners:** both balances, "Free until {date}" banner, package cards, Buy → PaymentInstructions (amount, InstaPay address and reference with copy buttons, "I've sent it" → back to the list with the order pending), orders with status, Credit history.
- **P9-A02:** `NO_CREDITS` on Post ride shows "Buy a package" with a button to Packages.
- **P9-W01 Orders page (admin web):** `/orders`: pending orders oldest first (reference, amount, package, host first name, created time) with a reference search box; Confirm or Reject with a note → `confirmPackageOrder`. The dashboard adds the pending orders count.

**E2E:** Appendix C steps 20–21.

---

## Phase 10 — Hitchy Rewards

**Exit:** with `rewardsEnabled`, both sides get points after a verified trip, tiers update nightly, and a host turns 300 points into 3 credits.

| Task | Spec | Done when |
| --- | --- | --- |
| P10-B01 `repos/points.ts` + trip and five-star points | 8.9 | points only with `verifiedTogether`; host multiplier; pair limit per day |
| P10-B02 Referral | 8.9 | +100 to both exactly once at the 5th trip |
| P10-B03 `weeklyRewards` | 8.9 | once per week per user; no bonus after a fee or late cancel |
| P10-B04 `computeTiers` | 8.9 | exact boundaries (20 trips with 4.5 = Silver) |
| P10-B05 `expirePoints` | 8.9 | oldest first; balance always equals the sum of entries |
| P10-B06 `redeemPoints` | 8.9 | spends oldest first; adds credits and a ledger entry |

**Android**

```kotlin
interface RewardsRepository {
    fun observeBalance(): Flow<Int>
    fun observeHistory(): Flow<List<PointsEntry>>       // newest 100
    suspend fun redeem(bundles: Int): AppResult<RedeemResult>
}
```

- **P10-A01 Rewards screen `[FIGMA]`, in the Shop tab for riders:** balance, tier per role and the next tier's rule (trips and rating), how to earn (values from `config/rewards`), history with expiry dates, Redeem for hosts (enabled from 300).
- **P10-A02 Invite `[FIGMA]`:** referral code with the share sheet. No Firebase Dynamic Links (shut down). The code is typed at sign-up.
- **P10-A03:** tier badges on ride cards and profiles. Everything hidden while `rewardsEnabled` is false.

**E2E:** Appendix C step 22.

---

## Phase 11 — AI assistant

**Exit:** on `dev`, "عايز أروح القرية الذكية بكرة الساعة 8" returns real seeded rides, Book works from the chat, and the daily limit shows its message.

| Task | Spec | Done when |
| --- | --- | --- |
| P11-B01 `LlmClient`, fake, one provider adapter `[DECISION-12]` | 8.10 | the fake drives tool calls in tests; the adapter reads its key with `defineSecret` |
| P11-B02 `askAssistant` | 8.10 | runs search as the caller; only tool results become cards; daily limit; budget guard; no message text in logs |
| P11-B03 Eval set | `test/ai/requests.json`: about 30 Egyptian requests with the expected tool arguments; `npm run ai:eval` runs them against the real provider (manual, costs money) | ≥ 90% of requests produce the expected zones and a window that contains the asked time |

**Android**

```kotlin
interface AssistantRepository {
    suspend fun ask(message: String, history: List<ChatTurn>): AppResult<AssistantReply>
}
```

- **P11-A01 Assistant screen `[FIGMA]`:** messages, input (500 characters), 3 suggestion chips, typing indicator, ride cards with Request (opens the Phase 7 request sheet), "Search instead" on `AI_DAILY_LIMIT` and `AI_UNAVAILABLE`. The ViewModel keeps the chat in `SavedStateHandle` and sends the last 6 turns. Hidden while `assistantEnabled` is false.

**E2E:** Appendix C step 23.

---

## Phase 12 — Fixed Ride and backup rides

**Exit:** a host creates a Fixed Ride, two riders join with the code, tomorrow's ride appears with both booked, a skip frees a seat, and a host cancel sends backup options.

| Task | Spec | Done when |
| --- | --- | --- |
| P12-B01 `createFixedRide`, `joinFixedRide`, `leaveFixedRide`, `setFixedRideStatus` | 8.11 | every check has a test |
| P12-B02 `skipFixedDay`, `generateFixedRides` | 8.11 | one ride per day even when run twice; skips, cutoff, credits, conflicts and unpaid fees handled |
| P12-B03 `backupAlert`, `getBackupOptions`, hooks in `cancelRide` and `cancelBooking` | 8.11 | only affected riders; Silver and Gold first |
| P12-B04 Fixed week bonus | 8.9 `weeklyRewards` | full weeks only |

**Android**

```kotlin
interface FixedRepository {
    fun observeMine(): Flow<List<FixedRide>>            // as host (hostId) and as member (riderUids)
    suspend fun create(input: CreateFixedRideInput): AppResult<CreatedFixedRide>
    suspend fun join(code: String): AppResult<String>
    suspend fun leave(fixedRideId: String, riderUid: String?): AppResult<Unit>
    suspend fun setStatus(fixedRideId: String, status: FixedStatus): AppResult<Unit>
    suspend fun skip(fixedRideId: String, dateKey: String): AppResult<Unit>
    suspend fun backupOptions(cancelledRideId: String): AppResult<List<RideCard>>
}
```

- **P12-A01 Screens `[FIGMA]`:** Fixed rides list; Create (Post ride form + weekdays + start date); Details (next 2 weeks with skips, members, invite code with share, pause, end, skip a day, leave); Join with code; Backup options (ride cards + request sheet). Hidden while `fixedRideEnabled` is false.

**E2E:** Appendix C steps 24–25.

---

## Phase 13 — Safety, privacy, account deletion

**Exit:** SOS opens the dialer with 122 and alerts admins; share sends the trip text; account deletion leaves no personal data; the deletion web page is live.

| Task | Spec | Done when |
| --- | --- | --- |
| P13-B01 `reportSafetyEvent`, `reportUser`, `blockUser`, `unblockUser` | 8.12 | works while suspended (SOS); blocks hide both ways in search |
| P13-B02 `setSuspended`, `closeReport`, `handleSafetyEvent` | 8.12 | suspending cancels open rides and bookings |
| P13-B03 `deleteAccount` | 8.12 | integration test: no doc or file with the user's personal data remains; anonymized records stay; Auth user deleted; safe to re-run |
| P13-B04 `purgeKycPhotos` | 8.12 `[DECISION-14]` | only reviewed documents older than the retention |
| P13-B05 Deletion web page | `backend/hosting/public/delete-account.html`, Arabic and English: how to delete in the app, and how to ask by email `[HUMAN: support email]` | deployed on Hosting at a fixed URL |

**Android**

- **P13-A01 Share trip:** share sheet text with host first name, car make, model, color and plate, route names, time, and `https://maps.google.com/?q={lat},{lng}` of the current location.
- **P13-A02 SOS `[FIGMA]`:** on both trip screens; confirm sheet with police 122, ambulance 123 and the emergency contact; `ACTION_DIAL` (no call permission); SMS intent with the location link; then `reportSafetyEvent`.
- **P13-A03:** Emergency contact screen; Report and Block from ride details and the rating sheet.
- **P13-A04 Delete account `[FIGMA]`:** explain what is deleted → fresh OTP (re-run `PhoneAuth`) → `deleteAccount` → sign out. `ACTIVE_TRIP_SOON` explains what to cancel.
- `[HUMAN]` Privacy policy and the Google Play Data safety form, matching what the app collects: phone, name, gender, workplace, ID photos, location during trips.

**Admin web**

- **P13-W01 Reports and SOS:** `/reports` (open reports including low ratings, links to the ride and both users; Close with a note → `closeReport`; Suspend → `setSuspended`). `/sos` (live list, unhandled on top with a sound, ride and people details, pickup map link; Mark handled → `handleSafetyEvent`). The dashboard adds both counts.
- **P13-W02 Users and audit:** `/users` finds a user by phone (normalized to `+20…`); `/users/:uid` shows profile, roles, host status, credits, points, fees, recent rides and bookings, with Suspend / Unsuspend, Set gender and Set Verified; `/audit` lists `adminActions`, newest first.

**E2E:** Appendix C steps 26–28.

---

## Phase 14 — Release and operations

**Exit:** prod deployed, pilot testers installed, alerts and daily backups on, the manual test script passes on two phones.

| Task | Spec |
| --- | --- |
| P14-B01 Impact counter | 8.13, in `onRideFinished` |
| P14-B02 `weeklyMetrics` | 8.13 |
| P14-H01 Backups | `gcloud firestore backups schedules create --database='(default)' --recurrence=daily --retention=7d --project=hitchy-prod` (check the current gcloud syntax) |
| P14-H02 Alerts | Cloud Monitoring: function error rate and latency; Crashlytics velocity alerts; budget alerts already set |
| P14-H03 App Check | Move testers to a Google Play internal or closed test (Play Integrity only trusts Play installs), watch App Check metrics for about a week, then set `config/limits.appCheckEnforced: true` and enforce for Firestore and Storage in the console |
| P14-H04 Store | Listing, screenshots, privacy policy URL, deletion URL, Data safety; closed test rules for personal accounts (12 testers, 14 days) `[DECISION-03]` |
| P14-A01 Analytics | `Analytics` wrapper and the events in Appendix E; no personal data in parameters |
| P14-A02 Force update | Remote Config `minVersionCode`; blocking update screen when the app is older |
| P14-A03 Impact on home | live `metrics/impact`: trips, liters, CO₂, EGP saved |
| P14-A04 Baseline Profile | generated for the start-up and search flows |
| P14-W01 Dashboard metrics | the dashboard shows `metrics/impact` and the weekly retention from `metrics/{week}` |
| P14-A05 Releases | tags `android-v*`, `backend-v*` and `admin-v*`, release notes in Arabic and English, staged rollout later (10% → 50% → 100%) |

---

## Appendix A — CLAUDE.md

Copy this into `CLAUDE.md` at the repo root.

````markdown
# Hitchy

Carpooling for Greater Cairo commuters. Riders are free; hosts buy trip packages.
Full spec: docs/BUILD_PLAN.md. Read its sections 1–10 before any task.

## Layout
- backend/ — Firebase: functions (TypeScript), rules, indexes, seed data
- android/ — Kotlin + Compose Multiplatform (`app` + `shared`), Android target only
- admin-web/ — React + TypeScript admin dashboard (English, Mantine), hosted on Firebase Hosting
- contract/ — data and API contract shared with the iOS app (ios/ is not ours)

## Commands
Backend (in backend/functions):
- npm ci · npm run lint · npm run typecheck · npm run build
- npm test            (all tests on the emulators, project demo-hitchy)
- npm run test:unit   (fast, no emulator)
- npm run emulators · npm run seed
Android (in android/):
- ./gradlew assembleDevDebug -Phitchy.emulator=true
- ./gradlew check
Admin web (in admin-web/):
- npm ci · npm run dev (emulators) · npm run lint · npm run typecheck · npm test · npm run e2e

## Rules
1. Apps never write money, seats, credits, points, ratings, fees, roles or statuses. Functions do.
2. zod on every callable input; read-then-write inside one transaction.
3. A function or field change updates contract/ docs and fixtures in the same commit.
4. No secrets in code. No phone numbers, ID numbers, plates, locations or chat text in logs or analytics.
5. Done = the task's Verify commands pass. Never skip or delete a failing test.
6. Store UTC Timestamps, show Africa/Cairo. Money is integer EGP.
7. All UI text in resources, English and Arabic.
8. Only the libraries in BUILD_PLAN section 4.4.
9. Markers: [FIGMA] plain Material 3 UI · [DECISION-NN] use the Appendix F default + TODO(DECISION-NN) · [HUMAN] do not do, list it · [LATER] do not build.
10. Spec unclear or in conflict with the code → stop and ask.
11. The admin web never writes Firestore directly; it calls admin-only functions.

## End of every task
List files changed, test results, and any [HUMAN] or [DECISION] items.
````

---

## Appendix B — Prompts for Claude Code

| When | Prompt |
| --- | --- |
| Start of a phase | `Read docs/BUILD_PLAN.md sections 1–10 and Phase <N>. List the tasks in order with the files each one touches and anything unclear. Do not write code yet.` |
| Each task | The prompt in [0.4](#04-prompt-to-start-a-task) with the task ID. |
| Review | `Review the current diff for <TASK-ID> against section 1 of BUILD_PLAN.md and the task's "Done when". List problems only, most serious first.` |
| Contract check | `Compare contract/functions.md, contract/fixtures and backend/functions/src/schemas. List every mismatch, then fix them in one commit.` |
| End of a phase | `Run npm test in backend/functions and ./gradlew check in android. Then give me the manual steps from Appendix C for Phase <N>.` |
| Design system | `Read docs/BUILD_PLAN.md Phase 3, then open each Figma frame listed there with the Figma connection. Implement <TASK-ID> only.` |

---

## Appendix C — End-to-end test script

**Setup:** emulators running with seed data (`npm run emulators`, `npm run seed`) and two Android emulators on `devDebug` with `-Phitchy.emulator=true`; or `hitchy-dev` with the test numbers. Phone A = host, phone B = rider. Code for every test number: `123456`.

| Seed user | Phone |
| --- | --- |
| `rider@test` (man) | +201000000001 |
| `rider-w@test` (woman) | +201000000002 |
| `host@test` (approved, car) | +201000000003 |
| `host-w@test` (approved woman, car) | +201000000004 |
| `admin@test` | +201000000005 |
| `host-new@test` (plain user) | +201000000006 |

| # | Step | Expected |
| --- | --- | --- |
| 1 | A signs in with +201000000007 and fills the profile | Rider home; `users`, `profiles`, `kyc` docs exist |
| 2 | A (`host-new@test`): Become an owner, 4 photos, car, submit | Status "pending"; admin notified |
| 3 | Admin approves in the dashboard (Google sign-in) | A gets `host_approved`; status shows "Free until" |
| 4 | `host@test` posts Fifth Settlement → Smart Village, tomorrow 08:00, 3 seats, 56 EGP | Ride in My rides; "Fair price" shown |
| 5 | Same host tries 70 EGP, then a third ride that day | `PRICE_OUT_OF_RANGE`, then `DAILY_RIDE_LIMIT` |
| 6 | B (`rider@test`) searches tomorrow morning | The ride shows with host card and price |
| 7 | B requests 2 seats; the host accepts | Sheet shows "pay the owner directly", the late fee and the total; host gets `booking_requested`; after Accept, B gets `booking_accepted` and sees car and plate |
| 8 | `host-w@test` posts a Women-only ride; `rider-w@test` searches | Visible to `rider-w@test`, never to `rider@test` |
| 9 | B cancels (free) and requests again; the host declines; B requests once more and nobody answers | Seats go up, down, up, down, then up when the request expires; B gets `booking_declined`, then `request_expired` |
| 10 | Host posts a ride leaving in 35 minutes; B requests it; the host accepts | Confirmed |
| 11 | Host taps Start trip | Allowed; B gets `ride_started`; host shows "Trip in progress" |
| 12 | B taps "I'm on my way" | B's location sharing starts |
| 13 | B taps "I'm at the pickup point" | Host gets `rider_at_pickup` |
| 14 | Host taps Boarded for B | B shows "on board" |
| 15 | Host sends "Leaving now" in the chat; B taps Call | B gets `chat_message`; the dialer opens with the host's number |
| 16 | Host taps Finish, then marks B as paid | Ride finished; both trackers stop; B gets `payment_confirmed` and the receipt shows it |
| 17 | Both rate 5 stars | Ratings show on profiles |
| 18 | New ride: B does not check in; 5 minutes after departure the host reports late | B gets `fee_created`; B's next request → `UNPAID_FEE` |
| 19 | Host marks the fee paid (or B disputes and admin waives) | B can book again |
| 20 | Set the host's `freeUntil` in the past (emulator UI), finish a trip with a boarded rider | `credits.regular` −1 and a ledger entry |
| 21 | Host requests the 10-trip package; admin confirms | Balance +10; `package_confirmed` |
| 22 | `rewardsEnabled: true`; finish a verified trip | Both +10 points; host redeems 300 → +3 credits |
| 23 | `assistantEnabled: true`; B asks "عايز أروح القرية الذكية بكرة الساعة 8" | Short Arabic reply and up to 3 real ride cards; Request works |
| 24 | `fixedRideEnabled: true`; host creates a Sunday–Thursday 08:00 Fixed Ride; B joins with the code; run `generateFixedRides` | Tomorrow's ride exists with B booked; a skip before 20:00 removes B for that day |
| 25 | Host cancels a booked ride | B gets `backup_options`; the options screen requests another ride |
| 26 | B on a trip taps SOS | Dialer with 122; admins get `sos`; Share trip opens the share sheet |
| 27 | B blocks the host | The host's rides disappear from B's search |
| 28 | B deletes the account (no trips in the next 24 hours) | Fresh OTP, signed out; B's personal docs and files are gone in the emulator UI |
| 29 | Switch to Arabic | RTL layout, Arabic strings, Arabic notifications |
| 30 | Airplane mode, then post a ride | Clear offline message, no crash |

---

## Appendix D — Error catalog (EN/AR)

`errors.ts` (backend), `errors.md` (contract) and the string resources (`error_<reason_lowercase>`) all come from this table.

| Reason | HTTPS code | English | Arabic |
| --- | --- | --- | --- |
| `UNAUTHENTICATED` | unauthenticated | Please sign in again. | سجّل دخولك تاني من فضلك. |
| `PERMISSION_DENIED` | permission-denied | You can't do this. | مش مسموحلك تعمل كده. |
| `INVALID_INPUT` | invalid-argument | Some information is missing or wrong. | فيه بيانات ناقصة أو غلط. |
| `PROFILE_REQUIRED` | failed-precondition | Complete your profile first. | كمّل بياناتك الأول. |
| `SUSPENDED` | permission-denied | Your account is paused. Contact support. | حسابك موقوف مؤقتاً، كلّم الدعم. |
| `NOT_FOUND` | not-found | We couldn't find this. | مش لاقيين الحاجة دي. |
| `FEATURE_DISABLED` | failed-precondition | This isn't available yet. | الميزة دي لسه مش متاحة. |
| `INTERNAL` | internal | Something went wrong. Try again. | حصلت مشكلة، جرّب تاني. |
| `PHONE_REQUIRED` | failed-precondition | Sign in with your phone number first. | سجّل برقم موبايلك الأول. |
| `GENDER_LOCKED` | failed-precondition | Gender can't be changed. Contact support. | النوع مينفعش يتغير، كلّم الدعم. |
| `HOST_NOT_APPROVED` | failed-precondition | Your owner account isn't approved yet. | حسابك كصاحب عربية لسه متوافقش عليه. |
| `VEHICLE_LIMIT` | failed-precondition | You can add up to 2 vehicles. | تقدر تضيف عربيتين بس. |
| `SCOOTER_DISABLED` | failed-precondition | Scooters aren't available yet. | السكوتر لسه مش متاح. |
| `KYC_ALREADY_SUBMITTED` | failed-precondition | Your documents are under review. | ورقك بيتراجع دلوقتي. |
| `KYC_MISSING_FILES` | failed-precondition | Some documents are missing. | فيه ورق ناقص. |
| `KYC_NOT_SUBMITTED` | failed-precondition | There are no documents to review. | مفيش ورق للمراجعة. |
| `VEHICLE_NOT_APPROVED` | failed-precondition | This vehicle isn't approved yet. | العربية دي لسه متوافقش عليها. |
| `ROUTE_NOT_SUPPORTED` | failed-precondition | This route isn't available yet. | الطريق ده لسه مش متاح. |
| `SEATS_INVALID` | invalid-argument | Choose a valid number of seats. | اختار عدد كراسي مناسب. |
| `DEPART_TOO_SOON` | failed-precondition | Departure must be at least 30 minutes from now. | ميعاد الخروج لازم يكون بعد نص ساعة على الأقل. |
| `DEPART_TOO_FAR` | failed-precondition | You can post up to 7 days ahead. | تقدر تنزّل مشوار لحد 7 أيام قدام. |
| `PRICE_OUT_OF_RANGE` | invalid-argument | The price is outside the allowed range. | السعر برّه الحدود المسموحة. |
| `WOMEN_ONLY_HOST` | permission-denied | Only women owners can offer Women-only rides. | مشاوير "ستات بس" لصاحبات العربيات بس. |
| `DAILY_RIDE_LIMIT` | failed-precondition | You can post one ride each way per day. | تقدر تنزّل مشوار رايح وواحد جاي في اليوم. |
| `RIDE_TIME_CONFLICT` | failed-precondition | You have another ride close to this time. | عندك مشوار تاني قريب من الميعاد ده. |
| `NO_CREDITS` | failed-precondition | Buy a trip package to post rides. | اشتري باكدج رحلات عشان تنزّل مشاوير. |
| `RIDE_HAS_STARTED` | failed-precondition | This ride has already started. | المشوار بدأ خلاص. |
| `RIDE_NOT_OPEN` | failed-precondition | This ride isn't available anymore. | المشوار ده مبقاش متاح. |
| `RIDE_FULL` | failed-precondition | There aren't enough seats left. | مفيش كراسي كفاية فاضلة. |
| `OWN_RIDE` | failed-precondition | You can't book your own ride. | مينفعش تحجز في مشوارك. |
| `ALREADY_BOOKED` | already-exists | You already have a booking or request on this ride. | عندك حجز أو طلب في المشوار ده بالفعل. |
| `UNPAID_FEE` | failed-precondition | Pay your late fee before booking. | ادفع غرامة التأخير الأول. |
| `WOMEN_ONLY` | permission-denied | This ride is for women only. | المشوار ده للستات بس. |
| `TIME_CONFLICT` | failed-precondition | You have another booking close to this time. | عندك حجز تاني قريب من الميعاد ده. |
| `TOO_LATE_TO_BOOK` | failed-precondition | Booking closes 15 minutes before departure. | الحجز بيقفل قبل الميعاد بربع ساعة. |
| `BOOKING_NOT_FOUND` | not-found | We couldn't find your booking. | مش لاقيين حجزك. |
| `HOST_NOT_LATE` | failed-precondition | The owner isn't late yet. | صاحب العربية لسه متأخرش. |
| `START_WINDOW_CLOSED` | failed-precondition | You can start up to 30 minutes before or after departure. | تقدر تبدأ قبل الميعاد أو بعده بنص ساعة. |
| `RIDE_NOT_STARTED` | failed-precondition | Start the trip first. | ابدأ المشوار الأول. |
| `CHECK_IN_CLOSED` | failed-precondition | Check-in for this ride is closed. | تسجيل الوصول للمشوار ده قفل. |
| `NOT_ON_RIDE` | permission-denied | You're not on this ride. | إنت مش في المشوار ده. |
| `GRACE_NOT_OVER` | failed-precondition | Wait until the grace time ends. | استنى لحد ما وقت السماح يخلص. |
| `RIDER_WAS_ON_TIME` | failed-precondition | The rider checked in on time. | الراكب وصل في ميعاده. |
| `FEE_EXISTS` | already-exists | A fee was already recorded. | الغرامة اتسجلت قبل كده. |
| `FEE_NOT_FOUND` | not-found | We couldn't find this fee. | مش لاقيين الغرامة دي. |
| `FEE_NOT_DISPUTABLE` | failed-precondition | This fee can't be disputed now. | مينفعش تعترض على الغرامة دي دلوقتي. |
| `RATING_NOT_ALLOWED` | failed-precondition | You can rate after the trip ends. | تقدر تقيّم بعد ما المشوار يخلص. |
| `RATING_EXISTS` | already-exists | You already rated this trip. | إنت قيّمت المشوار ده قبل كده. |
| `RATING_WINDOW_CLOSED` | failed-precondition | Ratings close 7 days after the trip. | التقييم بيقفل بعد المشوار بـ 7 أيام. |
| `PACKAGE_NOT_FOUND` | not-found | This package isn't available. | الباكدج ده مش متاح. |
| `TOO_MANY_PENDING` | resource-exhausted | You have orders waiting for confirmation. | عندك طلبات لسه مستنية تأكيد. |
| `ORDER_NOT_PENDING` | failed-precondition | This order was already handled. | الطلب ده اتعامل معاه خلاص. |
| `NOT_ENOUGH_POINTS` | failed-precondition | You need 300 points. | محتاج 300 نقطة. |
| `AI_DAILY_LIMIT` | resource-exhausted | You've used today's assistant messages. Use search. | خلصت رسايل المساعد النهارده، استخدم البحث. |
| `AI_UNAVAILABLE` | unavailable | The assistant isn't available now. Use search. | المساعد مش متاح دلوقتي، استخدم البحث. |
| `INVITE_INVALID` | not-found | This invite code isn't valid. | كود الدعوة ده مش صحيح. |
| `FIXED_RIDE_FULL` | failed-precondition | This Fixed Ride is full. | المشوار الثابت ده مليان. |
| `FIXED_RIDE_EXISTS` | already-exists | You already have a Fixed Ride this way. | عندك مشوار ثابت في نفس الاتجاه. |
| `SKIP_TOO_LATE` | failed-precondition | Skips close at 8 PM the day before. | الإلغاء بيقفل الساعة 8 بالليل اليوم اللي قبله. |
| `CANNOT_REPORT_SELF` | invalid-argument | You can't report yourself. | مينفعش تبلّغ عن نفسك. |
| `ACTIVE_TRIP_SOON` | failed-precondition | Cancel your trips in the next 24 hours first. | ألغي مشاويرك اللي في الـ 24 ساعة الجاية الأول. |
| `REQUEST_NOT_PENDING` | failed-precondition | This request was already answered. | الطلب ده اترد عليه خلاص. |
| `REQUEST_EXPIRED` | failed-precondition | This request has expired. | الطلب ده وقته خلص. |
| `CHAT_CLOSED` | failed-precondition | Chat for this ride is closed. | الشات بتاع المشوار ده اتقفل. |
| `CONTACT_NOT_ALLOWED` | permission-denied | You can call only after a booking is accepted. | تقدر تتصل بس بعد ما الحجز يتقبل. |
| `PAYMENT_ALREADY_CONFIRMED` | already-exists | This payment was already confirmed. | الدفع ده اتأكد قبل كده. |
| `OTP_INVALID` (app only) | — | The code is wrong. | الكود غلط. |
| `OTP_EXPIRED` (app only) | — | The code expired. Send a new one. | الكود انتهى، ابعت كود جديد. |
| `TOO_MANY_ATTEMPTS` (app only) | — | Too many tries. Try again later. | محاولات كتير، جرّب بعد شوية. |
| `SMS_QUOTA` (app only) | — | We can't send codes right now. Try later. | مش قادرين نبعت كود دلوقتي، جرّب بعد شوية. |
| `NETWORK` (app only) | — | No internet connection. | مفيش اتصال بالإنترنت. |

---

## Appendix E — Analytics events

No personal data in any parameter.

| Event | When | Parameters |
| --- | --- | --- |
| `sign_up_completed` | `completeProfile` succeeds the first time | `language` |
| `host_application_submitted` | submit succeeds | — |
| `ride_posted` | `createRide` succeeds | `seats`, `direction`, `traffic_level`, `fair_price` |
| `search_done` | results shown | `result_count`, `sort`, `seats`, `verified_only`, `women_only` |
| `booking_requested` | `requestSeats` succeeds | `seats`, `fair_price`, `same_company`, `verified` |
| `request_answered` | `respondToRequest` succeeds | `accepted` |
| `booking_cancelled` | `cancelBooking` succeeds | `reason`, `late` |
| `chat_message_sent` | `sendChatMessage` succeeds | — |
| `call_tapped` | Call opens the dialer | — |
| `payment_confirmed` | `confirmSeatPayment` succeeds | — |
| `trip_started` | `startRide` succeeds | — |
| `trip_finished` | `finishRide` succeeds | `riders_completed` |
| `late_reported` | `reportLate` succeeds | `kind` |
| `fee_disputed` | `disputeFee` succeeds | — |
| `package_requested` | `requestPackage` succeeds | `package_id` |
| `points_redeemed` | `redeemPoints` succeeds | `bundles` |
| `assistant_message` | `askAssistant` returns | `used_search`, `result_count` |
| `fixed_ride_created` | `createFixedRide` succeeds | `weekdays_count` |
| `sos_pressed` | SOS confirmed | — |
| `language_changed` | language switched | `language` |
| `account_deleted` | `deleteAccount` succeeds | — |

---

## Appendix F — Open decisions and defaults

Until Islam decides, the agent uses the default and leaves `TODO(DECISION-NN)` where it matters.

| ID | Decision | Default for the agent | Needed by |
| --- | --- | --- | --- |
| 01 | Firestore and Functions region | `europe-west1` | Phase 0 |
| 02 | Android package name | `com.hitchy.app` (dev `com.hitchy.app.dev`) | Phase 0 |
| 03 | Google Play account type | Organization (no code impact) | Phase 0 |
| 04 | Default language and digits | Arabic, Western digits | Phase 2 |
| 05 | Dark mode | Light only (Figma has no dark tokens) | Phase 3 |
| 06 | Normal and off-peak hours | Normal 10:00–16:00, the rest off-peak | Phase 6 |
| 07 | Scooter numbers and helmet rule | Scooters disabled (`null` config) | Phase 6 |
| 08 | Late rider cancel | Free until 60 minutes before departure | Phase 7 |
| 09 | Rider no-show | 10 EGP fee after 15 minutes | Phase 8 |
| 10 | Rider and host contact | Decided in v1.1: chat and calls between the host and riders with an accepted booking (R23); details in 22 and 23 | Phase 8 |
| 11 | Package credits expiry | No expiry | Phase 9 |
| 12 | AI model provider | `LlmClient` with a fake in tests; assistant stays disabled until a provider is chosen | Phase 11 |
| 13 | Fixed Ride free skip cutoff | 20:00 the evening before | Phase 12 |
| 14 | ID photo retention | Delete 30 days after the decision | Phase 13 |
| 15 | Company registration and e-receipts | Not code; ask an accountant before charging | Before month 2 |
| 16 | Admin dashboard address | The `admin` Hosting site's default `web.app` URL until a subdomain is set | Phase 5 |
| 17 | Admin dashboard language | English only | Phase 5 |
| 18 | Bottom navigation tabs | Decided in v1.1: Home, Rides, Shop, Profile; Shop holds Rewards (rider) or Packages (host) | Phase 3 |
| 19 | Minimum touch target | 48 dp on Android | Phase 3 |
| 20 | English word for the car owner | Decided in v1.1: "Owner" in the app's text, never "driver"; code and data keep `host` | Phase 3 |
| 21 | Request deadline | The host has 30 minutes to answer, and never past 15 minutes before departure; the seats are held meanwhile | Phase 7 |
| 22 | Phone sharing | Numbers only between the host and riders with an accepted booking, from acceptance until the ride ends; calls through the phone's dialer (no masked numbers) | Phase 8 |
| 23 | Chat | Open from acceptance until 24 hours after the ride; kept 30 days; pushes say "New message from {name}" without the text; only the two people can read it (not admins) | Phase 8 |
| 24 | Several seats | One late or no-show fee per booking (10 EGP), not per seat; on Women-only rides the rider confirms every passenger is a woman | Phase 7 |
| 25 | Search window | The picked time ± 60 minutes; the time chips split it into before, the first hour after, and later | Phase 7 |
| 26 | Arrived and ETA | "Arrived" when the host is within 300 m of the pickup point; ETA = straight-line distance at 25 km/h | Phase 8 |
| 27 | Notification inbox | Every push except admin alerts is also stored for 30 days (Firestore TTL) | Phase 7 |
