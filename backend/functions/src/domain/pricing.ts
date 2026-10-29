// Ride pricing formula and traffic level (BUILD_PLAN 8.5) — pure logic, no Firebase imports.
import { hhmmToMinutes } from "../lib/time.js";
import type { PricingConfig, TimeWindow } from "../schemas/config.js";
import type { TrafficLevel, VehicleType } from "../schemas/docs.js";

export const floorEgp = (x: number): number => Math.floor(Math.round(x * 100) / 100);

export interface QuoteBreakdown {
  min: number;
  recommended: number;
  max: number;
  fuelCostEgp: number;
  tripCostEgp: number;
}

export interface QuoteParams {
  distanceKm: number;
  vehicleType: VehicleType;
  trafficLevel: TrafficLevel;
  seats: number;
}

// Windows include "from" and exclude "to".
function inWindow(minute: number, window: TimeWindow): boolean {
  const from = hhmmToMinutes(window.from);
  const to = hhmmToMinutes(window.to);
  if (from < to) return minute >= from && minute < to;
  if (from > to) return minute >= from || minute < to;
  return false;
}

/**
 * Determines traffic level ("peak", "normal", or "offPeak") from Cairo minute of the day (0 … 1439).
 * Windows include "from" and exclude "to"; anything not peak or normal is offPeak.
 */
export function trafficLevelAt(minuteOfDayCairo: number, c: PricingConfig): TrafficLevel {
  if (!Number.isInteger(minuteOfDayCairo) || minuteOfDayCairo < 0 || minuteOfDayCairo >= 1440) {
    throw new RangeError(
      `minuteOfDayCairo must be an integer between 0 and 1439, got: ${String(minuteOfDayCairo)}`,
    );
  }

  for (const window of c.peakWindows) {
    if (inWindow(minuteOfDayCairo, window)) return "peak";
  }

  for (const window of c.normalWindows) {
    if (inWindow(minuteOfDayCairo, window)) return "normal";
  }

  return "offPeak";
}

/**
 * Calculates seat price range (min, recommended, max) and fuel/trip cost totals.
 * Throws if the vehicle type is disabled in the config (null liters/km) or inputs are invalid.
 */
export function quote(p: QuoteParams, c: PricingConfig): QuoteBreakdown {
  const litersPer100 = c.litersPer100Km[p.vehicleType];
  const runningCost = c.runningCostEgpPerKm[p.vehicleType];

  if (litersPer100 == null || runningCost == null) {
    throw new Error(`Vehicle type "${p.vehicleType}" is disabled in pricing config`);
  }
  if (!Number.isInteger(p.seats) || p.seats <= 0) {
    throw new RangeError(`seats must be a positive integer, got: ${String(p.seats)}`);
  }
  if (p.distanceKm <= 0) {
    throw new RangeError(`distanceKm must be positive, got: ${String(p.distanceKm)}`);
  }

  const fuelCost =
    p.distanceKm * (litersPer100 / 100) * c.trafficFactor[p.trafficLevel] * c.fuelPriceEgpPerLiter;
  const tripCost = fuelCost + p.distanceKm * runningCost;

  return {
    min: floorEgp(fuelCost / p.seats),
    recommended: floorEgp(tripCost / p.seats),
    max: floorEgp((tripCost * (1 + c.marginCap)) / p.seats),
    fuelCostEgp: floorEgp(fuelCost),
    tripCostEgp: floorEgp(tripCost),
  };
}
