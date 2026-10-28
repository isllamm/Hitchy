<!-- Title: start with the task ID, for example "P7-B02 bookSeat". -->

## Task ID

<!-- For example P7-B02 (docs/BUILD_PLAN.md). -->

## What changed

## How to test

<!-- The commands you ran and what they showed. -->

## Screenshots

<!-- UI changes only, in English and Arabic. -->

## Contract changed?

- [ ] No
- [ ] Yes, and `contract/` docs and fixtures are updated in this pull request

## Checklist (rules in section 1 of the build plan)

- [ ] 1. Server decides: the apps write no money, seats, credits, points, ratings, fees, roles or `status` fields
- [ ] 2. Every callable input is parsed with zod; every write that depends on a read is in one transaction
- [ ] 3. Any function or Firestore field change also updates `contract/` in the same commit
- [ ] 4. No secrets in code; no phone numbers, ID numbers, plates, locations or chat text in logs or analytics
- [ ] 5. Tests pass locally and in CI; no failing test was skipped or deleted
- [ ] 6. Instants stored as UTC `Timestamp` and shown in `Africa/Cairo`; money is integer EGP
- [ ] 7. Every user-facing string is in resources, in English and Arabic
- [ ] 8. Only libraries from section 4.4, or a one-line reason here for each new one
- [ ] 9. Only Phase 3 tokens and components; `[FIGMA]` followed for screens not designed yet
- [ ] 10. Small commits with Conventional Commits and a scope
- [ ] 11. Nothing guessed: unclear spec was asked about

## [HUMAN] and [DECISION] items

<!-- List anything left for Islam. -->
