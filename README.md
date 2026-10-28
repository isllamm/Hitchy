# Hitchy

Carpooling for commuters in Greater Cairo. Riders use the app for free. Hosts (car owners) buy trip packages. Riders pay hosts for seats directly, by cash or InstaPay; Hitchy never holds that money.

- Full spec: [docs/BUILD_PLAN.md](docs/BUILD_PLAN.md)
- Rules for AI agents: [CLAUDE.md](CLAUDE.md)
- Architecture decisions: [docs/adr/](docs/adr/)

## Layout

| Folder | What is in it |
| --- | --- |
| `backend/` | Firebase backend: Cloud Functions (TypeScript), Firestore and Storage rules, indexes, seed data, admin scripts, the account deletion page |
| `android/` | Android app in Kotlin + Compose Multiplatform. `app` is the entry point; `shared` holds the features. Android target only |
| `admin-web/` | Admin dashboard for the team: React + TypeScript + Mantine on Firebase Hosting. English only |
| `contract/` | Data and API contract shared with the iOS app: documents, functions, errors and JSON fixtures |
| `ios/` | iOS app in Swift. Owned by the iOS teammate and built from `contract/` only |
| `docs/` | The build plan and architecture decision records |
| `.github/` | CI/CD workflows, code owners, pull request and issue templates, Dependabot |

Section 4 of the build plan shows the full tree. Empty folders hold a `.gitkeep` file until a task adds files to them.

## Status

Phase 0: repo skeleton only. Code comes task by task, starting with P1-B01 (backend), P2-A01 (Android) and P5-W01 (admin web).

## Run locally

Each task that adds a project also adds its commands here.

## CI/CD

| Workflow | Starts on | What it does |
| --- | --- | --- |
| `backend.yml` | Pull requests and pushes to `main` that touch `backend/` or `contract/`; tags `backend-v*`; manual run | Lint, typecheck, build and test on the emulators. Deploys to `hitchy-dev` from `main` and to `hitchy-prod` from a tag |
| `android.yml` | Pull requests and pushes to `main` that touch `android/` or `contract/`; tags `android-v*` | `./gradlew check assembleDevDebug`. Sends `devRelease` to App Distribution group `team` from `main`, and `prodRelease` to group `pilot` from a tag |
| `admin-web.yml` | Pull requests and pushes to `main` that touch `admin-web/` or `backend/functions/src/schemas/`; tags `admin-v*` | Lint, typecheck, test and build. Deploys the `admin` Hosting site to `hitchy-dev` from `main` and to `hitchy-prod` from a tag |

Each workflow skips its jobs until its project exists (`backend/functions/package.json`, `android/gradlew`, `admin-web/package.json`). Deploys also need the `dev` and `prod` GitHub environments and the secrets from Phase 0 (P0-H01, P0-H05, P0-H06, P0-H09). Prod deploys wait for approval in the `prod` environment.

## Working on a task

One task per branch and pull request:

- Branch: `feat/<task-id>-short-name`, for example `feat/P7-B02-book-seat`
- Commits: Conventional Commits with a scope (`backend`, `android`, `admin`, `contract`, `ci`, `docs`)
- Pull request title: starts with the task ID

See section 5.9 of the build plan.
