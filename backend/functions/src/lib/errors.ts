// Business error reasons (BUILD_PLAN 5.5, Appendix D). The reasons marked "app only" there
// (OTP_INVALID, OTP_EXPIRED, TOO_MANY_ATTEMPTS, SMS_QUOTA, NETWORK) never come from the server.
import { type FunctionsErrorCode, HttpsError } from "firebase-functions/https";

export const REASON_CODES = {
  UNAUTHENTICATED: "unauthenticated",
  PERMISSION_DENIED: "permission-denied",
  INVALID_INPUT: "invalid-argument",
  PROFILE_REQUIRED: "failed-precondition",
  SUSPENDED: "permission-denied",
  NOT_FOUND: "not-found",
  FEATURE_DISABLED: "failed-precondition",
  INTERNAL: "internal",
  PHONE_REQUIRED: "failed-precondition",
  GENDER_LOCKED: "failed-precondition",
  HOST_NOT_APPROVED: "failed-precondition",
  VEHICLE_LIMIT: "failed-precondition",
  SCOOTER_DISABLED: "failed-precondition",
  KYC_ALREADY_SUBMITTED: "failed-precondition",
  KYC_MISSING_FILES: "failed-precondition",
  KYC_NOT_SUBMITTED: "failed-precondition",
  VEHICLE_NOT_APPROVED: "failed-precondition",
  ROUTE_NOT_SUPPORTED: "failed-precondition",
  SEATS_INVALID: "invalid-argument",
  DEPART_TOO_SOON: "failed-precondition",
  DEPART_TOO_FAR: "failed-precondition",
  PRICE_OUT_OF_RANGE: "invalid-argument",
  WOMEN_ONLY_HOST: "permission-denied",
  DAILY_RIDE_LIMIT: "failed-precondition",
  RIDE_TIME_CONFLICT: "failed-precondition",
  NO_CREDITS: "failed-precondition",
  RIDE_HAS_STARTED: "failed-precondition",
  RIDE_NOT_OPEN: "failed-precondition",
  RIDE_FULL: "failed-precondition",
  OWN_RIDE: "failed-precondition",
  ALREADY_BOOKED: "already-exists",
  UNPAID_FEE: "failed-precondition",
  WOMEN_ONLY: "permission-denied",
  TIME_CONFLICT: "failed-precondition",
  TOO_LATE_TO_BOOK: "failed-precondition",
  BOOKING_NOT_FOUND: "not-found",
  HOST_NOT_LATE: "failed-precondition",
  START_WINDOW_CLOSED: "failed-precondition",
  RIDE_NOT_STARTED: "failed-precondition",
  CHECK_IN_CLOSED: "failed-precondition",
  NOT_ON_RIDE: "permission-denied",
  GRACE_NOT_OVER: "failed-precondition",
  RIDER_WAS_ON_TIME: "failed-precondition",
  FEE_EXISTS: "already-exists",
  FEE_NOT_FOUND: "not-found",
  FEE_NOT_DISPUTABLE: "failed-precondition",
  RATING_NOT_ALLOWED: "failed-precondition",
  RATING_EXISTS: "already-exists",
  RATING_WINDOW_CLOSED: "failed-precondition",
  PACKAGE_NOT_FOUND: "not-found",
  TOO_MANY_PENDING: "resource-exhausted",
  ORDER_NOT_PENDING: "failed-precondition",
  NOT_ENOUGH_POINTS: "failed-precondition",
  AI_DAILY_LIMIT: "resource-exhausted",
  AI_UNAVAILABLE: "unavailable",
  INVITE_INVALID: "not-found",
  FIXED_RIDE_FULL: "failed-precondition",
  FIXED_RIDE_EXISTS: "already-exists",
  SKIP_TOO_LATE: "failed-precondition",
  CANNOT_REPORT_SELF: "invalid-argument",
  ACTIVE_TRIP_SOON: "failed-precondition",
  REQUEST_NOT_PENDING: "failed-precondition",
  REQUEST_EXPIRED: "failed-precondition",
  CHAT_CLOSED: "failed-precondition",
  CONTACT_NOT_ALLOWED: "permission-denied",
  PAYMENT_ALREADY_CONFIRMED: "already-exists",
} as const satisfies Record<string, FunctionsErrorCode>;

export type Reason = keyof typeof REASON_CODES;

export class AppFailure extends Error {
  constructor(
    readonly reason: Reason,
    readonly extra?: unknown,
  ) {
    super(reason);
    this.name = "AppFailure";
  }
}

export function fail(reason: Reason, extra?: unknown): never {
  throw new AppFailure(reason, extra);
}

// The app reads details.reason and maps it to its own message (error_<reason_lowercase>).
export function toHttps(e: AppFailure): HttpsError {
  return new HttpsError(REASON_CODES[e.reason], e.reason, {
    reason: e.reason,
    extra: e.extra ?? null,
  });
}
