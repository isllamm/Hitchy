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
