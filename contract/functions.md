# Cloud Functions Contract (Phases 4–8)

This document defines the callable APIs provided by Cloud Functions for Firebase.
The backend source of truth is:

- Inputs (zod): `backend/functions/src/schemas/inputs.ts`
- Outputs (TypeScript): `backend/functions/src/schemas/io.ts`
- Error reasons: `contract/errors.md` and `backend/functions/src/lib/errors.ts`

---

## 1. General Rules for Every Callable

1. **Invocation**: Functions are deployed as Firebase HTTPS Callables in region `europe-west1`.
2. **Authentication & Authorization**:
   - Every callable requires an authenticated user (`request.auth.uid`), otherwise fails with `UNAUTHENTICATED`.
   - When App Check enforcement is active, missing or invalid App Check tokens fail with `UNAUTHENTICATED`.
   - Admin-only functions require the Auth token claim `admin: true`, otherwise fail with `PERMISSION_DENIED`.
   - Non-admin functions (except `completeProfile`) require an existing profile document in `users/{uid}`, otherwise fail with `PROFILE_REQUIRED`.
   - Suspended accounts cannot invoke functions unless explicitly marked `allowSuspended`, failing with `SUSPENDED`.
3. **Strict Inputs**: Input objects are strict (`z.strictObject`); unexpected fields are rejected with `INVALID_INPUT`. Text fields are trimmed before length validation.
4. **Instants & Timestamps**:
   - **Inputs**: Accept ISO-8601 UTC strings with or without milliseconds (e.g. `"2026-11-02T05:30:00Z"` or `"2026-11-02T05:30:00.000Z"`).
   - **Outputs**: **Always** formatted as full ISO-8601 UTC strings with milliseconds via `toISOString()` (e.g. `"2026-11-02T05:30:00.000Z"`).
5. **Idempotency**: Mutating callables accept a unique client-generated UUID `requestId` (stored in `idempotency/{uid}_{requestId}`) ensuring safe retries.

---

## 2. Phase 4 — Profile & Roles

### `completeProfile`

Completes first-time user onboarding and profile setup.

- **Caller**: Authenticated user with no existing `users/{uid}`.
- **Input**:
  - `firstName`: `string` (1–40 chars)
  - `lastName`: `string` (1–40 chars)
  - `gender`: `"female" | "male"`
  - `companyId`: `string | null`
  - `companyOther`: `string | null` (1–60 chars)
  - `workHours`: `{ start: "HH:mm"; end: "HH:mm" } | null`
  - `language`: `"ar" | "en"`
  - `referralCode`: `string | null` (1–20 chars)
- **Output**:
  - `activeRole`: `"rider"`
  - `hostStatus`: `"none"`
  - `referralApplied`: `boolean`
- **Errors**: `INVALID_INPUT`, `ALREADY_EXISTS`.

### `switchRole`

Switches the active role of the caller between rider and host.

- **Caller**: Signed-in user.
- **Input**:
  - `role`: `"rider" | "host"`
- **Output**:
  - `activeRole`: `"rider" | "host"`
- **Errors**: `HOST_NOT_APPROVED`.

---

## 3. Phase 5 — Host Verification & Admin

### `addVehicle`

Adds a vehicle to the host's account for KYC review.

- **Caller**: Signed-in user.
- **Input**:
  - `type`: `"car" | "scooter"`
  - `make`: `string` (1–30 chars)
  - `model`: `string` (1–30 chars)
  - `year`: `number` (1990–2100)
  - `color`: `string` (1–20 chars)
  - `plate`: `string` (1–15 chars)
- **Output**:
  - `vehicleId`: `string`
- **Errors**: `VEHICLE_LIMIT`, `SCOOTER_DISABLED`.

### `submitHostApplication`

Submits host KYC documents and an added vehicle for administrator verification.

- **Caller**: Signed-in user with uploaded KYC documents.
- **Input**:
  - `vehicleId`: `string`
- **Output**:
  - `status`: `"submitted"`
- **Errors**: `KYC_ALREADY_SUBMITTED`, `KYC_MISSING_FILES`, `NOT_FOUND`.

### `approveHost`

Admin callable approving a host application and granting free subscription trial days.

- **Caller**: Admin only.
- **Input**:
  - `uid`: `string`
  - `vehicleId`: `string`
- **Output**:
  - `freeUntil`: `string` (ISO UTC timestamp with milliseconds, +30 days)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `rejectHost`

Admin callable rejecting a host application with specific document failure reasons.

- **Caller**: Admin only.
- **Input**:
  - `uid`: `string`
  - `rejections`: `Array<{ doc: "idFront" | "idBack" | "drivingLicense" | "vehicleLicense" | "criminalRecord"; reason: "blurry" | "expired" | "mismatch" | "missing" | "other" }>` (1–5 items)
  - `note`: `string | null` (1–500 chars)
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `approveVehicle`

Admin callable approving or rejecting an additional registered vehicle.

- **Caller**: Admin only.
- **Input**:
  - `uid`: `string`
  - `vehicleId`: `string`
  - `approve`: `boolean`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `setVerified`

Admin callable granting or revoking the Verified host badge.

- **Caller**: Admin only.
- **Input**:
  - `uid`: `string`
  - `verified`: `boolean`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `setGender`

Admin callable to correct a user's gender (locked on self-profile).

- **Caller**: Admin only.
- **Input**:
  - `uid`: `string`
  - `gender`: `"female" | "male"`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `requestVerification`

Allows an approved host to request the Verified badge after uploading a criminal record.

- **Caller**: Approved host.
- **Input**: `{}`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `HOST_NOT_APPROVED`, `KYC_MISSING_FILES`.

### `updateConfig`

Admin callable to safely update system configuration parameters.

- **Caller**: Admin only.
- **Input**:
  - `name`: `"pricing" | "packages" | "limits" | "rewards" | "impact"`
  - `value`: `Record<string, unknown>`
- **Output**:
  - `version`: `number`
- **Errors**: `PERMISSION_DENIED`, `INVALID_INPUT`.

---

## 4. Phase 6 — Rides & Pricing

### `quotePrice`

Calculates fair and recommended price ranges for a route based on fuel consumption, traffic, distance, and seats.

- **Caller**: Signed-in user.
- **Input**:
  - `vehicleId`: `string`
  - `fromZone`: `string`
  - `toZone`: `string`
  - `departAt`: `string` (ISO timestamp)
  - `seats`: `number` (1–3)
- **Output**:
  - `min`: `number` (integer EGP)
  - `recommended`: `number` (integer EGP)
  - `max`: `number` (integer EGP)
  - `trafficLevel`: `"offPeak" | "normal" | "peak"`
  - `distanceKm`: `number`
  - `fuelCostEgp`: `number`
  - `tripCostEgp`: `number`
  - `configVersion`: `number`
- **Errors**: `ROUTE_NOT_SUPPORTED`, `SCOOTER_DISABLED`.

### `createRide`

Creates and posts a new carpool trip.

- **Caller**: Approved host with positive package credits or active free trial.
- **Input**:
  - `vehicleId`: `string`
  - `fromZone`: `string`
  - `fromPointId`: `string`
  - `toZone`: `string`
  - `toPointId`: `string`
  - `direction`: `"to_work" | "to_home"`
  - `departAt`: `string` (ISO timestamp)
  - `seats`: `number` (1–3)
  - `pricePerSeat`: `number` (integer EGP)
  - `womenOnly`: `boolean`
  - `requestId`: `string` (UUID)
- **Output**:
  - `rideId`: `string`
- **Errors**: `HOST_NOT_APPROVED`, `NO_CREDITS`, `DEPART_TOO_SOON`, `DEPART_TOO_FAR`, `PRICE_OUT_OF_RANGE`, `WOMEN_ONLY_HOST`, `DAILY_RIDE_LIMIT`, `RIDE_TIME_CONFLICT`.

### `cancelRide`

Cancels an open carpool trip before departure.

- **Caller**: Host owner of the ride.
- **Input**:
  - `rideId`: `string`
  - `reason`: `"sick" | "car_problem" | "plans_changed" | "other"`
- **Output**:
  - `cancelledBookings`: `number`
- **Errors**: `NOT_FOUND`, `PERMISSION_DENIED`, `RIDE_HAS_STARTED`.

---

## 5. Phase 7 — Search & Booking

### `searchRides`

Searches available open rides matching zone, date, and filter preferences.

- **Caller**: Signed-in user.
- **Input**:
  - `fromZone`: `string`
  - `toZone`: `string`
  - `fromPointId`: `string | null`
  - `date`: `string` (`"YYYY-MM-DD"`)
  - `time`: `string` (`"HH:mm"`)
  - `seats`: `number` (1–3)
  - `sort`: `"recommended" | "earliest" | "cheapest"`
  - `timeBand`: `"any" | "before" | "first_hour" | "after"`
  - `maxPrice`: `number | null`
  - `minRating`: `4.5 | 4.8 | null`
  - `verifiedOnly`: `boolean`
  - `womenOnly`: `boolean`
- **Output**:
  - `rides`: `RideCard[]`
- **Errors**: `ROUTE_NOT_SUPPORTED`.

### `requestSeats`

Rider initiates a booking request for one or more seats on an open ride.

- **Caller**: Signed-in rider.
- **Input**:
  - `rideId`: `string`
  - `seats`: `number` (1–3)
  - `requestId`: `string` (UUID)
- **Output**:
  - `booking`:
    - `rideId`: `string`
    - `status`: `"requested"`
    - `seats`: `number`
    - `price`: `number`
    - `total`: `number`
    - `expiresAt`: `string` (ISO UTC timestamp with milliseconds)
  - `ride`: `RideCard`
- **Errors**: `RIDE_FULL`, `OWN_RIDE`, `ALREADY_BOOKED`, `UNPAID_FEE`, `WOMEN_ONLY`, `TIME_CONFLICT`, `TOO_LATE_TO_BOOK`.

### `respondToRequest`

Host accepts or declines a pending booking request.

- **Caller**: Host owner of the ride.
- **Input**:
  - `rideId`: `string`
  - `riderUid`: `string`
  - `accept`: `boolean`
- **Output**:
  - `status`: `"booked" | "declined"`
- **Errors**: `NOT_FOUND`, `PERMISSION_DENIED`, `REQUEST_NOT_PENDING`, `REQUEST_EXPIRED`, `RIDE_FULL`.

### `cancelBooking`

Rider cancels their pending request or confirmed booking.

- **Caller**: Rider who booked.
- **Input**:
  - `rideId`: `string`
  - `reason`: `"host_late" | "plans_changed" | "found_another_ride" | "other"`
- **Output**:
  - `lateCancel`: `boolean`
- **Errors**: `NOT_FOUND`, `PERMISSION_DENIED`, `RIDE_HAS_STARTED`.

---

## 6. Phase 8 — Trip Day & Completion

### `startRide`

Host starts the trip up to 30 minutes before scheduled departure.

- **Caller**: Host owner of the ride.
- **Input**:
  - `rideId`: `string`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `START_WINDOW_CLOSED`, `RIDE_HAS_STARTED`.

### `checkIn`

Rider registers arrival at the pickup point via GPS.

- **Caller**: Booked rider.
- **Input**:
  - `rideId`: `string`
  - `lat`: `number`
  - `lng`: `number`
  - `accuracyM`: `number`
- **Output**:
  - `distanceM`: `number`
- **Errors**: `NOT_ON_RIDE`, `RIDE_NOT_STARTED`, `CHECK_IN_CLOSED`.

### `markBoarded`

Host confirms that a rider has boarded the vehicle.

- **Caller**: Host owner of the ride.
- **Input**:
  - `rideId`: `string`
  - `riderUid`: `string`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `reportLate`

Reports a no-show or late counterparty.

- **Caller**: Host or booked rider on a started ride.
- **Input**:
  - `rideId`: `string`
  - `riderUid`: `string`
  - `kind`: `"late" | "no_show"`
- **Output**:
  - `feeId`: `string`
- **Errors**: `PERMISSION_DENIED`, `GRACE_NOT_OVER`, `RIDER_WAS_ON_TIME`, `HOST_NOT_LATE`, `FEE_EXISTS`.

### `disputeFee`

Disputes an assessed late cancellation or no-show fee.

- **Caller**: User charged the fee.
- **Input**:
  - `feeId`: `string`
  - `text`: `string` (1–300 chars)
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `NOT_FOUND`, `PERMISSION_DENIED`, `FEE_NOT_DISPUTABLE`.

### `resolveFee`

Admin callable resolving a fee dispute by upholding or waiving the fee.

- **Caller**: Admin only.
- **Input**:
  - `feeId`: `string`
  - `decision`: `"keep" | "waive"`
  - `note`: `string | null`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `markFeePaid`

Admin callable marking an outstanding fee as settled.

- **Caller**: Admin only.
- **Input**:
  - `feeId`: `string`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`.

### `confirmSeatPayment`

Host confirms receipt of direct cash/Instapay payment from a rider.

- **Caller**: Host owner of the ride.
- **Input**:
  - `rideId`: `string`
  - `riderUid`: `string`
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `PERMISSION_DENIED`, `NOT_FOUND`, `PAYMENT_ALREADY_CONFIRMED`.

### `sendChatMessage`

Sends an in-ride message between host and accepted rider.

- **Caller**: Host or booked rider on the ride.
- **Input**:
  - `rideId`: `string`
  - `riderUid`: `string`
  - `text`: `string` (1–500 chars)
  - `requestId`: `string` (UUID)
- **Output**:
  - `messageId`: `string`
- **Errors**: `NOT_ON_RIDE`, `CHAT_CLOSED`.

### `getContactPhone`

Retrieves counterparty phone number to enable onetap direct calling after booking acceptance.

- **Caller**: Host or booked rider with accepted booking.
- **Input**:
  - `rideId`: `string`
  - `otherUid`: `string`
- **Output**:
  - `phone`: `string` (E.164, e.g. `"+201000000003"`)
- **Errors**: `NOT_ON_RIDE`, `CONTACT_NOT_ALLOWED`.

### `finishRide`

Host completes the ride, closing active bookings and deducting package credits.

- **Caller**: Host owner of the ride.
- **Input**:
  - `rideId`: `string`
- **Output**:
  - `completedBookings`: `number`
  - `creditUsed`: `boolean`
- **Errors**: `PERMISSION_DENIED`, `RIDE_NOT_STARTED`.

### `submitRating`

Submits a rating and structured feedback tags for a completed carpool trip.

- **Caller**: Host or rider from the completed ride.
- **Input**:
  - `rideId`: `string`
  - `toUid`: `string`
  - `stars`: `1 | 2 | 3 | 4 | 5`
  - `tags`: `string[]` (max 5 tags from allowed rating tags)
- **Output**: `{}` (`EmptyOutput`)
- **Errors**: `NOT_ON_RIDE`, `RATING_NOT_ALLOWED`, `RATING_EXISTS`, `RATING_WINDOW_CLOSED`.
