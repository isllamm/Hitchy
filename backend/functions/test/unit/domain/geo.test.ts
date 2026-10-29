import { describe, expect, it } from "vitest";
import { distanceM } from "../../../src/domain/geo.js";

describe("distanceM (haversine)", () => {
  const tahrir = { lat: 30.0444, lng: 31.2357 };
  const cairoAirport = { lat: 30.1219, lng: 31.4056 };
  const newCairo = { lat: 30.0074, lng: 31.4913 };
  const smartVillage = { lat: 30.0711, lng: 31.0214 };

  it("returns 0 for the exact same point", () => {
    expect(distanceM(tahrir, tahrir)).toBe(0);
    expect(distanceM({ lat: 0, lng: 0 }, { lat: 0, lng: 0 })).toBe(0);
  });

  it("is symmetric", () => {
    expect(distanceM(tahrir, cairoAirport)).toBe(distanceM(cairoAirport, tahrir));
    expect(distanceM(newCairo, smartVillage)).toBe(distanceM(smartVillage, newCairo));
  });

  it("returns whole integer meters", () => {
    const d = distanceM(tahrir, cairoAirport);
    expect(Number.isInteger(d)).toBe(true);
    expect(d).toBe(18480);
  });

  it("computes pilot route distance (New Cairo to Smart Village)", () => {
    const d = distanceM(newCairo, smartVillage);
    expect(d).toBe(45784);
  });

  it("computes antipodal and global distances correctly", () => {
    // North pole to South pole: pi * R = 20,015,087 m
    expect(distanceM({ lat: 90, lng: 0 }, { lat: -90, lng: 0 })).toBe(20015087);
    // Quarter circle along the equator: (pi / 2) * R = 10,007,543 m
    expect(distanceM({ lat: 0, lng: 0 }, { lat: 0, lng: 90 })).toBe(10007543);
  });
});
