// createRide (BUILD_PLAN 8.5, with the v1.2 stops of 8.16): a host posts a ride.
import { Timestamp, type Transaction } from "firebase-admin/firestore";
import { quote, trafficLevelAt } from "../../domain/pricing.js";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { withIdempotency } from "../../lib/idempotency.js";
import { dateKey, minuteOfDay } from "../../lib/time.js";
import { hostRides, LIVE_RIDE_STATUSES, newRideRef, writeRide } from "../../repos/rides.js";
import { getRoute } from "../../repos/routes.js";
import { getUser } from "../../repos/users.js";
import { getVehicle } from "../../repos/vehicles.js";
import { getActivePoint } from "../../repos/zones.js";
import type { CreateRideInput } from "../../schemas/inputs.js";
import { CreateRideInput as CreateRideInputSchema } from "../../schemas/inputs.js";
import type { RideDoc, RideStop } from "../../schemas/docs.js";
import type { CreateRideOutput } from "../../schemas/io.js";

const MINUTE_MS = 60 * 1000;

// The statuses that take one of the host's rides for that Cairo day (check 8).
const DAY_RIDE_STATUSES = ["open", "full", "started", "finished"] as const;

/**
 * Reads each stop's point and checks the times (`[DECISION-28]`): every stop is after `departAt`,
 * in order, at least a minute apart, and before `departAt` plus the route's usual minutes.
 * The zod input only checks the shape, because these rules need Firestore (ADR 0003 row 75).
 */
async function buildStops(
  input: CreateRideInput,
  departAt: Date,
  usualMinutes: number,
  tx: Transaction,
): Promise<RideStop[]> {
  const stops: RideStop[] = [];
  const latest = departAt.getTime() + usualMinutes * MINUTE_MS;
  let previous = departAt.getTime();

  for (const [index, stop] of input.stops.entries()) {
    const point = await getActivePoint(stop.zone, stop.pointId, tx);
    if (!point) fail("STOPS_INVALID");

    const at = new Date(stop.at).getTime();
    if (at < previous + MINUTE_MS || at >= latest) fail("STOPS_INVALID");
    previous = at;

    stops.push({
      id: `s${String(index + 1)}`,
      zone: stop.zone,
      pointId: stop.pointId,
      pointName: point.name,
      point: { lat: point.lat, lng: point.lng },
      at: Timestamp.fromMillis(at),
    });
  }

  return stops;
}

export async function createRideCore(
  input: CreateRideInput,
  ctx: CallCtx,
): Promise<CreateRideOutput> {
  const [pricing, limits] = await Promise.all([getConfig("pricing"), getConfig("limits")]);
  const departAt = new Date(input.departAt);

  return withIdempotency(ctx.uid, input.requestId, async (tx) => {
    const user = await getUser(ctx.uid, tx);
    if (user?.host.status !== "approved") fail("HOST_NOT_APPROVED");

    const vehicle = await getVehicle(ctx.uid, input.vehicleId, tx);
    if (!vehicle || vehicle.status !== "approved") fail("VEHICLE_NOT_APPROVED");
    if (pricing.litersPer100Km[vehicle.type] == null) fail("SCOOTER_DISABLED");

    const fromPoint = await getActivePoint(input.fromZone, input.fromPointId, tx);
    const toPoint = await getActivePoint(input.toZone, input.toPointId, tx);
    const route = await getRoute(input.fromZone, input.toZone, tx);
    if (!fromPoint || !toPoint || !route) fail("ROUTE_NOT_SUPPORTED");

    const stops = await buildStops(input, departAt, route.usualMinutes, tx);

    const departMs = departAt.getTime();
    if (departMs < ctx.now.getTime() + limits.minLeadMinutes * MINUTE_MS) fail("DEPART_TOO_SOON");
    if (departMs > ctx.now.getTime() + limits.maxDaysAhead * 24 * 60 * MINUTE_MS) {
      fail("DEPART_TOO_FAR");
    }

    if (input.seats > vehicle.maxSeats) fail("SEATS_INVALID");

    const trafficLevel = trafficLevelAt(minuteOfDay(departAt), pricing);
    const range = quote(
      {
        distanceKm: route.distanceKm,
        vehicleType: vehicle.type,
        trafficLevel,
        seats: input.seats,
      },
      pricing,
    );
    if (input.pricePerSeat < range.min || input.pricePerSeat > range.max) {
      fail("PRICE_OUT_OF_RANGE");
    }

    if (input.womenOnly && user.gender !== "female") fail("WOMEN_ONLY_HOST");

    // One read of the host's rides serves checks 8, 9 and 10 (repos/rides.ts).
    const rides = await hostRides(ctx.uid, tx);
    const rideDateKey = dateKey(departAt);

    const sameDay = rides.filter(
      (r) => r.dateKey === rideDateKey && DAY_RIDE_STATUSES.some((s) => s === r.status),
    );
    if (
      sameDay.length >= limits.maxRidesPerHostPerDay ||
      sameDay.some((r) => r.direction === input.direction)
    ) {
      fail("DAILY_RIDE_LIMIT");
    }

    const live = rides.filter((r) => LIVE_RIDE_STATUSES.some((s) => s === r.status));
    const gapMs = limits.rideGapMinutes * MINUTE_MS;
    if (live.some((r) => Math.abs(r.departAt.toDate().getTime() - departMs) < gapMs)) {
      fail("RIDE_TIME_CONFLICT");
    }

    const inFreeMonth =
      user.host.freeUntil != null && ctx.now.getTime() < user.host.freeUntil.toDate().getTime();
    if (!inFreeMonth) {
      const held = live.filter((r) => r.fixedRideId === null).length;
      if (user.host.credits.regular < held + 1) fail("NO_CREDITS");
    }

    const pickupZones = [...new Set([input.fromZone, ...stops.map((s) => s.zone)])];
    const createdAt = Timestamp.fromDate(ctx.now);
    const ride: RideDoc = {
      schemaVersion: 1,
      createdAt,
      updatedAt: createdAt,
      hostId: ctx.uid,
      hostCompanyId: user.companyId,
      vehicleId: input.vehicleId,
      vehicle: {
        type: vehicle.type,
        make: vehicle.make,
        model: vehicle.model,
        color: vehicle.color,
        plate: vehicle.plate,
      },
      fromZone: input.fromZone,
      fromPointId: input.fromPointId,
      fromPointName: fromPoint.name,
      fromPoint: { lat: fromPoint.lat, lng: fromPoint.lng },
      toZone: input.toZone,
      toPointId: input.toPointId,
      toPointName: toPoint.name,
      stops,
      pickupZones,
      direction: input.direction,
      departAt: Timestamp.fromDate(departAt),
      dateKey: rideDateKey,
      seatsTotal: input.seats,
      seatsLeft: input.seats,
      pricePerSeat: input.pricePerSeat,
      priceRange: {
        min: range.min,
        recommended: range.recommended,
        max: range.max,
        trafficLevel,
        distanceKm: route.distanceKm,
        configVersion: pricing.version,
      },
      womenOnly: input.womenOnly,
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

    const ref = newRideRef();
    writeRide(tx, ref, ride);
    return { rideId: ref.id };
  });
}

export const createRide = makeCallable("createRide", CreateRideInputSchema, createRideCore);
