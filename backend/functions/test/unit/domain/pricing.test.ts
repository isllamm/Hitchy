import { describe, expect, it } from "vitest";
import { floorEgp, quote, trafficLevelAt } from "../../../src/domain/pricing.js";
import type { PricingConfig } from "../../../src/schemas/config.js";
import type { TrafficLevel } from "../../../src/schemas/docs.js";

const testConfig: PricingConfig = {
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

describe("floorEgp", () => {
  it("floors numbers to integer EGP after rounding to 2 decimal places", () => {
    expect(floorEgp(97.9)).toBe(97);
    expect(floorEgp(139.15)).toBe(139);
    expect(floorEgp(166.98)).toBe(166);
    expect(floorEgp(48.95)).toBe(48);
    expect(floorEgp(99.999)).toBe(100);
    expect(floorEgp(50.0)).toBe(50);
  });
});

describe("trafficLevelAt", () => {
  // Helpers to convert "HH:mm" to minute of day for test readability
  const atTime = (hh: number, mm: number) => hh * 60 + mm;

  it("identifies offPeak, normal, and peak at boundary times", () => {
    // 06:59 -> offPeak
    expect(trafficLevelAt(atTime(6, 59), testConfig)).toBe("offPeak");
    // 07:00 -> peak (start of morning peak)
    expect(trafficLevelAt(atTime(7, 0), testConfig)).toBe("peak");
    // 09:59 -> peak
    expect(trafficLevelAt(atTime(9, 59), testConfig)).toBe("peak");
    // 10:00 -> normal (end of morning peak, start of normal)
    expect(trafficLevelAt(atTime(10, 0), testConfig)).toBe("normal");
    // 15:59 -> normal
    expect(trafficLevelAt(atTime(15, 59), testConfig)).toBe("normal");
    // 16:00 -> peak (start of evening peak)
    expect(trafficLevelAt(atTime(16, 0), testConfig)).toBe("peak");
    // 18:59 -> peak
    expect(trafficLevelAt(atTime(18, 59), testConfig)).toBe("peak");
    // 19:00 -> offPeak (end of evening peak)
    expect(trafficLevelAt(atTime(19, 0), testConfig)).toBe("offPeak");
  });

  it("handles night hours as offPeak", () => {
    expect(trafficLevelAt(atTime(0, 0), testConfig)).toBe("offPeak");
    expect(trafficLevelAt(atTime(3, 30), testConfig)).toBe("offPeak");
    expect(trafficLevelAt(atTime(23, 59), testConfig)).toBe("offPeak");
  });

  it("throws RangeError on out-of-range or non-integer minuteOfDayCairo", () => {
    expect(() => trafficLevelAt(-1, testConfig)).toThrow(RangeError);
    expect(() => trafficLevelAt(1440, testConfig)).toThrow(RangeError);
    expect(() => trafficLevelAt(2000, testConfig)).toThrow(RangeError);
    expect(() => trafficLevelAt(100.5, testConfig)).toThrow(RangeError);
  });
});

describe("quote", () => {
  // Golden test: 55 km, car, 8 L/100 km, 22.25 EGP/L, 0.75 EGP/km
  const goldenExpected: Record<
    TrafficLevel,
    Record<1 | 2 | 3, { min: number; recommended: number; max: number }>
  > = {
    offPeak: {
      1: { min: 97, recommended: 139, max: 166 },
      2: { min: 48, recommended: 69, max: 83 },
      3: { min: 32, recommended: 46, max: 55 },
    },
    normal: {
      1: { min: 112, recommended: 153, max: 184 },
      2: { min: 56, recommended: 76, max: 92 },
      3: { min: 37, recommended: 51, max: 61 },
    },
    peak: {
      1: { min: 127, recommended: 168, max: 202 },
      2: { min: 63, recommended: 84, max: 101 },
      3: { min: 42, recommended: 56, max: 67 },
    },
  };

  for (const level of ["offPeak", "normal", "peak"] as const) {
    describe(`golden test: trafficLevel = ${level}`, () => {
      for (const seats of [1, 2, 3] as const) {
        it(`calculates correct price range for ${String(seats)} seat(s)`, () => {
          const res = quote(
            {
              distanceKm: 55,
              vehicleType: "car",
              trafficLevel: level,
              seats,
            },
            testConfig,
          );

          const expected = goldenExpected[level][seats];
          expect(res.min).toBe(expected.min);
          expect(res.recommended).toBe(expected.recommended);
          expect(res.max).toBe(expected.max);
        });
      }
    });
  }

  it("calculates correct total trip and fuel costs", () => {
    const res = quote(
      {
        distanceKm: 55,
        vehicleType: "car",
        trafficLevel: "offPeak",
        seats: 1,
      },
      testConfig,
    );
    expect(res.fuelCostEgp).toBe(97);
    expect(res.tripCostEgp).toBe(139);
  });

  it("throws when vehicle type is disabled (scooter with null config)", () => {
    expect(() =>
      quote(
        {
          distanceKm: 55,
          vehicleType: "scooter",
          trafficLevel: "peak",
          seats: 1,
        },
        testConfig,
      ),
    ).toThrow(/disabled/);
  });

  it("throws RangeError on invalid seats or distance", () => {
    expect(() =>
      quote(
        {
          distanceKm: 55,
          vehicleType: "car",
          trafficLevel: "normal",
          seats: 0,
        },
        testConfig,
      ),
    ).toThrow(RangeError);

    expect(() =>
      quote(
        {
          distanceKm: 55,
          vehicleType: "car",
          trafficLevel: "normal",
          seats: 1.5,
        },
        testConfig,
      ),
    ).toThrow(RangeError);

    expect(() =>
      quote(
        {
          distanceKm: 0,
          vehicleType: "car",
          trafficLevel: "normal",
          seats: 1,
        },
        testConfig,
      ),
    ).toThrow(RangeError);

    expect(() =>
      quote(
        {
          distanceKm: -10,
          vehicleType: "car",
          trafficLevel: "normal",
          seats: 1,
        },
        testConfig,
      ),
    ).toThrow(RangeError);
  });
});
