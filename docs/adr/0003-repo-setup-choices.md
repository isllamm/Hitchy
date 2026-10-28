# 0003 · Repo setup choices

- **Status:** accepted
- **Date:** 2026-10-28
- **Source:** setup of the repo skeleton (P0-A01), CLAUDE.md and the CI/CD workflows; rows 12 to 17 from the backend scaffold (P1-B01, 2026-10-29)

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

## Consequences

- P1-B11, P2-A14 and P5-W01 check these workflows against the real code.
- To go back to the exact Appendix A text, change `CLAUDE.md` and this ADR together.
