# Firestore Data Contract

This document defines the schema, data types, and write permissions for Cloud Firestore in Hitchy.
The backend TypeScript source of truth is `backend/functions/src/schemas/docs.ts` and `backend/functions/src/schemas/config.ts`.

---

## 1. Conventions

1. **Document Timestamps**: All stored instants are Firestore `Timestamp` objects in UTC. UI layers convert them to `Africa/Cairo` for display.
2. **Base Document Structure**: Documents extending `Base` include:
   - `schemaVersion`: integer literal `1`
   - `createdAt`: `Timestamp`
   - `updatedAt`: `Timestamp`
3. **Write Permissions**:
   - **Server only**: Money, seats, credits, points, ratings, fees, roles, and status fields are written **only** by Cloud Functions.
   - **Client**: Clients may write only specific editable fields on their own records (e.g. `users/{uid}` profile details, `devices/{deviceId}`) strictly governed by `firestore.rules`.
4. **Money & Numbers**: All currency amounts are stored as whole integer Egyptian Pounds (EGP). Distances are integers in meters (`distanceM`) or kilometers (`distanceKm`).

---

## 2. Collections & Subcollections

### 2.1 User & Identity

#### `users/{uid}`

- **Read**: Authenticated user (`request.auth.uid == uid`) or Admin.
- **Write**: Authenticated user (strictly limited to `USER_EDITABLE_FIELDS`), or Cloud Functions.
- **Fields**:
  - `schemaVersion`: `1`
  - `phone`: `string` (E.164, e.g. `"+201000000001"`)
  - `firstName`: `string` (1–50 chars)
  - `lastName`: `string` (1–50 chars)
  - `gender`: `"male" | "female"`
  - `companyId`: `string | null`
  - `companyOther`: `string | null`
  - `workHours`: `{ start: HHmm; end: HHmm } | null`
  - `language`: `"ar" | "en"`
  - `photoPath`: `string | null` (`"avatars/{uid}.jpg"`)
  - `emergencyContact`: `{ name: string; phone: string } | null`
  - `savedPlaces`: `Array<{ label: "home" | "work" | "other"; zoneId: string; pointId: string }>` (max 5)
  - `activeRole`: `"rider" | "host"` (written only by `switchRole`)
  - `referralCode`: `string` (short uppercase code)
  - `referredBy`: `string | null`
  - `referralRewarded`: `boolean`
  - `blockedUids`: `string[]` (max 200)
  - `rider`:
    - `rating`: `number | null` (average 1.0–5.0)
    - `ratingCount`: `number`
    - `tripsCompleted`: `number`
    - `noShowCount`: `number`
    - `unpaidFeeId`: `string | null`
  - `host`:
    - `status`: `"none" | "pending" | "approved" | "rejected"`
    - `verified`: `boolean`
    - `freeUntil`: `Timestamp | null`
    - `regularCredits`: `number`
    - `fixedCredits`: `number`
    - `rating`: `number | null`
    - `ratingCount`: `number`
    - `tripsCompleted`: `number`
    - `noShowCount`: `number`
    - `activeRideId`: `string | null`
    - `currentVehicleId`: `string | null`
    - `tier`: `"bronze" | "silver" | "gold"`
  - `points`:
    - `balance`: `number`
    - `tier`: `"bronze" | "silver" | "gold"`
  - `safety`:
    - `suspended`: `boolean`
    - `suspendedReason`: `string | null`
    - `suspendedUntil`: `Timestamp | null`
    - `reportsReceived`: `number`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `profiles/{uid}`

- **Read**: Any signed-in user.
- **Write**: Server only (kept in sync via triggers).
- **Fields**:
  - `uid`: `string`
  - `firstName`: `string`
  - `gender`: `"male" | "female"`
  - `photoPath`: `string | null`
  - `companyId`: `string | null`
  - `rider`: `{ rating: number | null; ratingCount: number; trips: number }`
  - `host`: `{ rating: number | null; ratingCount: number; trips: number; verified: boolean; tier: "bronze" | "silver" | "gold" }`
  - `updatedAt`: `Timestamp`

#### `users/{uid}/devices/{deviceId}`

- **Read / Write**: Owner (`request.auth.uid == uid`).
- **Fields**:
  - `fcmToken`: `string`
  - `platform`: `"android" | "ios"`
  - `appVersion`: `string`
  - `updatedAt`: `Timestamp`

#### `users/{uid}/vehicles/{vehicleId}`

- **Read**: Owner or Admin.
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `make`: `string`
  - `model`: `string`
  - `year`: `number`
  - `color`: `string`
  - `plate`: `string` (Arabic letters and numbers)
  - `seats`: `number` (1–6)
  - `type`: `"car" | "scooter"`
  - `licenseExpiresAt`: `Timestamp`
  - `status`: `"pending" | "approved" | "rejected"`
  - `rejectReason`: `string | null`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `users/{uid}/credits/{entryId}`

- **Read**: Owner or Admin.
- **Write**: Server only (audit ledger for host credits).
- **Fields**:
  - `delta`: `number`
  - `bucket`: `"regular" | "fixed"`
  - `reason`: `"package" | "trip" | "reward" | "admin"`
  - `refId`: `string` (e.g. `orderId`, `rideId`)
  - `balanceAfter`: `number`
  - `createdAt`: `Timestamp`

#### `users/{uid}/points/{entryId}`

- **Read**: Owner or Admin.
- **Write**: Server only (Hitchy Points ledger).
- **Fields**:
  - `amount`: `number`
  - `remaining`: `number` (for earn entries; 0 for spend/expire)
  - `open`: `boolean` (`remaining > 0`)
  - `reason`: `"trip" | "on_time_week" | "five_star" | "fixed_week" | "referral" | "redeem" | "expire"`
  - `refId`: `string | null`
  - `expireAt`: `Timestamp | null`
  - `createdAt`: `Timestamp`

#### `inbox/{uid}/items/{itemId}`

- **Read / Update**: Owner (can update `read: true`).
- **Create / Delete**: Server only.
- **Fields**:
  - `title`: `{ ar: string; en: string }`
  - `body`: `{ ar: string; en: string }`
  - `deepLink`: `string | null`
  - `type`: `string`
  - `read`: `boolean`
  - `createdAt`: `Timestamp`

#### `kyc/{uid}`

- **Read**: Owner or Admin.
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `nationalId`: `string` (14 digits)
  - `status`: `"pending" | "submitted" | "approved" | "rejected"`
  - `docs`:
    - `nationalIdFront`: `{ path: string; status: "pending" | "uploaded" | "accepted" | "rejected"; rejectReason: string | null }`
    - `nationalIdBack`: `{ path: string; status: "pending" | "uploaded" | "accepted" | "rejected"; rejectReason: string | null }`
    - `driverLicenseFront`: `{ path: string; status: "pending" | "uploaded" | "accepted" | "rejected"; rejectReason: string | null }`
    - `driverLicenseBack`: `{ path: string; status: "pending" | "uploaded" | "accepted" | "rejected"; rejectReason: string | null }`
    - `vehicleLicenseFront`: `{ path: string; status: "pending" | "uploaded" | "accepted" | "rejected"; rejectReason: string | null }`
    - `vehicleLicenseBack`: `{ path: string; status: "pending" | "uploaded" | "accepted" | "rejected"; rejectReason: string | null }`
    - `criminalRecord`: `{ path: string; status: "pending" | "uploaded" | "accepted" | "rejected"; rejectReason: string | null }`
  - `submittedAt`: `Timestamp | null`
  - `reviewedAt`: `Timestamp | null`
  - `reviewerUid`: `string | null`
  - `note`: `string | null`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

---

### 2.2 Rides & Bookings

#### `rides/{rideId}`

- **Read**:
  - Any signed-in user if `status == "open"`
  - Host or riders with an accepted booking on this ride
  - Admins
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `hostUid`: `string`
  - `vehicleId`: `string`
  - `vehicleType`: `"car" | "scooter"`
  - `departAt`: `Timestamp`
  - `dateKey`: `string` (`"YYYY-MM-DD"` in Cairo time)
  - `timeOfDay`: `string` (`"HH:mm"` in Cairo time)
  - `direction`: `"to_work" | "to_home"`
  - `fromZone`: `string`
  - `toZone`: `string`
  - `fromPointId`: `string`
  - `toPointId`: `string`
  - `fromPoint`: `{ lat: number; lng: number }`
  - `toPoint`: `{ lat: number; lng: number }`
  - `seatsTotal`: `number` (1–6)
  - `seatsLeft`: `number`
  - `pricePerSeat`: `number` (integer EGP)
  - `womenOnly`: `boolean`
  - `notes`: `string | null`
  - `fixedRideId`: `string | null`
  - `status`: `"open" | "started" | "finished" | "cancelled"`
  - `cancelReason`: `"host_cancelled" | "weather" | "car_trouble" | "no_riders" | "emergency" | "other" | null`
  - `startedAt`: `Timestamp | null`
  - `finishedAt`: `Timestamp | null`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `rides/{rideId}/bookings/{bookingId}`

- **Read**: Host of the ride, the rider who booked (`riderUid == auth.uid`), or Admin.
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `rideId`: `string`
  - `riderUid`: `string`
  - `seats`: `number` (1–6)
  - `pricePerSeat`: `number` (integer EGP)
  - `totalPrice`: `number` (integer EGP)
  - `pickupPointId`: `string`
  - `dropoffPointId`: `string`
  - `status`: `"requested" | "booked" | "declined" | "cancelled" | "boarded" | "completed" | "no_show"`
  - `paymentConfirmed`: `boolean`
  - `checkInDistanceM`: `number | null`
  - `checkInAt`: `Timestamp | null`
  - `boardedAt`: `Timestamp | null`
  - `cancelReason`: `string | null`
  - `cancelledBy`: `"rider" | "host" | "system" | null`
  - `cancelledAt`: `Timestamp | null`
  - `expiresAt`: `Timestamp | null` (for `"requested"` state)
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `rides/{rideId}/presence/{uid}`

- **Read**:
  - Host reads all presence docs on their ride.
  - Accepted rider reads their own presence doc and the host's doc.
  - Admin reads all.
- **Write**: Owner (`request.auth.uid == uid`), when ride `status == "started"`.
- **Fields**:
  - `lat`: `number`
  - `lng`: `number`
  - `heading`: `number | null`
  - `speed`: `number | null`
  - `updatedAt`: `Timestamp`

#### `rides/{rideId}/chats/{riderUid}/messages/{messageId}`

- **Read**: Host of the ride or the rider (`request.auth.uid == riderUid`).
- **Write**: Server only (`sendChatMessage` callable).
- **Fields**:
  - `senderUid`: `string`
  - `text`: `string`
  - `sentAt`: `Timestamp`

---

### 2.3 Trip Lifecycle, Fees, Disputes & Ratings

#### `fees/{feeId}`

- **Read**: The user charged the fee (`uid == auth.uid`), the counterparty (`otherUid == auth.uid`), or Admin.
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `rideId`: `string`
  - `bookingId`: `string`
  - `uid`: `string` (user charged)
  - `otherUid`: `string` (beneficiary / other party)
  - `kind`: `"late_cancel" | "rider_no_show" | "host_no_show"`
  - `amount`: `number` (integer EGP)
  - `status`: `"unpaid" | "paid" | "disputed" | "waived"`
  - `paidAt`: `Timestamp | null`
  - `resolvedAt`: `Timestamp | null`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `disputes/{feeId}`

- **Read**: User who disputed, or Admin.
- **Write**: Server only (`disputeFee` / `resolveFee`).
- **Fields**:
  - `feeId`: `string`
  - `uid`: `string`
  - `text`: `string`
  - `status`: `"open" | "resolved_waived" | "resolved_upheld"`
  - `resolution`: `string | null`
  - `resolverUid`: `string | null`
  - `createdAt`: `Timestamp`
  - `resolvedAt`: `Timestamp | null`

#### `ratings/{rideId}_{fromUid}_{toUid}`

- **Read**: Creator (`fromUid == auth.uid`) or Admin.
- **Write**: Server only (`submitRating` callable).
- **Fields**:
  - `rideId`: `string`
  - `fromUid`: `string`
  - `toUid`: `string`
  - `role`: `"rider_to_host" | "host_to_rider"`
  - `stars`: `number` (1–5)
  - `tags`: `string[]` (from allowed enum tags)
  - `createdAt`: `Timestamp`

---

### 2.4 Packages, Fixed Rides, Safety & Admin

#### `orders/{orderId}`

- **Read**: Owner (`uid == auth.uid`) or Admin.
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `uid`: `string`
  - `packageId`: `string`
  - `credits`: `number`
  - `priceEgp`: `number`
  - `instapayReference`: `string`
  - `status`: `"pending" | "confirmed" | "rejected"`
  - `confirmedBy`: `string | null`
  - `confirmedAt`: `Timestamp | null`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `fixedRides/{fixedRideId}`

- **Read**: Host, accepted riders, or Admin.
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `hostUid`: `string`
  - `direction`: `"to_work" | "to_home"`
  - `fromZone`: `string`
  - `toZone`: `string`
  - `fromPointId`: `string`
  - `toPointId`: `string`
  - `timeOfDay`: `string` (`"HH:mm"`)
  - `weekdays`: `number[]` (1 = Monday, 7 = Sunday)
  - `seatsTotal`: `number`
  - `riderUids`: `string[]`
  - `status`: `"active" | "paused" | "cancelled"`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `fixedRides/{fixedRideId}/skips/{skipId}`

- **Read**: Host, rider, or Admin.
- **Write**: Server only.
- **Fields**:
  - `dateKey`: `string` (`"YYYY-MM-DD"`)
  - `uid`: `string`
  - `reason`: `string | null`
  - `createdAt`: `Timestamp`

#### `reports/{reportId}`

- **Read**: Creator or Admin.
- **Write**: Server only.
- **Fields**:
  - `schemaVersion`: `1`
  - `reporterUid`: `string`
  - `reportedUid`: `string`
  - `rideId`: `string | null`
  - `category`: `string`
  - `text`: `string`
  - `status`: `"open" | "resolved" | "dismissed"`
  - `createdAt`: `Timestamp`
  - `updatedAt`: `Timestamp`

#### `safetyEvents/{eventId}`

- **Read**: Admin only.
- **Write**: Server only (`triggerSos` callable).
- **Fields**:
  - `uid`: `string`
  - `rideId`: `string | null`
  - `type`: `"sos"`
  - `location`: `{ lat: number; lng: number } | null`
  - `resolved`: `boolean`
  - `createdAt`: `Timestamp`

#### `adminActions/{actionId}`

- **Read**: Admin only.
- **Write**: Server only (audit trail for admin operations).
- **Fields**:
  - `adminUid`: `string`
  - `action`: `string`
  - `targetType`: `string`
  - `targetId`: `string`
  - `payload`: `Record<string, unknown>`
  - `createdAt`: `Timestamp`

#### `idempotency/{uid}_{requestId}`

- **Read / Write**: Server only (ensures at-most-once execution for mutating callables).
- **Fields**:
  - `result`: `unknown`
  - `createdAt`: `Timestamp`
  - `expireAt`: `Timestamp` (TTL: 24 hours)

---

### 2.5 Reference & Config Collections

#### `companies/{companyId}`

- **Read**: Any signed-in user.
- **Write**: Admin only.
- **Fields**:
  - `name`: `{ ar: string; en: string }`
  - `domain`: `string | null`
  - `active`: `boolean`

#### `zones/{zoneId}` & `zones/{zoneId}/points/{pointId}`

- **Read**: Any signed-in user.
- **Write**: Admin only.
- **Zone fields**:
  - `name`: `{ ar: string; en: string }`
  - `active`: `boolean`
  - `order`: `number`
- **Point fields**:
  - `name`: `{ ar: string; en: string }`
  - `lat`: `number`
  - `lng`: `number`
  - `active`: `boolean`

#### `routes/{fromZone}__{toZone}`

- **Read**: Any signed-in user.
- **Write**: Admin only.
- **Fields**:
  - `fromZone`: `string`
  - `toZone`: `string`
  - `distanceKm`: `number`
  - `usualMinutes`: `number`
  - `active`: `boolean`

#### `config/{name}`

- **Documents**: `pricing`, `packages`, `limits`, `rewards`, `impact`, `admins`.
- **Read**: Signed-in users (`admins` is read only by admins).
- **Write**: Admin only via `updateConfig` or admin CLI.
- **Fields**:
  - Defined in `backend/functions/src/schemas/config.ts`.
