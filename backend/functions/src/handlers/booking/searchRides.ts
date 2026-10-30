// searchRides (BUILD_PLAN 8.6, with the pickups of 8.16). searchRidesCore is reused by the
// assistant (Phase 11) and the backup flow (Phase 12), so all the logic lives there.
import { rankScore } from "../../domain/ranking.js";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { cairoDateTime } from "../../lib/time.js";
import { getProfile } from "../../repos/profiles.js";
import { getRoute } from "../../repos/routes.js";
import { searchOpenRides } from "../../repos/rides.js";
import { getUser } from "../../repos/users.js";
import { getPoint } from "../../repos/zones.js";
import type { GeoPointLite, ProfileDoc, RideDoc, RouteDoc, UserDoc } from "../../schemas/docs.js";
import { SearchRidesInput } from "../../schemas/inputs.js";
import type { SearchRidesOutput } from "../../schemas/io.js";
import { choosePickup, toRideCard, type PickupChoice } from "./rideCard.js";

const MINUTE_MS = 60 * 1000;
const HOUR_MS = 60 * MINUTE_MS;

/**
 * How much earlier than the rider's window a ride may leave and still pick them up inside it.
 * A stop is always later than `departAt`, and the query can only filter `departAt`, so the read
 * is widened by one route length and the pickup time is filtered afterwards (ADR 0003 row 86).
 */
const STOP_SLACK_MS = 90 * MINUTE_MS;

const MAX_RIDES_READ = 100;

interface Candidate {
  rideId: string;
  ride: RideDoc;
  pickup: PickupChoice;
  host: UserDoc;
  profile: ProfileDoc | null;
  route: RouteDoc;
}

export async function searchRidesCore(
  callerUid: string,
  input: SearchRidesInput,
): Promise<SearchRidesOutput> {
  const [limits, caller] = await Promise.all([getConfig("limits"), getUser(callerUid)]);
  if (!caller) fail("PROFILE_REQUIRED");

  const pickedAt = cairoDateTime(input.date, input.time);
  const pickedMs = pickedAt.getTime();
  const windowMs = limits.searchWindowMinutes * MINUTE_MS;

  const rides = await searchOpenRides({
    pickupZone: input.fromZone,
    toZone: input.toZone,
    departFrom: new Date(pickedMs - windowMs - STOP_SLACK_MS),
    departTo: new Date(pickedMs + windowMs),
    limit: MAX_RIDES_READ,
  });

  const riderPoint = input.fromPointId ? await pointOf(input.fromZone, input.fromPointId) : null;

  const hosts = new Map<string, { user: UserDoc; profile: ProfileDoc | null }>();
  const routes = new Map<string, RouteDoc | null>();
  const candidates: Candidate[] = [];

  for (const { rideId, ...ride } of rides) {
    if (ride.hostId === callerUid) continue; // the caller's own ride
    if (ride.seatsLeft < input.seats) continue;

    const pickup = choosePickup(ride, input.fromZone, riderPoint);
    if (!pickup) continue;

    // The window and the time chips use the rider's own pickup, not departAt (ADR 0003 row 86).
    const pickupMs = pickup.at.getTime();
    if (Math.abs(pickupMs - pickedMs) > windowMs) continue;
    if (!inTimeBand(pickupMs, pickedMs, input.timeBand)) continue;

    if (input.maxPrice != null && ride.pricePerSeat > input.maxPrice) continue;
    if (input.womenOnly && !ride.womenOnly) continue;
    if (ride.womenOnly && caller.gender !== "female") continue;

    let host = hosts.get(ride.hostId);
    if (!host) {
      const [user, profile] = await Promise.all([getUser(ride.hostId), getProfile(ride.hostId)]);
      if (!user) continue;
      host = { user, profile };
      hosts.set(ride.hostId, host);
    }

    if (host.user.suspended) continue;
    if (caller.blockedUids.includes(ride.hostId)) continue;
    if (host.user.blockedUids.includes(callerUid)) continue;

    const verified = host.profile?.verified ?? host.user.host.verified;
    if (input.verifiedOnly && !verified) continue;

    const rating = ratingOf(host.user, host.profile);
    if (input.minRating != null && (rating.count < 3 || (rating.avg ?? 0) < input.minRating)) {
      continue;
    }

    const key = `${ride.fromZone}__${ride.toZone}`;
    let route = routes.get(key);
    if (route === undefined) {
      route = await getRoute(ride.fromZone, ride.toZone);
      routes.set(key, route);
    }
    if (!route) continue;

    candidates.push({ rideId, ride, pickup, host: host.user, profile: host.profile, route });
  }

  const sorted = sortCandidates(candidates, input.sort, pickedMs, caller);
  const top = sorted.slice(0, limits.searchMaxResults);

  return {
    rides: top.map((candidate, index) =>
      toRideCard({
        rideId: candidate.rideId,
        ride: candidate.ride,
        pickup: candidate.pickup,
        host: { uid: candidate.ride.hostId, profile: candidate.profile, user: candidate.host },
        usualMinutes: candidate.route.usualMinutes,
        riderPoint,
        callerCompanyId: caller.companyId,
        // Only the first card of the "recommended" sort is the best match (8.6 step 4).
        bestMatch: input.sort === "recommended" && index === 0,
      }),
    ),
  };
}

async function pointOf(zoneId: string, pointId: string): Promise<GeoPointLite | null> {
  const point = await getPoint(zoneId, pointId);
  return point ? { lat: point.lat, lng: point.lng } : null;
}

function ratingOf(
  user: UserDoc,
  profile: ProfileDoc | null,
): { avg: number | null; count: number } {
  if (profile) return profile.hostRating;
  const count = user.host.ratingCount;
  return { avg: count > 0 ? user.host.ratingSum / count : null, count };
}

function inTimeBand(
  pickupMs: number,
  pickedMs: number,
  band: SearchRidesInput["timeBand"],
): boolean {
  switch (band) {
    case "before":
      return pickupMs < pickedMs;
    case "first_hour":
      return pickupMs >= pickedMs && pickupMs <= pickedMs + HOUR_MS;
    case "after":
      return pickupMs > pickedMs + HOUR_MS;
    default:
      return true;
  }
}

function sortCandidates(
  candidates: Candidate[],
  sort: SearchRidesInput["sort"],
  pickedMs: number,
  caller: UserDoc,
): Candidate[] {
  const byPickup = (a: Candidate, b: Candidate) => a.pickup.at.getTime() - b.pickup.at.getTime();

  if (sort === "earliest") return [...candidates].sort(byPickup);
  if (sort === "cheapest") {
    return [...candidates].sort(
      (a, b) => a.ride.pricePerSeat - b.ride.pricePerSeat || byPickup(a, b),
    );
  }

  const scores = new Map<string, number>();
  for (const c of candidates) {
    const rating = ratingOf(c.host, c.profile);
    scores.set(
      c.rideId,
      rankScore({
        pickupAtMs: c.pickup.at.getTime(),
        pickedTimeMs: pickedMs,
        sameCompany: caller.companyId != null && caller.companyId === c.ride.hostCompanyId,
        ratingAvg: rating.avg,
        ratingCount: rating.count,
        verified: c.profile?.verified ?? c.host.host.verified,
        tier: c.profile?.hostTier ?? c.host.host.tier,
        lateCancels: c.host.host.lateCancels,
        hostLateCount: c.host.host.hostLateCount,
        tripsCompleted: c.host.host.tripsCompleted,
      }),
    );
  }

  // Ties go to the earlier pickup (8.6).
  return [...candidates].sort(
    (a, b) => (scores.get(b.rideId) ?? 0) - (scores.get(a.rideId) ?? 0) || byPickup(a, b),
  );
}

export const searchRides = makeCallable("searchRides", SearchRidesInput, (input, ctx: CallCtx) =>
  searchRidesCore(ctx.uid, input),
);
