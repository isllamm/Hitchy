import { describe, expect, it } from "vitest";
import { AppFailure, fail, REASON_CODES, type Reason, toHttps } from "../../../src/lib/errors.js";

// Appendix D, copied on purpose: changing a reason or code means changing this table too.
const APPENDIX_D: Record<string, string> = {
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
};

describe("error reasons", () => {
  it("has exactly the server reasons of Appendix D", () => {
    expect(REASON_CODES).toEqual(APPENDIX_D);
  });

  it.each(Object.entries(APPENDIX_D))("%s becomes an HttpsError with code %s", (reason, code) => {
    const error = toHttps(new AppFailure(reason as Reason));
    expect(error.code).toBe(code);
    expect(error.message).toBe(reason);
  });

  it("puts the reason and extra in details", () => {
    const error = toHttps(new AppFailure("KYC_MISSING_FILES", ["idFront", "idBack"]));
    expect(error.details).toEqual({ reason: "KYC_MISSING_FILES", extra: ["idFront", "idBack"] });
  });

  it("sends extra as null when there is none", () => {
    expect(toHttps(new AppFailure("RIDE_FULL")).details).toEqual({
      reason: "RIDE_FULL",
      extra: null,
    });
  });

  it("fail() throws an AppFailure with the reason", () => {
    expect(() => fail("NOT_FOUND")).toThrow(AppFailure);
    try {
      fail("RIDE_FULL", { seatsLeft: 0 });
    } catch (e) {
      expect(e).toMatchObject({ reason: "RIDE_FULL", extra: { seatsLeft: 0 } });
    }
  });
});
