// requestSeats (BUILD_PLAN 8.6, with the pickup and wallet checks of 8.16).
// The request holds its seats until the owner answers or it expires (R20).
import { Timestamp } from "firebase-admin/firestore";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { withIdempotency } from "../../lib/idempotency.js";
import {
  bookingRef,
  LIVE_BOOKING_STATUSES,
  OWED_BOOKING_STATUSES,
  riderBookings,
  writeBooking,
} from "../../repos/bookings.js";
import { getProfile } from "../../repos/profiles.js";
import { getRide, updateRide } from "../../repos/rides.js";
import { getRoute } from "../../repos/routes.js";
import { getUser } from "../../repos/users.js";
import { getBalance } from "../../repos/wallets.js";
import type { BookingDoc } from "../../schemas/docs.js";
import { RequestSeatsInput } from "../../schemas/inputs.js";
import type { RequestSeatsOutput } from "../../schemas/io.js";
import { pickupById, toRideCard } from "./rideCard.js";

const MINUTE_MS = 60 * 1000;

// A booking in one of these statuses is finished and may be replaced by a new request (check 6).
const REPLACEABLE = ["declined", "expired", "cancelled_by_rider", "cancelled_by_host", "no_show"];

export async function requestSeatsCore(
  input: RequestSeatsInput,
  ctx: CallCtx,
): Promise<RequestSeatsOutput> {
  const limits = await getConfig("limits");

  const result = await withIdempotency(ctx.uid, input.requestId, async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride) fail("NOT_FOUND");
    if (ride.status !== "open") fail("RIDE_NOT_OPEN");

    const pickup = pickupById(ride, input.pickupId);
    if (!pickup) fail("PICKUP_NOT_ON_RIDE");

    const departMs = ride.departAt.toDate().getTime();
    if (departMs - ctx.now.getTime() <= limits.bookingCutoffMinutes * MINUTE_MS) {
      fail("TOO_LATE_TO_BOOK");
    }

    if (ride.hostId === ctx.uid) fail("OWN_RIDE");

    const [rider, host] = await Promise.all([getUser(ctx.uid, tx), getUser(ride.hostId, tx)]);
    if (!rider || !host) fail("NOT_FOUND");

    // A block is never revealed: the ride simply looks closed (check 4).
    if (rider.blockedUids.includes(ride.hostId) || host.blockedUids.includes(ctx.uid)) {
      fail("RIDE_NOT_OPEN");
    }

    if (ride.seatsLeft < input.seats) fail("RIDE_FULL");

    const ref = bookingRef(input.rideId, ctx.uid);
    const existing = await tx.get(ref);
    if (existing.exists) {
      const current = existing.data() as BookingDoc;
      if (!REPLACEABLE.includes(current.status)) fail("ALREADY_BOOKED");
    }

    if (rider.rider.unpaidFees > 0) fail("UNPAID_FEE");

    const total = ride.pricePerSeat * input.seats;
    const bookings = await riderBookings(ctx.uid, tx);

    // [DECISION-29]: the balance must cover this booking on top of everything still owed.
    const balance = await getBalance(ctx.uid, tx);
    const owed = bookings
      .filter(
        (b) =>
          b.rideId !== input.rideId &&
          b.paymentConfirmedAt === null &&
          OWED_BOOKING_STATUSES.includes(b.status),
      )
      .reduce((sum, b) => sum + b.total, 0);
    if (balance - owed < total) fail("INSUFFICIENT_BALANCE");

    if (ride.womenOnly && rider.gender !== "female") fail("WOMEN_ONLY");

    const gapMs = limits.rideGapMinutes * MINUTE_MS;
    const clashes = bookings.some(
      (b) =>
        b.rideId !== input.rideId &&
        LIVE_BOOKING_STATUSES.includes(b.status) &&
        Math.abs(b.departAt.toDate().getTime() - departMs) < gapMs,
    );
    if (clashes) fail("TIME_CONFLICT");

    // The deadline never runs past the booking cutoff before departure [DECISION-21].
    const expiresMs = Math.min(
      ctx.now.getTime() + limits.requestTtlMinutes * MINUTE_MS,
      departMs - limits.bookingCutoffMinutes * MINUTE_MS,
    );
    const at = Timestamp.fromDate(ctx.now);

    const booking: BookingDoc = {
      schemaVersion: 1,
      createdAt: at,
      updatedAt: at,
      rideId: input.rideId,
      riderUid: ctx.uid,
      hostId: ride.hostId,
      departAt: ride.departAt,
      status: "requested",
      seats: input.seats,
      price: ride.pricePerSeat,
      total,
      pickupId: pickup.id,
      pickupPointName: pickup.name,
      pickupPoint: pickup.point,
      pickupAt: Timestamp.fromDate(pickup.at),
      expiresAt: Timestamp.fromMillis(expiresMs),
      respondedAt: null,
      checkedInAt: null,
      checkInDistanceM: null,
      hostArrivedAt: null,
      boardedAt: null,
      lateFeeId: null,
      paymentConfirmedAt: null,
      verifiedTogether: false,
      rewarded: false,
      lateCancel: false,
      cancelledAt: null,
      cancelReason: null,
    };
    writeBooking(tx, ref, booking);

    // The seats stay held while the request waits for the owner (R20).
    const seatsLeft = ride.seatsLeft - input.seats;
    updateRide(tx, input.rideId, {
      seatsLeft,
      status: seatsLeft === 0 ? "full" : ride.status,
      updatedAt: at,
    });

    return {
      seats: input.seats,
      price: ride.pricePerSeat,
      total,
      expiresAt: new Date(expiresMs).toISOString(),
      pickupId: pickup.id,
    };
  });

  // The card is read after the commit: it only shows the ride back to the rider.
  const ride = await getRide(input.rideId);
  if (!ride) fail("NOT_FOUND");
  const [host, profile, route] = await Promise.all([
    getUser(ride.hostId),
    getProfile(ride.hostId),
    getRoute(ride.fromZone, ride.toZone),
  ]);
  if (!host || !route) fail("NOT_FOUND");

  const pickup = pickupById(ride, result.pickupId);
  if (!pickup) fail("PICKUP_NOT_ON_RIDE");

  return {
    booking: {
      rideId: input.rideId,
      status: "requested",
      seats: result.seats,
      price: result.price,
      total: result.total,
      expiresAt: result.expiresAt,
    },
    ride: toRideCard({
      rideId: input.rideId,
      ride,
      pickup,
      host: { uid: ride.hostId, profile, user: host },
      usualMinutes: route.usualMinutes,
      // requestSeats has no rider point in its input, so the card carries no walk distance.
      riderPoint: null,
      callerCompanyId: ctx.user?.companyId ?? null,
      bestMatch: false,
    }),
  };
}

export const requestSeats = makeCallable("requestSeats", RequestSeatsInput, requestSeatsCore);
