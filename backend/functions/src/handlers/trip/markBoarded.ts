// markBoarded (BUILD_PLAN 8.7): the rider is in the car. All seats of a booking board together.
import { Timestamp } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { bookingRef, getBooking } from "../../repos/bookings.js";
import { getRide } from "../../repos/rides.js";
import { MarkBoardedInput } from "../../schemas/inputs.js";
import type { EmptyOutput } from "../../schemas/io.js";

export async function markBoardedCore(input: MarkBoardedInput, ctx: CallCtx): Promise<EmptyOutput> {
  return db.runTransaction(async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride) fail("NOT_ON_RIDE");

    // Either the owner marks any rider, or a rider marks themselves.
    if (ride.hostId !== ctx.uid && ctx.uid !== input.riderUid) fail("NOT_ON_RIDE");
    if (ride.status !== "started") fail("RIDE_NOT_STARTED");

    const booking = await getBooking(input.rideId, input.riderUid, tx);
    if (!booking || booking.status !== "booked") fail("BOOKING_NOT_FOUND");

    const at = Timestamp.fromDate(ctx.now);
    tx.update(bookingRef(input.rideId, input.riderUid), {
      status: "boarded",
      boardedAt: at,
      updatedAt: at,
    });
    return {};
  });
}

export const markBoarded = makeCallable("markBoarded", MarkBoardedInput, markBoardedCore);
