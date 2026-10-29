# 0002 · Versions

- **Status:** backend versions accepted (P1-B01 on 2026-10-29). Android P2-A01 scaffold, P2-A02 convention plugins, P2-A04 core utilities, P2-A05 Firebase wiring, and P2-A06 Koin dependency injection built with pinned versions (AGP 9.5.0, Gradle 9.8.2, Kotlin 2.4.21, Compose Multiplatform 1.12.2, compileSdk 37, targetSdk 36, minSdk 26, serialization 1.12.0, kotlinx-datetime 0.8.0, GitLive 2.7.0, Firebase BoM 34.18.0, Google Services plugin 4.5.0, Crashlytics plugin 3.0.8, Coroutines 1.11.0, Koin BOM 4.2.2, DataStore Preferences 1.3.0). Other Android and admin web versions stay proposed until their tasks build with them.
- **Date:** 2026-10-28
- **Source:** [docs/BUILD_PLAN.md](../BUILD_PLAN.md), section 4.4 and tasks P1-B01, P2-A01

## Context

Section 4.4 lists the libraries the project may use (rule 8). Every version is pinned exactly, with no `+` and no ranges, in `package.json` or `gradle/libs.versions.toml`, and recorded here.

These are the newest stable releases on 2026-10-28, checked against each other's peer dependencies. Where the newest release does not fit, the note says which version is used and why. Rows marked "not in 4.4" are needed by the plan's own tasks or are peer dependencies of listed libraries; that note is their one-line reason (rule 8).

## Backend (`backend/functions`)

| Need | Package | Version | Note |
| --- | --- | --- | --- |
| Runtime | Node.js | 24.22.1 in `.nvmrc`, `24` in `engines`, `nodejs24` in `firebase.json` | Newest LTS that Cloud Functions supports (GA until 2028-04-30). Node 26 became LTS on 2026-10-28 but is only in preview on Cloud Functions, and firebase-tools does not list it yet. 24.22.1 is what Homebrew installs; 24.22.2 is one patch newer. |
| Language | typescript | 6.0.3 | 7.0.2 is newer, but typescript-eslint 8.72.1 supports TypeScript below 6.1 only. |
| Language | @types/node | 24.19.2 | Not in 4.4: types for the Node 24 runtime. |
| Firebase | firebase-functions | 7.4.0 | |
| Firebase | firebase-admin | 14.5.0 | Fits firebase-functions 7.4.0. |
| Validation | zod | 4.6.7 | Same version in the admin web. |
| Time | luxon | 3.7.2 | With @types/luxon 3.7.6. |
| Tests | vitest | 5.0.5 | |
| Tests | @firebase/rules-unit-testing | 6.0.0 | Needs Node 24.12 or newer and `firebase` 13. |
| Tests | firebase (JS SDK, dev only) | 13.1.0 | Not in 4.4: peer of @firebase/rules-unit-testing. |
| Emulators, deploy | firebase-tools | 15.34.1 | Not in 4.4 by name: it is the Firebase Emulator Suite and the CLI used by `npm test` and CI. |
| Scripts | tsx | 4.23.15 | Not in 4.4: runs `seed.ts` and `set-admin.ts` (P1-B01, P1-B09). |
| Lint | eslint | 10.13.0 | See open point 1. |
| Lint | typescript-eslint | 8.72.1 | |
| Lint | prettier | 3.9.10 | |

## Android (`android/`)

| Need | Library | Version | Note |
| --- | --- | --- | --- |
| Build | Gradle (wrapper) | 9.8.2 | |
| Build | Android Gradle Plugin | 9.5.0 | Includes the KMP library plugin `com.android.kotlin.multiplatform.library`. |
| Build | compileSdk | 37 | Required by Compose Multiplatform 1.12.2 (AndroidX Compose 1.12.1 AAR metadata requires compileSdk >= 37). |
| Build | targetSdk and minSdk | targetSdk 36, minSdk 26 | Google Play requires targetSdk 36. minSdk 26 as proposed in P2-A02. |
| Language | Kotlin | 2.4.21 | Also the Compose compiler plugin and kotlin.test. |
| UI | Compose Multiplatform | 1.12.2 | |
| UI | org.jetbrains.compose.material3:material3 | 1.9.0 | Material 3 now has its own version line; 1.9.0 is its newest stable (1.13.0 is in beta). |
| ViewModel | org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose | 2.11.0 | |
| Navigation | org.jetbrains.androidx.navigation:navigation-compose | 2.10.0 | |
| DI | Koin BOM | 4.2.2 | koin-core, koin-compose, koin-compose-viewmodel, koin-android. |
| Firebase in `commonMain` | GitLive firebase-kotlin-sdk | 2.7.0 | firebase-auth, -firestore, -functions, -storage. Built with Kotlin 2.2.21 and Firebase BoM 34.18.0. |
| Firebase in `androidMain` and `app` | Firebase Android BoM | 34.18.0 | GitLive 2.7.0 was built with 34.18.0. See open point 3. |
| Firebase setup | Google services Gradle plugin | 4.5.0 | Not in 4.4: reads `google-services.json`. |
| Firebase setup | Crashlytics Gradle plugin | 3.0.8 | Not in 4.4: needed for Crashlytics. |
| Async | kotlinx-coroutines (core and test) | 1.11.0 | |
| JSON | kotlinx-serialization-json | 1.12.0 | |
| Time | kotlinx-datetime | 0.8.0 | |
| Images | Coil 3 (coil-compose) | 3.6.3 | |
| Settings | DataStore Preferences | 1.3.0 | |
| Logs | Kermit | 2.2.0 | |
| Location | play-services-location | 21.4.0 | |
| Android | androidx.appcompat | 1.8.0 | Not in 4.4: `MainActivity` is an `AppCompatActivity` (P2-A09). |
| Android | androidx.activity:activity-compose | 1.13.0 | Not in 4.4: `setContent` in `MainActivity`. |
| Tests | koin-test | 4.2.2 (from Koin BOM) | Not in 4.4: JVM verification test (module.verify()) in app/src/test. |
| Tests | Turbine | 1.2.1 | |
| Tests | Konsist | 0.18.1 | |
| Tests | Roborazzi | 1.77.0 | After Figma (Phase 3). |
| Quality | detekt | 1.23.8 | See open point 2. |
| Quality | Spotless Gradle plugin | 8.10.4 | |
| Quality | ktlint | 1.8.0 | |
| Quality | LeakCanary | 2.14 | `devDebug` only. |

Kotlin libraries built with an older Kotlin work with Kotlin 2.4. The P2-A01 build confirms the whole set.

## Admin web (`admin-web/`)

| Need | Package | Version | Note |
| --- | --- | --- | --- |
| Runtime | Node.js | 24.22.1 | CI uses the backend's `.nvmrc`. |
| App | react, react-dom | 19.3.1 | With @types/react and @types/react-dom 19.3.0. |
| App | typescript | 6.0.3 | Same reason as the backend. |
| App | vite | 8.3.5 | |
| App | @vitejs/plugin-react | 6.1.2 | Needs Vite 8. |
| App | react-router | 8.5.1 | Needs React 19.2.7 or newer and Node 22.22 or newer. |
| Firebase | firebase (JS SDK) | 13.1.0 | |
| UI | @mantine/core, @mantine/notifications, @mantine/dates | 9.7.4 | Need React 19.2 or newer. |
| UI | @mantine/hooks | 9.7.4 | Not in 4.4: peer of Mantine. |
| UI | dayjs | 1.11.23 | Not in 4.4: peer of @mantine/dates. |
| Validation | zod | 4.6.7 | Same as the backend. |
| Tests | vitest | 5.0.5 | |
| Tests | @testing-library/react | 16.3.3 | |
| Tests | @testing-library/dom | 10.4.2 | Not in 4.4: peer of @testing-library/react. |
| Tests | jsdom | 30.1.3 | Not in 4.4: the DOM for Vitest component tests. |
| Tests | @playwright/test | 1.64.1 | |
| Lint | eslint, typescript-eslint, prettier | 10.13.0, 8.72.1, 3.9.10 | Same as the backend. |

## CI (GitHub Actions)

Each action is pinned to the commit of its release, with the version in a comment. Dependabot keeps them current.

| Action | Version | Note |
| --- | --- | --- |
| actions/checkout | v7.0.1 | |
| actions/setup-node | v7.1.0 | |
| actions/setup-java | v6.1.0 | Temurin JDK 21 for the backend emulators, 17 for Android. |
| gradle/actions/setup-gradle | v6.4.1 | `cache-provider: basic` (ADR 0003). |
| google-github-actions/auth | v3.0.0 | Workload Identity Federation. |

## Open points

1. **ESLint config format (decided 2026-10-29).** ESLint 10 reads only `eslint.config.*` (flat config); it no longer includes the old `.eslintrc` system. The plan names `.eslintrc.cjs` in sections 4.2 and 4.5; the project uses `eslint.config.mjs` instead, in the backend (P1-B01) and later in the admin web (P5-W01).
2. **detekt.** 1.23.8 is the newest stable, but it was built for Kotlin 2.0. detekt 2.0 (for Kotlin 2.x) is still alpha (2.0.0-alpha.6). With Kotlin 2.4, P2-A12 may need to force detekt's own Kotlin version, or wait for detekt 2.0. Decide in P2-A12.
3. **Firebase BoM (decided in P2-A05).** GitLive 2.7.0 was built and tested against Firebase BoM 34.18.0; BoM 35.x has major breaking changes. P2-A05 pins BoM 34.18.0 and will re-evaluate BoM 35 when GitLive releases a compatible update.

## How to change a version

Change it here and in `package.json` or `gradle/libs.versions.toml` in the same pull request, with the reason.
