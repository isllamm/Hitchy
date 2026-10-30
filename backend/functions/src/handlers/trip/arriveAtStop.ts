// arriveAtStop (BUILD_PLAN 8.16, v1.2): the owner taps "I'm here" at a pickup, which starts the
// 5 free minutes for the riders waiting there (R9). No push: the rider's screen reads the booking.
import { Timestamp } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { bookingRef, listBookings } from "../../repos/bookings.js";
import { getRide } from "../../repos/rides.js";
import { ArriveAtStopInput } from "../../schemas/inputs.js";
import type { ArriveAtStopOutput } from "../../schemas/io.js";
import { pickupById } from "../booking/rideCard.js";

export async function arriveAtStopCore(
  input: ArriveAtStopInput,
  ctx: CallCtx,
): Promise<ArriveAtStopOutput> {
  return db.runTransaction(async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride || ride.hostId !== ctx.uid) fail("NOT_FOUND");
    if (ride.status !== "started") fail("RIDE_NOT_STARTED");
    if (!pickupById(ride, input.pickupId)) fail("PICKUP_NOT_ON_RIDE");

    const bookings = await listBookings(input.rideId, tx);
    const at = Timestamp.fromDate(ctx.now);

    // Only the riders waiting at this pickup, and only the first arrival counts.
    const waiting = bookings.filter(
      (b) => b.status === "booked" && b.pickupId === input.pickupId && b.hostArrivedAt === null,
    );
    for (const booking of waiting) {
      tx.update(bookingRef(input.rideId, booking.riderUid), { hostArrivedAt: at, updatedAt: at });
    }

    return { bookings: waiting.length };
  });
}

export const arriveAtStop = makeCallable("arriveAtStop", ArriveAtStopInput, arriveAtStopCore);
