// finishRide (BUILD_PLAN 8.7): the owner ends the trip. finishRideCore is the shared core that
// closeStaleRides (P8-B06) will also call. Everything after it runs in onRideFinished (P8-B06).
import { Timestamp, type Transaction } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { bookingRef, listBookings } from "../../repos/bookings.js";
import { addLedgerEntry } from "../../repos/ledger.js";
import { getRide, updateRide } from "../../repos/rides.js";
import { getUser } from "../../repos/users.js";
import { FinishRideInput } from "../../schemas/inputs.js";
import type { FinishRideOutput } from "../../schemas/io.js";

export interface FinishOptions {
  autoFinished: boolean;
}

/**
 * Ends one ride inside an open transaction: boarded bookings complete, bookings nobody boarded
 * become no-shows without a fee, and one credit is taken when at least one rider travelled and
 * the owner's free month is over (R3, R4).
 */
export async function finishRideCore(
  tx: Transaction,
  rideId: string,
  at: Date,
  opts: FinishOptions = { autoFinished: false },
): Promise<FinishRideOutput> {
  const ride = await getRide(rideId, tx);
  if (!ride) fail("NOT_FOUND");
  if (ride.status !== "started") fail("RIDE_NOT_STARTED");

  const host = await getUser(ride.hostId, tx);
  if (!host) fail("NOT_FOUND");

  const bookings = await listBookings(rideId, tx);
  const stamp = Timestamp.fromDate(at);

  let completedBookings = 0;
  for (const booking of bookings) {
    if (booking.status === "boarded") {
      tx.update(bookingRef(rideId, booking.riderUid), {
        status: "completed",
        updatedAt: stamp,
      });
      completedBookings += 1;
    } else if (booking.status === "booked") {
      // Nobody boarded them, and no fee follows from that alone (8.7).
      tx.update(bookingRef(rideId, booking.riderUid), { status: "no_show", updatedAt: stamp });
    }
  }

  const inFreeMonth =
    host.host.freeUntil != null && at.getTime() < host.host.freeUntil.toDate().getTime();
  const creditUsed = completedBookings > 0 && !inFreeMonth && !ride.creditConsumed;

  if (creditUsed) {
    const bucket = ride.fixedRideId !== null ? "fixed" : "regular";
    const balanceAfter = host.host.credits[bucket] - 1;

    // The balance may go below 0; createRide then blocks the next ride (8.7).
    tx.update(db.collection("users").doc(ride.hostId), {
      [`host.credits.${bucket}`]: balanceAfter,
      updatedAt: stamp,
    });
    addLedgerEntry(
      tx,
      ride.hostId,
      { delta: -1, bucket, reason: "trip", refId: rideId, balanceAfter },
      at,
    );
  }

  updateRide(tx, rideId, {
    status: "finished",
    finishedAt: stamp,
    creditConsumed: ride.creditConsumed || creditUsed,
    autoFinished: opts.autoFinished,
    updatedAt: stamp,
  });

  return { completedBookings, creditUsed };
}

export async function finishRideHandler(
  input: FinishRideInput,
  ctx: CallCtx,
): Promise<FinishRideOutput> {
  return db.runTransaction(async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride || ride.hostId !== ctx.uid) fail("NOT_FOUND");
    return finishRideCore(tx, input.rideId, ctx.now);
  });
}

export const finishRide = makeCallable("finishRide", FinishRideInput, finishRideHandler);
