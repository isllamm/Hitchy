import { readdirSync, readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { z } from "zod";
import { REASON_CODES } from "../../src/lib/errors.js";
import * as inputs from "../../src/schemas/inputs.js";
import type {
  AddVehicleOutput,
  ApproveHostOutput,
  CancelBookingOutput,
  CancelRideOutput,
  CheckInOutput,
  CompleteProfileOutput,
  CreateRideOutput,
  EmptyOutput,
  FinishRideOutput,
  GetContactPhoneOutput,
  QuotePriceOutput,
  ReportLateOutput,
  RequestSeatsOutput,
  RespondToRequestOutput,
  RideCard,
  SearchRidesOutput,
  SendChatMessageOutput,
  SubmitHostApplicationOutput,
  SwitchRoleOutput,
  UpdateConfigOutput,
} from "../../src/schemas/io.js";

const fixturesDir = resolve(import.meta.dirname, "../../../../contract/fixtures");
const errorsMdPath = resolve(import.meta.dirname, "../../../../contract/errors.md");

// Strict ISO UTC with milliseconds: "2026-11-02T05:30:00.000Z"
const isoUtcWithMs = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/, "Must be ISO-8601 UTC with milliseconds");

// ---- Zod Mirrors strictly typed against io.ts TypeScript types ----
// If io.ts changes, these definitions will fail to compile.

const CompleteProfileOutputSchema: z.ZodType<CompleteProfileOutput> = z.strictObject({
  activeRole: z.enum(["rider", "host"]),
  hostStatus: z.enum(["none", "pending", "approved", "rejected"]),
  referralApplied: z.boolean(),
});

const SwitchRoleOutputSchema: z.ZodType<SwitchRoleOutput> = z.strictObject({
  activeRole: z.enum(["rider", "host"]),
});

const AddVehicleOutputSchema: z.ZodType<AddVehicleOutput> = z.strictObject({
  vehicleId: z.string().min(1),
});

const SubmitHostApplicationOutputSchema: z.ZodType<SubmitHostApplicationOutput> = z.strictObject({
  status: z.literal("submitted"),
});

const ApproveHostOutputSchema: z.ZodType<ApproveHostOutput> = z.strictObject({
  freeUntil: isoUtcWithMs,
});

const EmptyOutputSchema: z.ZodType<EmptyOutput> = z.strictObject({});

const UpdateConfigOutputSchema: z.ZodType<UpdateConfigOutput> = z.strictObject({
  version: z.number().int().min(1),
});

const QuotePriceOutputSchema: z.ZodType<QuotePriceOutput> = z.strictObject({
  min: z.number().int().min(1),
  recommended: z.number().int().min(1),
  max: z.number().int().min(1),
  trafficLevel: z.enum(["offPeak", "normal", "peak"]),
  distanceKm: z.number().positive(),
  fuelCostEgp: z.number().int().nonnegative(),
  tripCostEgp: z.number().int().nonnegative(),
  configVersion: z.number().int().min(1),
});

const CreateRideOutputSchema: z.ZodType<CreateRideOutput> = z.strictObject({
  rideId: z.string().min(1),
});

const CancelRideOutputSchema: z.ZodType<CancelRideOutput> = z.strictObject({
  cancelledBookings: z.number().int().nonnegative(),
});

const LocalizedNameSchema = z.strictObject({
  ar: z.string().min(1),
  en: z.string().min(1),
});

const RideCardSchema: z.ZodType<RideCard> = z.strictObject({
  rideId: z.string().min(1),
  departAt: isoUtcWithMs,
  arriveAt: isoUtcWithMs,
  fromZone: z.string().min(1),
  toZone: z.string().min(1),
  fromPointId: z.string().min(1),
  toPointId: z.string().min(1),
  fromPointName: LocalizedNameSchema,
  toPointName: LocalizedNameSchema,
  pricePerSeat: z.number().int().min(1),
  fairPrice: z.boolean(),
  seatsLeft: z.number().int().nonnegative(),
  walkM: z.number().int().nonnegative().nullable(),
  bestMatch: z.boolean(),
  womenOnly: z.boolean(),
  vehicleType: z.enum(["car", "scooter"]),
  fixed: z.boolean(),
  host: z.strictObject({
    uid: z.string().min(1),
    firstName: z.string().min(1),
    photoPath: z.string().nullable(),
    rating: z.number().nullable(),
    ratingCount: z.number().int().nonnegative(),
    trips: z.number().int().nonnegative(),
    verified: z.boolean(),
    tier: z.enum(["bronze", "silver", "gold"]),
    sameCompany: z.boolean(),
  }),
});

const SearchRidesOutputSchema: z.ZodType<SearchRidesOutput> = z.strictObject({
  rides: z.array(RideCardSchema),
});

const RequestSeatsOutputSchema: z.ZodType<RequestSeatsOutput> = z.strictObject({
  booking: z.strictObject({
    rideId: z.string().min(1),
    status: z.literal("requested"),
    seats: z.number().int().min(1),
    price: z.number().int().min(1),
    total: z.number().int().min(1),
    expiresAt: isoUtcWithMs,
  }),
  ride: RideCardSchema,
});

const RespondToRequestOutputSchema: z.ZodType<RespondToRequestOutput> = z.strictObject({
  status: z.enum(["booked", "declined"]),
});

const CancelBookingOutputSchema: z.ZodType<CancelBookingOutput> = z.strictObject({
  lateCancel: z.boolean(),
});

const CheckInOutputSchema: z.ZodType<CheckInOutput> = z.strictObject({
  distanceM: z.number().nonnegative(),
});

const ReportLateOutputSchema: z.ZodType<ReportLateOutput> = z.strictObject({
  feeId: z.string().min(1),
});

const SendChatMessageOutputSchema: z.ZodType<SendChatMessageOutput> = z.strictObject({
  messageId: z.string().min(1),
});

const GetContactPhoneOutputSchema: z.ZodType<GetContactPhoneOutput> = z.strictObject({
  phone: z.string().regex(/^\+20\d{10}$/),
});

const FinishRideOutputSchema: z.ZodType<FinishRideOutput> = z.strictObject({
  completedBookings: z.number().int().nonnegative(),
  creditUsed: z.boolean(),
});

// Map of output schemas by callable name
const outputSchemas: Record<string, z.ZodType> = {
  completeProfile: CompleteProfileOutputSchema,
  switchRole: SwitchRoleOutputSchema,
  addVehicle: AddVehicleOutputSchema,
  submitHostApplication: SubmitHostApplicationOutputSchema,
  approveHost: ApproveHostOutputSchema,
  rejectHost: EmptyOutputSchema,
  approveVehicle: EmptyOutputSchema,
  setVerified: EmptyOutputSchema,
  setGender: EmptyOutputSchema,
  requestVerification: EmptyOutputSchema,
  updateConfig: UpdateConfigOutputSchema,
  quotePrice: QuotePriceOutputSchema,
  createRide: CreateRideOutputSchema,
  cancelRide: CancelRideOutputSchema,
  searchRides: SearchRidesOutputSchema,
  requestSeats: RequestSeatsOutputSchema,
  respondToRequest: RespondToRequestOutputSchema,
  cancelBooking: CancelBookingOutputSchema,
  startRide: EmptyOutputSchema,
  checkIn: CheckInOutputSchema,
  markBoarded: EmptyOutputSchema,
  reportLate: ReportLateOutputSchema,
  disputeFee: EmptyOutputSchema,
  resolveFee: EmptyOutputSchema,
  markFeePaid: EmptyOutputSchema,
  confirmSeatPayment: EmptyOutputSchema,
  sendChatMessage: SendChatMessageOutputSchema,
  getContactPhone: GetContactPhoneOutputSchema,
  finishRide: FinishRideOutputSchema,
  submitRating: EmptyOutputSchema,
};

function inputExportToCallableName(exportName: string): string {
  const base = exportName.replace(/Input$/, "");
  return base.charAt(0).toLowerCase() + base.slice(1);
}

// Dynamically discover all callables from inputs.ts
const callableEntries = Object.entries(inputs)
  .filter(
    ([key, value]) => key.endsWith("Input") && typeof value === "object" && "safeParse" in value,
  )
  .map(([exportName, schema]) => ({
    name: inputExportToCallableName(exportName),
    exportName,
    schema: schema as z.ZodType,
  }));

describe("Contract v1 fixtures", () => {
  it("dynamically discovers all callables from inputs.ts", () => {
    expect(callableEntries.length).toBeGreaterThan(0);
    for (const { name } of callableEntries) {
      expect(outputSchemas[name]).toBeDefined();
    }
  });

  it("ensures every callable has both request and response fixtures and no extra files exist", () => {
    const files = readdirSync(fixturesDir).filter((f) => f.endsWith(".json"));

    const expectedFiles = new Set<string>();
    for (const { name } of callableEntries) {
      expectedFiles.add(`${name}.request.json`);
      expectedFiles.add(`${name}.response.json`);
    }

    // Verify all expected files exist
    for (const expected of expectedFiles) {
      expect(files, `Missing fixture file: ${expected} in contract/fixtures/`).toContain(expected);
    }

    // Verify no extra files exist
    for (const file of files) {
      expect(
        expectedFiles.has(file),
        `Unexpected extra file found in contract/fixtures/: ${file}`,
      ).toBe(true);
    }

    expect(files.length).toBe(callableEntries.length * 2);
  });

  it.each(callableEntries)(
    "validates $name.request.json strictly against its zod input schema",
    ({ name, schema }) => {
      const filePath = resolve(fixturesDir, `${name}.request.json`);
      const raw = readFileSync(filePath, "utf-8");
      const parsedJson: unknown = JSON.parse(raw);

      const result = schema.safeParse(parsedJson);
      if (!result.success) {
        throw new Error(
          `Request fixture validation failed for ${name}: ${JSON.stringify(result.error.issues, null, 2)}`,
        );
      }
      expect(result.success).toBe(true);
    },
  );

  it.each(callableEntries)(
    "validates $name.response.json strictly against its zod output mirror",
    ({ name }) => {
      const outputSchema = outputSchemas[name];
      if (!outputSchema) {
        throw new Error(`Missing output schema for callable: ${name}`);
      }

      const filePath = resolve(fixturesDir, `${name}.response.json`);
      const raw = readFileSync(filePath, "utf-8");
      const parsedJson: unknown = JSON.parse(raw);

      const result = outputSchema.safeParse(parsedJson);
      if (!result.success) {
        throw new Error(
          `Response fixture validation failed for ${name}: ${JSON.stringify(result.error.issues, null, 2)}`,
        );
      }
      expect(result.success).toBe(true);
    },
  );
});

describe("Contract error catalog vs server Reason codes", () => {
  it("matches all server Reason codes with the reasons documented in contract/errors.md", () => {
    const errorsMd = readFileSync(errorsMdPath, "utf-8");

    // Extract reasons from backticks in table rows: | `REASON_NAME` |
    const reasonRegex = /\|\s*`([A-Z0-9_]+)`\s*\|/g;
    const foundReasons = new Set<string>();
    let match;
    while ((match = reasonRegex.exec(errorsMd)) !== null) {
      const reason = match[1];
      if (reason) {
        foundReasons.add(reason);
      }
    }

    // App-only reasons listed in Section 3 of errors.md
    const appOnlyReasons = new Set([
      "OTP_INVALID",
      "OTP_EXPIRED",
      "TOO_MANY_ATTEMPTS",
      "SMS_QUOTA",
      "NETWORK",
    ]);

    // Check that every server Reason in errors.ts is documented in contract/errors.md
    const serverReasons = Object.keys(REASON_CODES);
    for (const reason of serverReasons) {
      expect(
        foundReasons.has(reason),
        `Server Reason "${reason}" is missing in contract/errors.md`,
      ).toBe(true);
    }

    // Check that every documented reason in errors.md is either a server Reason or app-only
    for (const reason of foundReasons) {
      const isKnown = reason in REASON_CODES || appOnlyReasons.has(reason);
      expect(isKnown, `contract/errors.md documents unknown reason "${reason}"`).toBe(true);
    }
  });
});
