// Builders for the integration tests: complete documents (section 6) with fake values and no
// personal data. They use the Admin SDK Timestamp, so they can be written straight to the
// emulator the way the functions do.
import { Timestamp } from "firebase-admin/firestore";
import type { LimitsConfig, PricingConfig } from "../../src/schemas/config.js";
import type { ProfileDoc, RideDoc, UserDoc, VehicleDoc } from "../../src/schemas/docs.js";

export const FIXED_NOW = new Date("2026-11-02T05:30:00Z"); // 07:30 Cairo, inside a peak window

export function ts(date: Date): Timestamp {
  return Timestamp.fromDate(date);
}

export function userDoc(overrides: Partial<UserDoc> = {}): UserDoc {
  const at = ts(FIXED_NOW);
  const base: UserDoc = {
    schemaVersion: 1,
    createdAt: at,
    updatedAt: at,
    phone: "test-phone",
    firstName: "Test",
    lastName: "User",
    gender: "male",
    companyId: null,
    companyOther: null,
    workHours: null,
    language: "en",
    photoPath: null,
    emergencyContact: null,
    savedPlaces: [],
    activeRole: "host",
    referralCode: "TESTCODE",
    referredBy: null,
    referralRewarded: false,
    blockedUids: [],
    suspended: false,
    rider: {
      tier: "bronze",
      ratingSum: 0,
      ratingCount: 0,
      tripsCompleted: 0,
      unpaidFees: 0,
      lateCancels: 0,
    },
    host: {
      status: "approved",
      verified: false,
      approvedAt: at,
      freeUntil: null,
      freeEndingNotified: false,
      credits: { regular: 10, fixed: 0 },
      tier: "bronze",
      ratingSum: 0,
      ratingCount: 0,
      tripsCompleted: 0,
      lateCancels: 0,
      hostLateCount: 0,
    },
    points: { balance: 0 },
  };
  return { ...base, ...overrides };
}

export function vehicleDoc(overrides: Partial<VehicleDoc> = {}): VehicleDoc {
  const at = ts(FIXED_NOW);
  const base: VehicleDoc = {
    schemaVersion: 1,
    createdAt: at,
    updatedAt: at,
    type: "car",
    make: "TestMake",
    model: "TestModel",
    year: 2022,
    color: "Silver",
    plate: "TEST-PLATE",
    maxSeats: 3,
    status: "approved",
  };
  return { ...base, ...overrides };
}

/**
 * config/pricing with the numbers of BUILD_PLAN 7.1, so the golden price table of 8.5 holds.
 * Tests write their own config instead of trusting the seed, whose demo values are looser
 * (ADR 0003 row 81).
 */
export function pricingConfig(overrides: Partial<PricingConfig> = {}): PricingConfig {
  const base: PricingConfig = {
    version: 1,
    fuelPriceEgpPerLiter: 22.25,
    litersPer100Km: { car: 8, scooter: null },
    runningCostEgpPerKm: { car: 0.75, scooter: null },
    trafficFactor: { offPeak: 1.0, normal: 1.15, peak: 1.3 },
    peakWindows: [
      { from: "07:00", to: "10:00" },
      { from: "16:00", to: "19:00" },
    ],
    normalWindows: [{ from: "10:00", to: "16:00" }],
    marginCap: 0.2,
    maxSeats: { car: 3, scooter: 1 },
  };
  return { ...base, ...overrides };
}

// config/limits with the numbers of BUILD_PLAN 7.3, not the looser demo seed values.
export function limitsConfig(overrides: Partial<LimitsConfig> = {}): LimitsConfig {
  const base: LimitsConfig = {
    version: 1,
    minLeadMinutes: 30,
    maxDaysAhead: 7,
    rideGapMinutes: 90,
    maxRidesPerHostPerDay: 2,
    bookingCutoffMinutes: 15,
    freeCancelMinutes: 60,
    hostLateCancelMinutes: 60,
    startWindowMinutes: 30,
    lateGraceMinutes: 5,
    lateFeeEgp: 10,
    noShowMinutes: 15,
    checkInRadiusM: 150,
    hostLateMinutes: 10,
    hostLateDistanceM: 300,
    presenceIntervalSec: 60,
    presenceMaxSamples: 200,
    togetherMinSamples: 5,
    togetherMaxGapSec: 180,
    togetherMaxDistanceM: 300,
    staleRideHours: 3,
    expireNotStartedHours: 1,
    ratingWindowDays: 7,
    lowRatingThreshold: 2,
    maxVehiclesPerHost: 2,
    searchMaxResults: 20,
    searchWindowMinutes: 60,
    requestTtlMinutes: 30,
    arrivedDistanceM: 300,
    etaSpeedKmh: 25,
    chatOpenHoursAfterRide: 24,
    chatRetentionDays: 30,
    notificationRetentionDays: 30,
    maxSavedPlaces: 5,
    aiDailyMessages: 20,
    aiMonthlyBudgetEgp: 1000,
    aiEgpPer1kInputTokens: 0,
    aiEgpPer1kOutputTokens: 0,
    fixedSkipCutoff: "20:00",
    backupWindowMinutes: 45,
    backupPriorityDelaySec: 120,
    deleteBlockHours: 24,
    kycPhotoRetentionDays: 30,
    appCheckEnforced: false,
    assistantEnabled: false,
    fixedRideEnabled: false,
    rewardsEnabled: false,
  };
  return { ...base, ...overrides };
}

export function profileDoc(overrides: Partial<ProfileDoc> = {}): ProfileDoc {
  const base: ProfileDoc = {
    schemaVersion: 1,
    firstName: "Test",
    photoPath: null,
    hostRating: { avg: null, count: 0 },
    hostTrips: 0,
    riderRating: { avg: null, count: 0 },
    verified: false,
    hostTier: "bronze",
    riderTier: "bronze",
    updatedAt: ts(FIXED_NOW),
  };
  return { ...base, ...overrides };
}

// A ride as createRide would write it (6.5). Tests that are about search or booking write rides
// straight to the emulator instead of going through createRide and its daily limits.
export function rideDoc(overrides: Partial<RideDoc> = {}): RideDoc {
  const at = ts(FIXED_NOW);
  const name = { ar: "نقطة", en: "Point" };
  const base: RideDoc = {
    schemaVersion: 1,
    createdAt: at,
    updatedAt: at,
    hostId: "some-host",
    hostCompanyId: null,
    vehicleId: "some-car",
    vehicle: {
      type: "car",
      make: "TestMake",
      model: "TestModel",
      color: "Silver",
      plate: "TEST-PLATE",
    },
    fromZone: "zone-from",
    fromPointId: "pt1",
    fromPointName: name,
    fromPoint: { lat: 30.0, lng: 31.0 },
    toZone: "zone-to",
    toPointId: "pt1",
    toPointName: name,
    stops: [],
    pickupZones: ["zone-from"],
    direction: "to_work",
    departAt: ts(new Date("2026-11-02T06:30:00Z")),
    dateKey: "2026-11-02",
    seatsTotal: 3,
    seatsLeft: 3,
    pricePerSeat: 56,
    priceRange: {
      min: 42,
      recommended: 56,
      max: 67,
      trafficLevel: "peak",
      distanceKm: 55,
      configVersion: 1,
    },
    womenOnly: false,
    status: "open",
    fixedRideId: null,
    bookingsCount: 0,
    creditConsumed: false,
    postProcessed: false,
    autoFinished: false,
    startedAt: null,
    finishedAt: null,
    cancelledAt: null,
    cancelReason: null,
  };
  return { ...base, ...overrides };
}
