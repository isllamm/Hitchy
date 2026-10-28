# 0001 · Architecture

- **Status:** accepted
- **Date:** 2026-10-28
- **Source:** [docs/BUILD_PLAN.md](../BUILD_PLAN.md), section 3

## Context

Hitchy has an Android app, an iOS app built separately in Swift, and an admin web dashboard for the team. All three need sign-in, shared data, rules about money and seats, photo uploads and notifications. The pilot runs in Greater Cairo.

## Decisions

| # | Decision | Reason |
| --- | --- | --- |
| 1 | Firebase is the whole backend: Auth, Firestore, Cloud Functions (2nd gen, TypeScript), Storage and Cloud Messaging, all in one region `[DECISION-01]` | One managed platform serves all three clients, and the data stays in one region (Firestore's location can never change). |
| 2 | Users sign in with a phone OTP, and App Check guards the backend | One account per phone (R12), and only genuine apps can call the backend. |
| 3 | Cloud Functions own all business logic: pricing, booking, credits, fees, points, ratings, notifications and scheduled jobs | Only the server decides money, seats and trust, so a changed or old app cannot break the rules. |
| 4 | Apps read through security rules and write only three things: their own profile basics, their device tokens and trip presence | Reads are direct and live; every other write goes through a validated function inside a transaction. |
| 5 | Apps upload photos (ID documents, avatars) straight to Storage, checked by storage rules | Files do not pass through functions; the rules limit who can upload, the file type and the size. |
| 6 | Only Cloud Functions send notifications, through Cloud Messaging | One place decides who gets which message, in the language of each device. |
| 7 | The admin web (`admin-web/`) is a separate React + TypeScript site on Firebase Hosting. Admins sign in with Google, and every change calls an admin-only function | Admin changes are checked and written to `adminActions`, like every other write. |
| 8 | `contract/` describes every document and function. The iOS app is built from it, never from Android code | The two app codebases stay in sync through one reviewed source. |
| 9 | Remote Config holds app-side flags only (feature toggles, minimum app version). Business numbers live in Firestore `config/*` | Functions can read `config/*`, and the server checks the matching limits, so an old app cannot bypass a flag. |
| 10 | The Android app uses Kotlin and Compose Multiplatform with an Android target only (`app` + `shared`) | Logic and UI live in `commonMain`, with platform code behind interfaces, so most of the code is tested without Android. |

## Consequences

- Every business change is a function change, tested on the emulators.
- A change to a function or a Firestore field also updates `contract/` in the same commit.
- The apps stay thin: they read, show and call functions.
