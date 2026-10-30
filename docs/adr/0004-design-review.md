# 0004 · Design review: product changes

- **Status:** accepted
- **Date:** 2026-10-29
- **Source:** [docs/BUILD_PLAN.md](../BUILD_PLAN.md) v1.1, section 2.1 (decisions) and section 3.8 (screen map and fix list)

## Context

The finished app designs (46 PNG exports) were checked against build plan v1.0. They changed 14 product rules: booking, rider fee, price, payment proof, places, time, seats, contact, tabs, wording, trip steps, rating, filters and role. Islam decided each one.

## Decisions

| Area | Decision |
| --- | --- |
| Booking | The owner accepts each booking; a request holds the seats until it is accepted, declined or expires (30 minutes, never past 15 minutes before departure) |
| Seats | One booking can hold several seats |
| Money | Riders pay Hitchy nothing; they pay the owner's price × seats directly, see a receipt, and the owner marks them as paid |
| Places and time | Fixed points inside zones, picked with the designs' search, saved, recent and map picker; the rider picks one time and the search covers an hour either side |
| Contact | Chat and calls between the owner and riders with an accepted booking, until the ride ends (calls) or 24 hours after it (chat) |
| App | Tabs Home, Rides, Shop (Rewards or Packages), Profile; the word "Owner" in the app's text while code and data keep `host`; the designs' trip steps, rating tags plus the plan's negative tags, and the designs' filters plus Verified only and Women-only; the Welcome choice only picks the starting path |
| Inbox | Every push is also kept in a notification inbox for 30 days |

The full list, with where each decision changes the data model, functions, rules and tasks, is in section 2.1 of the build plan. New open decisions 21 to 27 in Appendix F hold the defaults for the details.

## Consequences

- New functions: `requestSeats` (replaces `bookSeat`), `respondToRequest`, `expireRequest`, `confirmSeatPayment`, `sendChatMessage` (replaces `sendQuickMessage`), `getContactPhone`.
- New data: booking `seats`, `total`, `expiresAt`, `respondedAt`, `paymentConfirmedAt`; statuses `requested`, `declined`, `expired`; chat messages; the inbox; saved places.
- Phone numbers are no longer always hidden (R18 changed). They are shared only inside an accepted booking.
- The designs still need the fixes listed in section 3.8 of the build plan.

## Update 2026-10-30: full rider design set

Islam shared the full rider design set (57 PNG files, English only; the index and flow map are in [docs/design/rider-flow.md](../design/rider-flow.md)). It adds Profile, Settings, Saved places, Help, Contact support and About. Where it disagreed with the plan, Islam decided:

| Area | Decision |
| --- | --- |
| Saved places | Keep fixed points only; "Add" opens the point search and point-pin map, with no address fields |
| Wallet | No wallet at launch (R19 stays). A rider wallet to pay deposits to owners is `[LATER]`, after launch, in its own plan update |
| Notifications | Keep the plan's texts (8.15): no Hitchy fee text, no "Booking request sent" item |
| Rating | Keep the plan: stars and tags, no comment box; the thank-you screen ends with "Done" |
| Wording | The badge says "Verified owner" |
| Settings | Opens from a row in the Profile tab; the notification toggle and language move there. Identity Verification shows in owner mode only. Privacy Settings and Safety Center are `[LATER]` |
| New screens | Plan 3.8 maps Profile, ProfileEdit, Settings and About (P4-A04), Saved places (P7-A02), and Help and Contact support (P13-A05) |
| Help and support | The FAQ is a fixed list in string resources. Contact support saves a request in Firestore through a new callable `contactSupport` (P13-B06) that admins read in the admin web (P13-W03); its details are asked when that task starts |

## Update 2026-10-30 (night): demo designs

Islam shared the demo designs (26 rider and 11 owner files; the flow maps are in [docs/design/demo-flow.md](../design/demo-flow.md)) and set a demo of the full rider and owner cycle for Friday 2026-10-30 evening, on the real backend. Plan 2.2 (v1.2) holds the decisions; 3.9 holds the tasks.

| Area | Decision |
| --- | --- |
| Demo | The real screens on the local emulators with seed data, happy path first (tasks P3-B01 to P3-H01) |
| Wallet | Replaces the earlier "no wallet" decision. Seed balance and an admin top-up; the owner's Confirm Payment moves each rider's fare and late fee to the owner; a request needs enough balance |
| Rider designs | The earlier decisions stand, except the wallet |
| Pickups | The owner adds stops (fixed points with times); the rider picks one |
| Free wait | Starts when the owner taps "I'm here"; a fee only after "Report late" |
| Rides tab | One list for both roles |
| Earnings line | "Expected EGP x · up to EGP y" on owner ride details |
| Tests | A one-time exception to rule 5 for the demo tasks: the happy path plus every money, permission and input check |

Consequences:

- Hitchy now holds user balances. Before any real money: a legal check (a wallet may need a licence in Egypt) and a payment provider. Both are `[LATER]`.
- New functions `arriveAtStop`, `confirmRidePayment` (replaces `confirmSeatPayment`) and `topUpWallet`; new data `RideStop`, `pickupZones`, booking pickup fields, `hostArrivedAt`, wallets and wallet entries; new reasons `INSUFFICIENT_BALANCE`, `STOPS_INVALID`, `PICKUP_NOT_ON_RIDE`, `RIDE_NOT_FINISHED`; open decisions 28 to 32.
