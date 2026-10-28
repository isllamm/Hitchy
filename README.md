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

Phase 1: the backend scaffold is in place (P1-B01). Code comes task by task; the Android and admin web projects start with P2-A01 and P5-W01.

## Run locally

Each task that adds a project also adds its commands here.

### Backend (`backend/functions`)

Needs Node 24 (exact version in `.nvmrc`) and Java 21 for the Firebase emulators.

```bash
cd backend/functions
npm ci
npm run build
npm test
```

| Command | What it does |
| --- | --- |
| `npm run build` | Compiles TypeScript to `lib/` |
| `npm run typecheck` | Type-checks without writing files |
| `npm run lint` | ESLint, then the Prettier check |
| `npm run test:unit` | Unit tests, no emulator |
| `npm test` | All tests on the emulators (project `demo-hitchy`). Build first: the Functions emulator loads `lib/` |
| `npm run emulators` | Starts the emulators; the UI is at http://localhost:4000 |
| `npm run seed` | Loads the seed data into the emulators (from P1-B08) |

Emulator ports: Auth 9099, Firestore 8080, Functions 5001, Storage 9199, Hosting 5002 (macOS uses 5000), UI 4000. The project ID `demo-hitchy` never touches real cloud resources.

## CI/CD

| Workflow | Starts on | What it does |
| --- | --- | --- |
| `backend.yml` | Pull requests and pushes to `main` that touch `backend/` or `contract/`; tags `backend-v*`; manual run | Lint, typecheck, build and test on the emulators. Deploys to `hitchy-dev` from `main` and to `hitchy-prod` from a tag |
| `android.yml` | Pull requests and pushes to `main` that touch `android/` or `contract/`; tags `android-v*` | `./gradlew check assembleDevDebug`. Sends `devRelease` to App Distribution group `team` from `main`, and `prodRelease` to group `pilot` from a tag |
| `admin-web.yml` | Pull requests and pushes to `main` that touch `admin-web/` or `backend/functions/src/schemas/`; tags `admin-v*` | Lint, typecheck, test and build. Deploys the `admin` Hosting site to `hitchy-dev` from `main` and to `hitchy-prod` from a tag |

Each workflow skips its jobs until its project exists (`backend/functions/package.json`, `android/gradlew`, `admin-web/package.json`). Deploys also need the `dev` and `prod` GitHub environments and the secrets from Phase 0 (P0-H01, P0-H05, P0-H06, P0-H09). Until those secrets exist, each deploy job skips its steps and leaves a notice that names the missing secrets. Prod deploys wait for approval in the `prod` environment.

## Working on a task

One task per branch and pull request:

- Branch: `feat/<task-id>-short-name`, for example `feat/P7-B02-book-seat`
- Commits: Conventional Commits with a scope (`backend`, `android`, `admin`, `contract`, `ci`, `docs`)
- Pull request title: starts with the task ID

See section 5.9 of the build plan.
