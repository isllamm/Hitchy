# Contract

The data and API contract between the backend and the apps. The iOS app is built from this folder only, never from Android code.

| File | What is in it |
| --- | --- |
| `firestore.md` | Every path and field: its type and who writes it |
| `functions.md` | Every function: input, output and errors |
| `errors.md` | Every error reason, with its English and Arabic message |
| `fixtures/` | `<function>.request.json` and `<function>.response.json` for every function |

The source of truth is `backend/functions/src/schemas/` (build plan sections 6 to 8 and Appendix D). These files mirror it. They arrive with task P1-B10.

## How to change the contract safely

1. Both owners approve: Islam and the iOS teammate. GitHub counts one code owner's approval as enough, so wait for both.
2. Docs and fixtures change in the same pull request as the schema (rule 3 of the build plan).
3. No field is removed or renamed without a deprecation period.
4. The fixtures must stay valid: the backend contract test (`backend/functions/test/unit/contract.test.ts`) and the Android DTO test (task P2-A13) read every file in `fixtures/`.
