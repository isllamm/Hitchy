// Geospatial helpers (BUILD_PLAN 8.5) — pure logic, no Firebase imports.
import type { GeoPointLite } from "../schemas/docs.js";

// Mean Earth radius in meters (WGS84 spherical approximation).
const EARTH_RADIUS_M = 6_371_000;

const toRad = (deg: number): number => (deg * Math.PI) / 180;

/**
 * Great-circle distance between two coordinates in whole meters using the haversine formula.
 */
export function distanceM(a: GeoPointLite, b: GeoPointLite): number {
  if (a.lat === b.lat && a.lng === b.lng) return 0;

  const dLat = toRad(b.lat - a.lat);
  const dLng = toRad(b.lng - a.lng);
  const lat1 = toRad(a.lat);
  const lat2 = toRad(b.lat);

  const sinDLat2 = Math.sin(dLat / 2);
  const sinDLng2 = Math.sin(dLng / 2);

  const h = sinDLat2 * sinDLat2 + Math.cos(lat1) * Math.cos(lat2) * sinDLng2 * sinDLng2;
  const c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));

  return Math.round(EARTH_RADIUS_M * c);
}
