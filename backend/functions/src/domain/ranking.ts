// Ride ranking for the "recommended" sort (BUILD_PLAN 8.6) — pure logic, no Firebase imports.
// The weights are constants so they can be tuned without touching the formula.
import type { Tier } from "../schemas/docs.js";

export const WEIGHTS = {
  timeFit: 40,
  sameCompany: 20,
  rating: 15,
  verified: 10,
  reliability: 5,
} as const;

export const TIER_BONUS: Record<Tier, number> = { bronze: 0, silver: 5, gold: 10 };

// How far from the picked time a ride can be before timeFit reaches 0.
export const TIME_FIT_WINDOW_MINUTES = 120;

export interface RankInput {
  // The chosen pickup's time, in milliseconds (v1.2: the rider's own pickup, not departAt).
  pickupAtMs: number;
  pickedTimeMs: number;
  sameCompany: boolean;
  ratingAvg: number | null;
  ratingCount: number;
  verified: boolean;
  tier: Tier;
  lateCancels: number;
  hostLateCount: number;
  tripsCompleted: number;
}

const clamp = (x: number, low: number, high: number): number => Math.min(high, Math.max(low, x));

export function timeFit(pickupAtMs: number, pickedTimeMs: number): number {
  const minutesApart = Math.abs(pickupAtMs - pickedTimeMs) / 60000;
  return Math.max(0, 1 - minutesApart / TIME_FIT_WINDOW_MINUTES);
}

// Fewer than 3 ratings is neither good nor bad, so it scores in the middle.
export function ratingScore(avg: number | null, count: number): number {
  if (count < 3 || avg == null) return 0.5;
  return clamp((avg - 3) / 2, 0, 1);
}

export function reliability(input: {
  lateCancels: number;
  hostLateCount: number;
  tripsCompleted: number;
}): number {
  const letDowns = input.lateCancels + input.hostLateCount;
  return 1 - Math.min(1, letDowns / Math.max(5, input.tripsCompleted));
}

export function rankScore(input: RankInput): number {
  return (
    WEIGHTS.timeFit * timeFit(input.pickupAtMs, input.pickedTimeMs) +
    WEIGHTS.sameCompany * (input.sameCompany ? 1 : 0) +
    WEIGHTS.rating * ratingScore(input.ratingAvg, input.ratingCount) +
    WEIGHTS.verified * (input.verified ? 1 : 0) +
    TIER_BONUS[input.tier] +
    WEIGHTS.reliability * reliability(input)
  );
}
