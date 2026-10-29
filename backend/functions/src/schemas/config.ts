// Types of the business config documents config/{name} (BUILD_PLAN section 7, v1.1).
// No Firebase imports: the admin web imports this file too. P5-B05 adds the zod schemas
// here and derives these types from them (ADR 0003).
import type { HHmm, Tier, TrafficLevel } from "./docs.js";

// Windows include "from" and exclude "to".
export interface TimeWindow {
  from: HHmm;
  to: HHmm;
}

// config/pricing (7.1)
export interface PricingConfig {
  version: number;
  fuelPriceEgpPerLiter: number;
  litersPer100Km: { car: number; scooter: number | null }; // scooter null = disabled [DECISION-07]
  runningCostEgpPerKm: { car: number; scooter: number | null };
  trafficFactor: Record<TrafficLevel, number>;
  peakWindows: TimeWindow[];
  normalWindows: TimeWindow[]; // [DECISION-06]
  marginCap: number;
  maxSeats: { car: number; scooter: number };
}

// config/packages (7.2)
export interface PackageItem {
  id: string;
  trips: number;
  priceEgp: number;
  bucket: "regular" | "fixed";
}
export interface PackagesConfig {
  version: number;
  items: PackageItem[];
  freeMonthDays: number;
  instapayAddress: string;
  maxPendingOrders: number;
  lowBalanceAlert: number;
}

// config/limits (7.3)
export interface LimitsConfig {
  version: number;
  minLeadMinutes: number;
  maxDaysAhead: number;
  rideGapMinutes: number;
  maxRidesPerHostPerDay: number;
  bookingCutoffMinutes: number;
  freeCancelMinutes: number; // [DECISION-08]
  hostLateCancelMinutes: number;
  startWindowMinutes: number;
  lateGraceMinutes: number;
  lateFeeEgp: number;
  noShowMinutes: number; // [DECISION-09]
  checkInRadiusM: number;
  hostLateMinutes: number;
  hostLateDistanceM: number;
  presenceIntervalSec: number;
  presenceMaxSamples: number;
  togetherMinSamples: number;
  togetherMaxGapSec: number;
  togetherMaxDistanceM: number;
  staleRideHours: number;
  expireNotStartedHours: number;
  ratingWindowDays: number;
  lowRatingThreshold: number;
  maxVehiclesPerHost: number;
  searchMaxResults: number;
  searchWindowMinutes: number; // [DECISION-25]
  requestTtlMinutes: number; // [DECISION-21]
  arrivedDistanceM: number; // [DECISION-26]
  etaSpeedKmh: number; // [DECISION-26]
  chatOpenHoursAfterRide: number; // [DECISION-23]
  chatRetentionDays: number; // [DECISION-23]
  notificationRetentionDays: number; // [DECISION-27]
  maxSavedPlaces: number;
  aiDailyMessages: number;
  aiMonthlyBudgetEgp: number;
  aiEgpPer1kInputTokens: number; // [DECISION-12]
  aiEgpPer1kOutputTokens: number; // [DECISION-12]
  fixedSkipCutoff: HHmm; // [DECISION-13]
  backupWindowMinutes: number;
  backupPriorityDelaySec: number;
  deleteBlockHours: number;
  kycPhotoRetentionDays: number; // [DECISION-14]
  appCheckEnforced: boolean;
  assistantEnabled: boolean;
  fixedRideEnabled: boolean;
  rewardsEnabled: boolean;
}

// config/rewards (7.4)
export interface RewardsConfig {
  version: number;
  perTrip: number;
  onTimeWeek: number;
  fiveStar: number;
  fixedWeek: number;
  referral: number;
  referralTrips: number;
  tiers: { silver: { trips: number; rating: number }; gold: { trips: number; rating: number } };
  tierWindowDays: number;
  hostMultiplier: Record<Tier, number>;
  redeem: { points: number; trips: number };
  expiryMonths: number;
  maxPairTripsPerDay: number;
}

// config/impact (7.5)
export interface ImpactConfig {
  version: number;
  shareOfSeatsReplacingSoloTrip: number;
  co2KgPerLiter: number;
}

export interface ConfigDocs {
  pricing: PricingConfig;
  packages: PackagesConfig;
  limits: LimitsConfig;
  rewards: RewardsConfig;
  impact: ImpactConfig;
}
export type ConfigName = keyof ConfigDocs;
