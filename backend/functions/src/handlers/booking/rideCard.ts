// Building a RideCard, shared by searchRides and requestSeats (BUILD_PLAN 8.2, 8.6, 8.16).
import { distanceM } from "../../domain/geo.js";
import type {
  GeoPointLite,
  LocalizedName,
  ProfileDoc,
  RideDoc,
  UserDoc,
} from "../../schemas/docs.js";
import type { RideCard } from "../../schemas/io.js";

// The ride's own start point counts as a pickup, under the id "start" (v1.2, R24).
export const START_PICKUP_ID = "start";

export interface PickupChoice {
  id: string;
  name: LocalizedName;
  at: Date;
  point: GeoPointLite;
}

export function pickupsOf(ride: RideDoc): PickupChoice[] {
  return [
    {
      id: START_PICKUP_ID,
      name: ride.fromPointName,
      at: ride.departAt.toDate(),
      point: ride.fromPoint,
    },
    ...ride.stops.map((stop) => ({
      id: stop.id,
      name: stop.pointName,
      at: stop.at.toDate(),
      point: stop.point,
    })),
  ];
}

export function pickupById(ride: RideDoc, pickupId: string): PickupChoice | null {
  return pickupsOf(ride).find((p) => p.id === pickupId) ?? null;
}

/**
 * The pickup this rider would use: the start point or the stop in their zone, whichever is
 * nearest to their own point (8.16). Without a point, the earliest pickup in the zone wins,
 * because there is nothing to measure a walk against (ADR 0003 row 88).
 */
export function choosePickup(
  ride: RideDoc,
  riderZone: string,
  riderPoint: GeoPointLite | null,
): PickupChoice | null {
  const inZone = pickupsOf(ride).filter((pickup) =>
    pickup.id === START_PICKUP_ID
      ? ride.fromZone === riderZone
      : ride.stops.find((s) => s.id === pickup.id)?.zone === riderZone,
  );
  if (inZone.length === 0) return null;

  if (!riderPoint) {
    return inZone.reduce((best, p) => (p.at.getTime() < best.at.getTime() ? p : best));
  }
  return inZone.reduce((best, p) =>
    distanceM(riderPoint, p.point) < distanceM(riderPoint, best.point) ? p : best,
  );
}

export interface HostCardSource {
  uid: string;
  profile: ProfileDoc | null;
  user: UserDoc;
}

// The public card comes from profiles/{uid}; an account the syncPublicProfile trigger has not
// written yet falls back to the same fields on users/{uid} (ADR 0003 row 87).
function hostOfCard(source: HostCardSource, sameCompany: boolean): RideCard["host"] {
  const { profile, user } = source;
  const avgFromUser =
    user.host.ratingCount > 0
      ? Math.round((user.host.ratingSum / user.host.ratingCount) * 10) / 10
      : null;

  return {
    uid: source.uid,
    firstName: profile?.firstName ?? user.firstName,
    photoPath: profile?.photoPath ?? user.photoPath,
    rating: profile ? profile.hostRating.avg : avgFromUser,
    ratingCount: profile ? profile.hostRating.count : user.host.ratingCount,
    trips: profile ? profile.hostTrips : user.host.tripsCompleted,
    verified: profile?.verified ?? user.host.verified,
    tier: profile?.hostTier ?? user.host.tier,
    sameCompany,
  };
}

export interface CardParams {
  rideId: string;
  ride: RideDoc;
  pickup: PickupChoice;
  host: HostCardSource;
  usualMinutes: number;
  riderPoint: GeoPointLite | null;
  callerCompanyId: string | null;
  bestMatch: boolean;
}

export function toRideCard(params: CardParams): RideCard {
  const { ride, pickup } = params;
  const walkM = params.riderPoint ? distanceM(params.riderPoint, pickup.point) : null;
  const sameCompany =
    params.callerCompanyId != null && params.callerCompanyId === ride.hostCompanyId;

  return {
    rideId: params.rideId,
    departAt: ride.departAt.toDate().toISOString(),
    arriveAt: new Date(
      ride.departAt.toDate().getTime() + params.usualMinutes * 60000,
    ).toISOString(),
    fromZone: ride.fromZone,
    toZone: ride.toZone,
    fromPointId: ride.fromPointId,
    toPointId: ride.toPointId,
    fromPointName: ride.fromPointName,
    toPointName: ride.toPointName,
    pricePerSeat: ride.pricePerSeat,
    fairPrice: ride.pricePerSeat <= ride.priceRange.recommended,
    seatsLeft: ride.seatsLeft,
    walkM,
    pickup: {
      id: pickup.id,
      name: pickup.name,
      at: pickup.at.toISOString(),
      // No rider point means no walk to measure, so the card shows 0 (ADR 0003 row 88).
      walkM: walkM ?? 0,
    },
    bestMatch: params.bestMatch,
    womenOnly: ride.womenOnly,
    vehicleType: ride.vehicle.type,
    fixed: ride.fixedRideId !== null,
    host: hostOfCard(params.host, sameCompany),
  };
}
