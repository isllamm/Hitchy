// checkIn (BUILD_PLAN 8.7, with the booking's own pickup from 8.16): "I'm at the pickup point".
// The server's own time and distance are the evidence reportLate uses.
import { Timestamp } from "firebase-admin/firestore";
import { distanceM } from "../../domain/geo.js";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { bookingRef, getBooking } from "../../repos/bookings.js";
import { getRide } from "../../repos/rides.js";
import { CheckInInput } from "../../schemas/inputs.js";
import type { CheckInOutput } from "../../schemas/io.js";

const MINUTE_MS = 60 * 1000;
const OPEN_RIDE_STATUSES = ["open", "full", "started"];

export async function checkInCore(input: CheckInInput, ctx: CallCtx): Promise<CheckInOutput> {
  const limits = await getConfig("limits");

  return db.runTransaction(async (tx) => {
    const booking = await getBooking(input.rideId, ctx.uid, tx);
    if (!booking || booking.status !== "booked") fail("BOOKING_NOT_FOUND");

    const ride = await getRide(input.rideId, tx);
    if (!ride || !OPEN_RIDE_STATUSES.includes(ride.status)) fail("RIDE_NOT_OPEN");

    // v1.2: the window runs from this booking's own pickup time, not the ride's departure.
    const closesAt = booking.pickupAt.toDate().getTime() + limits.noShowMinutes * MINUTE_MS;
    if (ctx.now.getTime() > closesAt) fail("CHECK_IN_CLOSED");

    const distance = distanceM({ lat: input.lat, lng: input.lng }, booking.pickupPoint);
    const at = Timestamp.fromDate(ctx.now);
    tx.update(bookingRef(input.rideId, ctx.uid), {
      checkedInAt: at,
      checkInDistanceM: distance,
      updatedAt: at,
    });

    return { distanceM: distance };
  });
}

export const checkIn = makeCallable("checkIn", CheckInInput, checkInCore);
