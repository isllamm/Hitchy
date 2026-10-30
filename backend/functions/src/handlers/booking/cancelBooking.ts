// cancelBooking (BUILD_PLAN 8.6): the rider cancels a request or an accepted booking.
// The notifications come from the onBookingChanged trigger (P3-B06), never from here.
import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { bookingRef, getBooking, updateBooking } from "../../repos/bookings.js";
import { getRide, updateRide } from "../../repos/rides.js";
import { CancelBookingInput } from "../../schemas/inputs.js";
import type { CancelBookingOutput } from "../../schemas/io.js";

const MINUTE_MS = 60 * 1000;

export async function cancelBookingCore(
  input: CancelBookingInput,
  ctx: CallCtx,
): Promise<CancelBookingOutput> {
  const limits = await getConfig("limits");

  return db.runTransaction(async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride) fail("BOOKING_NOT_FOUND");

    const booking = await getBooking(input.rideId, ctx.uid, tx);
    if (!booking || (booking.status !== "requested" && booking.status !== "booked")) {
      fail("BOOKING_NOT_FOUND");
    }

    const wasBooked = booking.status === "booked";

    // "The owner is late" needs the owner's presence document to prove the distance (8.6 step 2),
    // and presence only arrives in Phase 8. Until then this reason is refused (ADR 0003 row 89).
    if (input.reason === "host_late") fail("HOST_NOT_LATE");

    // Cancelling a request that is still waiting is always free and skips the ride check.
    if (wasBooked && ride.status !== "open" && ride.status !== "full") fail("RIDE_HAS_STARTED");

    const departMs = ride.departAt.toDate().getTime();
    const lateCancel =
      wasBooked && ctx.now.getTime() >= departMs - limits.freeCancelMinutes * MINUTE_MS;

    const at = Timestamp.fromDate(ctx.now);
    updateBooking(tx, bookingRef(input.rideId, ctx.uid), {
      status: "cancelled_by_rider",
      cancelReason: input.reason,
      cancelledAt: at,
      lateCancel,
      updatedAt: at,
    });

    const seatsLeft = ride.seatsLeft + booking.seats;
    updateRide(tx, input.rideId, {
      seatsLeft,
      status: ride.status === "full" ? "open" : ride.status,
      bookingsCount: wasBooked ? Math.max(0, ride.bookingsCount - 1) : ride.bookingsCount,
      updatedAt: at,
    });

    if (lateCancel) {
      // An atomic increment, so the counter is right without reading the user first.
      tx.update(db.collection("users").doc(ctx.uid), {
        "rider.lateCancels": FieldValue.increment(1),
        updatedAt: at,
      });
    }

    return { lateCancel };
  });
}

export const cancelBooking = makeCallable("cancelBooking", CancelBookingInput, cancelBookingCore);
