// zod schemas of the callable inputs (BUILD_PLAN 8.3–8.7, Phases 4–8). Later phases add theirs.
// Objects are strict: an unknown field is INVALID_INPUT. Text is trimmed before its length is
// checked. Rules that need data or the clock (a ride's seats, "this year + 1") stay in the handlers.
// No Firebase imports: the admin web imports this file too.
import { z } from "zod";
import { RATING_TAGS } from "./docs.js";

// ---- Building blocks ----

// Firestore document ids (uids, auto ids, zone and point ids): no slashes or spaces.
const id = z.string().regex(/^[A-Za-z0-9_-]{1,128}$/);

const text = (min: number, max: number) => z.string().trim().min(min).max(max);

// "HH:mm", Cairo time.
const hhmm = z.string().regex(/^([01]\d|2[0-3]):[0-5]\d$/);

// "YYYY-MM-DD", a real calendar date.
const dateKey = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}$/)
  .refine((s) => {
    const d = new Date(`${s}T00:00:00Z`);
    return !Number.isNaN(d.getTime()) && d.toISOString().startsWith(s);
  }, "Not a calendar date");

// ISO-8601 UTC, "2026-11-02T05:30:00Z" (no offsets).
const isoUtc = z.iso.datetime();

const requestId = z.uuid();
const note = text(1, 500).nullable();
const seats = z.int().min(1).max(3);

const gender = z.enum(["female", "male"]);
const language = z.enum(["ar", "en"]);
const role = z.enum(["rider", "host"]);
const vehicleType = z.enum(["car", "scooter"]);
const direction = z.enum(["to_work", "to_home"]);
const kycDocKey = z.enum([
  "idFront",
  "idBack",
  "drivingLicense",
  "vehicleLicense",
  "criminalRecord",
]);
const ratingTag = z.enum([...new Set([...RATING_TAGS.host, ...RATING_TAGS.rider])]);

// ---- 8.3 Profile and roles (Phase 4) ----

export const CompleteProfileInput = z.strictObject({
  firstName: text(1, 40),
  lastName: text(1, 40),
  gender,
  companyId: id.nullable(),
  companyOther: text(1, 60).nullable(),
  workHours: z.strictObject({ start: hhmm, end: hhmm }).nullable(),
  language,
  // An unknown or own code is not an error (referralApplied: false), so any short text passes.
  referralCode: text(1, 20).nullable(),
});
export type CompleteProfileInput = z.infer<typeof CompleteProfileInput>;

export const SwitchRoleInput = z.strictObject({ role });
export type SwitchRoleInput = z.infer<typeof SwitchRoleInput>;

// ---- 8.4 Host verification and admin (Phase 5) ----

export const AddVehicleInput = z.strictObject({
  type: vehicleType,
  make: text(1, 30),
  model: text(1, 30),
  year: z.int().min(1990).max(2100), // the handler also checks "this year + 1" with lib/clock
  color: text(1, 20),
  plate: text(1, 15),
});
export type AddVehicleInput = z.infer<typeof AddVehicleInput>;

export const SubmitHostApplicationInput = z.strictObject({ vehicleId: id });
export type SubmitHostApplicationInput = z.infer<typeof SubmitHostApplicationInput>;

export const ApproveHostInput = z.strictObject({ uid: id, vehicleId: id });
export type ApproveHostInput = z.infer<typeof ApproveHostInput>;

export const RejectHostInput = z.strictObject({
  uid: id,
  rejections: z
    .array(
      z.strictObject({
        doc: kycDocKey,
        reason: z.enum(["blurry", "expired", "mismatch", "missing", "other"]),
      }),
    )
    .min(1)
    .max(5)
    .refine((list) => new Set(list.map((r) => r.doc)).size === list.length, "A document twice"),
  note,
});
export type RejectHostInput = z.infer<typeof RejectHostInput>;

export const ApproveVehicleInput = z.strictObject({ uid: id, vehicleId: id, approve: z.boolean() });
export type ApproveVehicleInput = z.infer<typeof ApproveVehicleInput>;

export const SetVerifiedInput = z.strictObject({ uid: id, verified: z.boolean() });
export type SetVerifiedInput = z.infer<typeof SetVerifiedInput>;

export const SetGenderInput = z.strictObject({ uid: id, gender });
export type SetGenderInput = z.infer<typeof SetGenderInput>;

export const RequestVerificationInput = z.strictObject({});
export type RequestVerificationInput = z.infer<typeof RequestVerificationInput>;

// The value is checked against that config's full schema in P5-B05.
export const UpdateConfigInput = z.strictObject({
  name: z.enum(["pricing", "packages", "limits", "rewards", "impact"]),
  value: z.record(z.string(), z.unknown()),
});
export type UpdateConfigInput = z.infer<typeof UpdateConfigInput>;

// ---- 8.5 Rides and pricing (Phase 6) ----

export const QuotePriceInput = z.strictObject({
  vehicleId: id,
  fromZone: id,
  toZone: id,
  departAt: isoUtc,
  seats,
});
export type QuotePriceInput = z.infer<typeof QuotePriceInput>;

export const CreateRideInput = z.strictObject({
  vehicleId: id,
  fromZone: id,
  fromPointId: id,
  toZone: id,
  toPointId: id,
  direction,
  departAt: isoUtc,
  seats,
  pricePerSeat: z.int().min(1), // integer EGP; the price range is checked against the quote
  womenOnly: z.boolean(),
  requestId,
});
export type CreateRideInput = z.infer<typeof CreateRideInput>;

export const CancelRideInput = z.strictObject({
  rideId: id,
  reason: z.enum(["sick", "car_problem", "plans_changed", "other"]),
});
export type CancelRideInput = z.infer<typeof CancelRideInput>;

// ---- 8.6 Search and booking (Phase 7) ----

export const SearchRidesInput = z.strictObject({
  fromZone: id,
  toZone: id,
  fromPointId: id.nullable(),
  date: dateKey,
  time: hhmm,
  seats,
  sort: z.enum(["recommended", "earliest", "cheapest"]),
  timeBand: z.enum(["any", "before", "first_hour", "after"]),
  maxPrice: z.int().min(1).nullable(),
  minRating: z.union([z.literal(4.5), z.literal(4.8)]).nullable(),
  verifiedOnly: z.boolean(),
  womenOnly: z.boolean(),
});
export type SearchRidesInput = z.infer<typeof SearchRidesInput>;

export const RequestSeatsInput = z.strictObject({ rideId: id, seats, requestId });
export type RequestSeatsInput = z.infer<typeof RequestSeatsInput>;

export const RespondToRequestInput = z.strictObject({
  rideId: id,
  riderUid: id,
  accept: z.boolean(),
});
export type RespondToRequestInput = z.infer<typeof RespondToRequestInput>;

export const CancelBookingInput = z.strictObject({
  rideId: id,
  reason: z.enum(["host_late", "plans_changed", "found_another_ride", "other"]),
});
export type CancelBookingInput = z.infer<typeof CancelBookingInput>;

// ---- 8.7 Trip day (Phase 8) ----

export const StartRideInput = z.strictObject({ rideId: id });
export type StartRideInput = z.infer<typeof StartRideInput>;

export const CheckInInput = z.strictObject({
  rideId: id,
  lat: z.number().min(-90).max(90),
  lng: z.number().min(-180).max(180),
  accuracyM: z.number().min(0),
});
export type CheckInInput = z.infer<typeof CheckInInput>;

export const MarkBoardedInput = z.strictObject({ rideId: id, riderUid: id });
export type MarkBoardedInput = z.infer<typeof MarkBoardedInput>;

export const ReportLateInput = z.strictObject({
  rideId: id,
  riderUid: id,
  kind: z.enum(["late", "no_show"]),
});
export type ReportLateInput = z.infer<typeof ReportLateInput>;

export const DisputeFeeInput = z.strictObject({ feeId: id, text: text(1, 300) });
export type DisputeFeeInput = z.infer<typeof DisputeFeeInput>;

export const ResolveFeeInput = z.strictObject({
  feeId: id,
  decision: z.enum(["keep", "waive"]),
  note,
});
export type ResolveFeeInput = z.infer<typeof ResolveFeeInput>;

export const MarkFeePaidInput = z.strictObject({ feeId: id });
export type MarkFeePaidInput = z.infer<typeof MarkFeePaidInput>;

export const ConfirmSeatPaymentInput = z.strictObject({ rideId: id, riderUid: id });
export type ConfirmSeatPaymentInput = z.infer<typeof ConfirmSeatPaymentInput>;

export const SendChatMessageInput = z.strictObject({
  rideId: id,
  riderUid: id,
  text: text(1, 500), // trimmed at the ends only; inner lines and spaces stay
  requestId,
});
export type SendChatMessageInput = z.infer<typeof SendChatMessageInput>;

export const GetContactPhoneInput = z.strictObject({ rideId: id, otherUid: id });
export type GetContactPhoneInput = z.infer<typeof GetContactPhoneInput>;

export const FinishRideInput = z.strictObject({ rideId: id });
export type FinishRideInput = z.infer<typeof FinishRideInput>;

// Tags must also belong to RATING_TAGS[roleRated]; submitRating checks that.
export const SubmitRatingInput = z.strictObject({
  rideId: id,
  toUid: id,
  stars: z.union([z.literal(1), z.literal(2), z.literal(3), z.literal(4), z.literal(5)]),
  tags: z
    .array(ratingTag)
    .max(5)
    .refine((list) => new Set(list).size === list.length, "A tag twice"),
});
export type SubmitRatingInput = z.infer<typeof SubmitRatingInput>;
