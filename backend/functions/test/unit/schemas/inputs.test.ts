import { describe, expect, it } from "vitest";
import type { z } from "zod";
import * as inputs from "../../../src/schemas/inputs.js";

const REQUEST_ID = "3f6c1a2e-8b4d-4c1e-9f2a-7d5e6b8c9a01";
const DEPART_AT = "2026-11-02T05:30:00Z";

// One valid and one invalid example per schema (P1-B03).
const cases: [string, z.ZodType, unknown, unknown][] = [
  [
    "CompleteProfileInput",
    inputs.CompleteProfileInput,
    {
      firstName: "Omar",
      lastName: "Hassan",
      gender: "male",
      companyId: "acme",
      companyOther: null,
      workHours: { start: "08:00", end: "17:00" },
      language: "ar",
      referralCode: null,
    },
    {
      firstName: "   ",
      lastName: "Hassan",
      gender: "male",
      companyId: null,
      companyOther: null,
      workHours: null,
      language: "ar",
      referralCode: null,
    },
  ],
  ["SwitchRoleInput", inputs.SwitchRoleInput, { role: "host" }, { role: "driver" }],
  [
    "AddVehicleInput",
    inputs.AddVehicleInput,
    {
      type: "car",
      make: "Hyundai",
      model: "Elantra",
      year: 2021,
      color: "Silver",
      plate: "س م و ٢٤٧١",
    },
    {
      type: "car",
      make: "Hyundai",
      model: "Elantra",
      year: 1989,
      color: "Silver",
      plate: "ABC 123",
    },
  ],
  [
    "SubmitHostApplicationInput",
    inputs.SubmitHostApplicationInput,
    { vehicleId: "v1" },
    { vehicleId: "users/v1" },
  ],
  ["ApproveHostInput", inputs.ApproveHostInput, { uid: "u1", vehicleId: "v1" }, { uid: "u1" }],
  [
    "RejectHostInput",
    inputs.RejectHostInput,
    { uid: "u1", rejections: [{ doc: "idFront", reason: "blurry" }], note: null },
    { uid: "u1", rejections: [], note: null },
  ],
  [
    "ApproveVehicleInput",
    inputs.ApproveVehicleInput,
    { uid: "u1", vehicleId: "v1", approve: true },
    { uid: "u1", vehicleId: "v1", approve: "yes" },
  ],
  ["SetVerifiedInput", inputs.SetVerifiedInput, { uid: "u1", verified: true }, { uid: "u1" }],
  [
    "SetGenderInput",
    inputs.SetGenderInput,
    { uid: "u1", gender: "female" },
    { uid: "u1", gender: "other" },
  ],
  ["RequestVerificationInput", inputs.RequestVerificationInput, {}, { extra: 1 }],
  [
    "UpdateConfigInput",
    inputs.UpdateConfigInput,
    { name: "limits", value: { version: 2 } },
    { name: "admins", value: {} },
  ],
  [
    "QuotePriceInput",
    inputs.QuotePriceInput,
    {
      vehicleId: "v1",
      fromZone: "fifth_settlement",
      toZone: "smart_village",
      departAt: DEPART_AT,
      seats: 3,
    },
    {
      vehicleId: "v1",
      fromZone: "fifth_settlement",
      toZone: "smart_village",
      departAt: DEPART_AT,
      seats: 4,
    },
  ],
  [
    "CreateRideInput",
    inputs.CreateRideInput,
    {
      vehicleId: "v1",
      fromZone: "fifth_settlement",
      fromPointId: "gate_1",
      toZone: "smart_village",
      toPointId: "gate_2",
      stops: [{ zone: "rehab", pointId: "gate_3", at: "2026-11-02T05:45:00Z" }],
      direction: "to_work",
      departAt: DEPART_AT,
      seats: 3,
      pricePerSeat: 56,
      womenOnly: false,
      requestId: REQUEST_ID,
    },
    {
      vehicleId: "v1",
      fromZone: "fifth_settlement",
      fromPointId: "gate_1",
      toZone: "smart_village",
      toPointId: "gate_2",
      stops: [],
      direction: "to_work",
      departAt: "2026-11-02T07:30:00+02:00",
      seats: 3,
      pricePerSeat: 56,
      womenOnly: false,
      requestId: REQUEST_ID,
    },
  ],
  [
    "CancelRideInput",
    inputs.CancelRideInput,
    { rideId: "r1", reason: "sick" },
    { rideId: "r1", reason: "bored" },
  ],
  [
    "SearchRidesInput",
    inputs.SearchRidesInput,
    {
      fromZone: "fifth_settlement",
      toZone: "smart_village",
      fromPointId: null,
      date: "2026-11-02",
      time: "07:30",
      seats: 1,
      sort: "recommended",
      timeBand: "any",
      maxPrice: null,
      minRating: null,
      verifiedOnly: false,
      womenOnly: false,
    },
    {
      fromZone: "fifth_settlement",
      toZone: "smart_village",
      fromPointId: null,
      date: "2026-02-30",
      time: "07:30",
      seats: 1,
      sort: "recommended",
      timeBand: "any",
      maxPrice: null,
      minRating: null,
      verifiedOnly: false,
      womenOnly: false,
    },
  ],
  [
    "RequestSeatsInput",
    inputs.RequestSeatsInput,
    { rideId: "r1", seats: 2, pickupId: "s1", requestId: REQUEST_ID },
    { rideId: "r1", seats: 2, pickupId: "s1", requestId: "abc" },
  ],
  [
    "RespondToRequestInput",
    inputs.RespondToRequestInput,
    { rideId: "r1", riderUid: "u2", accept: true },
    { rideId: "r1", riderUid: "u2" },
  ],
  [
    "CancelBookingInput",
    inputs.CancelBookingInput,
    { rideId: "r1", reason: "found_another_ride" },
    { rideId: "r1", reason: "late" },
  ],
  ["StartRideInput", inputs.StartRideInput, { rideId: "r1" }, {}],
  [
    "CheckInInput",
    inputs.CheckInInput,
    { rideId: "r1", lat: 30.07, lng: 31.47, accuracyM: 12 },
    { rideId: "r1", lat: 91, lng: 31.47, accuracyM: 12 },
  ],
  [
    "MarkBoardedInput",
    inputs.MarkBoardedInput,
    { rideId: "r1", riderUid: "u2" },
    { rideId: "r1", riderUid: "" },
  ],
  [
    "ReportLateInput",
    inputs.ReportLateInput,
    { rideId: "r1", riderUid: "u2", kind: "no_show" },
    { rideId: "r1", riderUid: "u2", kind: "early" },
  ],
  [
    "DisputeFeeInput",
    inputs.DisputeFeeInput,
    { feeId: "f1", text: "I was at the gate on time" },
    { feeId: "f1", text: "x".repeat(301) },
  ],
  [
    "ResolveFeeInput",
    inputs.ResolveFeeInput,
    { feeId: "f1", decision: "waive", note: null },
    { feeId: "f1", decision: "refund", note: null },
  ],
  ["MarkFeePaidInput", inputs.MarkFeePaidInput, { feeId: "f1" }, { feeId: 5 }],
  [
    "ConfirmSeatPaymentInput",
    inputs.ConfirmSeatPaymentInput,
    { rideId: "r1", riderUid: "u2" },
    { rideId: "r1" },
  ],
  [
    "SendChatMessageInput",
    inputs.SendChatMessageInput,
    { rideId: "r1", riderUid: "u2", text: "On my way", requestId: REQUEST_ID },
    { rideId: "r1", riderUid: "u2", text: "   ", requestId: REQUEST_ID },
  ],
  [
    "GetContactPhoneInput",
    inputs.GetContactPhoneInput,
    { rideId: "r1", otherUid: "u2" },
    { rideId: "r1", otherUid: "u 2" },
  ],
  ["FinishRideInput", inputs.FinishRideInput, { rideId: "r1" }, { rideId: "r1", extra: true }],
  [
    "SubmitRatingInput",
    inputs.SubmitRatingInput,
    { rideId: "r1", toUid: "u1", stars: 5, tags: ["on_time", "followed_route"] },
    { rideId: "r1", toUid: "u1", stars: 6, tags: [] },
  ],
  [
    "ArriveAtStopInput",
    inputs.ArriveAtStopInput,
    { rideId: "r1", pickupId: "start" },
    { rideId: "r1", pickupId: "s4" },
  ],
  [
    "ConfirmRidePaymentInput",
    inputs.ConfirmRidePaymentInput,
    { rideId: "r1", requestId: REQUEST_ID },
    { rideId: "r1", requestId: REQUEST_ID, riderUid: "u2" },
  ],
  [
    "TopUpWalletInput",
    inputs.TopUpWalletInput,
    { uid: "u1", amount: 250, requestId: REQUEST_ID },
    { uid: "u1", amount: 0, requestId: REQUEST_ID },
  ],
];

describe("callable input schemas", () => {
  it("covers every schema in inputs.ts", () => {
    expect(cases.map(([name]) => name).sort()).toEqual(Object.keys(inputs).sort());
  });

  it.each(cases)("%s accepts a valid input", (_name, schema, valid) => {
    expect(schema.safeParse(valid).success).toBe(true);
  });

  it.each(cases)("%s rejects an invalid input", (_name, schema, _valid, invalid) => {
    expect(schema.safeParse(invalid).success).toBe(false);
  });

  it("rejects unknown fields", () => {
    const result = inputs.SwitchRoleInput.safeParse({ role: "host", admin: true });
    expect(result.success).toBe(false);
  });

  it("trims text before checking its length", () => {
    const result = inputs.AddVehicleInput.parse({
      type: "car",
      make: "  Hyundai ",
      model: "Elantra",
      year: 2021,
      color: "Silver",
      plate: " ABC 123 ",
    });
    expect(result.make).toBe("Hyundai");
    expect(result.plate).toBe("ABC 123");
  });

  it("keeps the inner lines and spaces of chat text", () => {
    const result = inputs.SendChatMessageInput.parse({
      rideId: "r1",
      riderUid: "u2",
      text: " Hi\n  I'm at gate 2 ",
      requestId: REQUEST_ID,
    });
    expect(result.text).toBe("Hi\n  I'm at gate 2");
  });

  it("accepts only real 24-hour times", () => {
    for (const time of ["23:59", "00:00"]) {
      expect(inputs.SearchRidesInput.shape.time.safeParse(time).success).toBe(true);
    }
    for (const time of ["24:00", "7:30", "07:60"]) {
      expect(inputs.SearchRidesInput.shape.time.safeParse(time).success).toBe(false);
    }
  });

  it("accepts only 4.5 or 4.8 as a minimum rating", () => {
    expect(inputs.SearchRidesInput.shape.minRating.safeParse(4.8).success).toBe(true);
    expect(inputs.SearchRidesInput.shape.minRating.safeParse(4.6).success).toBe(false);
  });

  it("accepts 0 to 3 ride stops and refuses a fourth (v1.2 [DECISION-28])", () => {
    const stop = { zone: "rehab", pointId: "gate_3", at: DEPART_AT };
    const stops = inputs.CreateRideInput.shape.stops;

    expect(stops.safeParse([]).success).toBe(true);
    expect(stops.safeParse([stop, stop, stop]).success).toBe(true);
    expect(stops.safeParse([stop, stop, stop, stop]).success).toBe(false);
  });

  it("accepts only the start point or a stop id as a pickup (v1.2)", () => {
    const pickupId = inputs.RequestSeatsInput.shape.pickupId;

    for (const value of ["start", "s1", "s2", "s3"]) {
      expect(pickupId.safeParse(value).success).toBe(true);
    }
    for (const value of ["s0", "s4", "stop1", "START", ""]) {
      expect(pickupId.safeParse(value).success).toBe(false);
    }
  });

  it("accepts a top-up of 1 to 5000 whole EGP (v1.2 [DECISION-32])", () => {
    const amount = inputs.TopUpWalletInput.shape.amount;

    expect(amount.safeParse(1).success).toBe(true);
    expect(amount.safeParse(5000).success).toBe(true);
    for (const value of [0, -250, 5001, 250.5]) {
      expect(amount.safeParse(value).success).toBe(false);
    }
  });

  it("rejects a document or tag listed twice", () => {
    const twice = { doc: "idFront", reason: "blurry" };
    expect(
      inputs.RejectHostInput.safeParse({ uid: "u1", rejections: [twice, twice], note: null })
        .success,
    ).toBe(false);
    expect(
      inputs.SubmitRatingInput.safeParse({
        rideId: "r1",
        toUid: "u1",
        stars: 4,
        tags: ["late", "late"],
      }).success,
    ).toBe(false);
  });
});
